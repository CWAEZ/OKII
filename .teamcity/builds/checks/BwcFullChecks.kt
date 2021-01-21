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

package builds.checks

import UnixTemplate
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import org.yaml.snakeyaml.Yaml
import java.io.File

val bwcVersions = Yaml().load<Map<String, Any>>(File(DslContext.baseDir, "bwcVersions").reader())["BWC_VERSION"] as List<String>

val bwcChecks = bwcVersions.map { version ->
    BuildType {
        id("BwcCheck_${version.replace(".", "_")}")
        name = "Elasticsearch $version"
        description = "Backward compatibility testing for version ${version}"

        templates(UnixTemplate)

        steps {
            gradle {
                useGradleWrapper = true
                gradleParams = "%gradle.params%"
                tasks = "v${version}#bwcTest"
            }
        }
    }
}
