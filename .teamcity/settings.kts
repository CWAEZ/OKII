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

import builds.Intake
import builds.JavaPeriodic
import builds.PlatformPeriodic
import builds.PullRequest
import builds.checks.*
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.version

version = "2020.1"

project {
    vcsRoot(DefaultRoot)
    template(UnixTemplate)
    template(WindowsTemplate)

    params {
        param("teamcity.internal.webhooks.enable", "true")
        param("teamcity.internal.webhooks.events", "BUILD_STARTED;BUILD_FINISHED")
        param("teamcity.internal.webhooks.url", "https://homer.app.elstc.co/webhook/teamcity")
    }

    buildType(Intake)
    buildType(PullRequest)
    buildType(JavaPeriodic)
    buildType(PlatformPeriodic)

    subProject {
        id("Checks")
        name = "Checks"

        buildType(SanityCheck)
        buildType(OssChecks)
        buildType(XpackChecks)
        buildType(BwcChecks)

        subProject {
            id("JavaCompatibilityChecks")
            name = "Java Compatibility Checks"

            buildTypes(javaCompatibilityChecks)
        }

        subProject {
            id("PlatformCompatibilityChecks")
            name = "Platform Compatibility Checks"

            buildTypes(platformCompatibilityChecks)
        }
    }
}
