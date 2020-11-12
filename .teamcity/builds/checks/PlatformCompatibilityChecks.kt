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

import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle

val platforms = setOf(
    "centos-7",
    "centos-8",
    "debian-9",
    "debian-10",
    "fedora-32",
    "opensuse-15",
    "oraclelinux-7",
    "sles-12",
    "sles-15",
    "ubuntu-18.04",
    "ubuntu-20.04",
    "windows-2012-r2",
    "windows-2016",
    "windows-2019"
)

val platformCompatibilityChecks = platforms.map { platform ->
    BuildType {
        id("PlatformCompatibilityCheck_${platform.toUpperCase()}")
        name = platform
        description = "Platform compatibility testing for \"${platform}\""

        requirements {
            contains("teamcity.agent.name", platform)
        }

        steps {
            gradle {
                useGradleWrapper = true
                gradleParams = "%gradle.params%"
                tasks = "check"
            }
        }
    }
}
