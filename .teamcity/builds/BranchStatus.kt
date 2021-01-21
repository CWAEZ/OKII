/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package builds

import UnixTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule

object BranchStatus : BuildType({
    name = "Branch Status"
    description = "Verify branch consistency and protection status"

    templates(UnixTemplate)

    triggers {
        schedule {
            branchFilter = "+:<default>"
            schedulingPolicy = daily()
        }
    }

    steps {
        script {
            name = "Verify GitHub development branch is protected"

            scriptContent = """
                #!/bin/bash
                set +x
                STATUS=${'$'}(curl -s https://api.github.com/repos/elastic/elasticsearch/branches/${DslContext.projectName} | jq '.protected')
                echo "Branch ${DslContext.projectName} protection status is: ${'$'}STATUS"
                if [[ "${'$'}STATUS" == "false" ]]; then
                  echo "Development branch ${DslContext.projectName} is not set as protected in GitHub but should be."
                  exit 1
                fi
            """.trimIndent()
        }
        gradle {
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            useGradleWrapper = true
            gradleParams = "%gradle.params%"
            tasks = "branchConsistency"
        }
    }
})
