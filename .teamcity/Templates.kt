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

import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Template
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.placeholder
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

object UnixTemplate : Template({
    name = "Unix Template"

    vcs {
        checkoutDir = "/dev/shm/%system.teamcity.buildType.id%/%system.build.number%"
    }

    requirements {
        doesNotContain("teamcity.agent.jvm.os.name", "Windows")
    }
})

object WindowsTemplate : Template({
    name = "Windows Template"

    requirements {
        contains("teamcity.agent.jvm.os.name", "Windows")
    }

    params {
        param("teamcity.agent.work.dir", "C:\\workspace")
    }
})

object DefaultTemplate : Template({
    name = "Default Template"

    vcs {
        root(DefaultRoot)
    }

    params {
        param("gradle.max.workers", "2")
        param("gradle.params", "--max-workers=%gradle.max.workers% --scan --build-cache -Dorg.elasticsearch.build.cache.url=https://gradle-enterprise.elastic.co/cache/")

        param("env.JAVA_HOME", "%teamcity.agent.jvm.user.home%/.java/openjdk14")
        param("env.RUNTIME_JAVA_HOME", "%teamcity.agent.jvm.user.home%/.java/openjdk11")
        param("env.JAVA7_HOME", "%teamcity.agent.jvm.user.home%/.java/java7")
        param("env.JAVA8_HOME", "%teamcity.agent.jvm.user.home%/.java/java8")
        param("env.JAVA9_HOME", "%teamcity.agent.jvm.user.home%/.java/java9")
        param("env.JAVA10_HOME", "%teamcity.agent.jvm.user.home%/.java/java10")
        param("env.JAVA11_HOME", "%teamcity.agent.jvm.user.home%/.java/java11")
        param("env.JAVA12_HOME", "%teamcity.agent.jvm.user.home%/.java/openjdk12")
        param("env.JAVA13_HOME", "%teamcity.agent.jvm.user.home%/.java/openjdk13")
        param("env.JAVA14_HOME", "%teamcity.agent.jvm.user.home%/.java/openjdk14")
        param("env.GRADLE_OPTS", "-XX:+HeapDumpOnOutOfMemoryError -Xmx128m -Xms128m")

        // For now these are just to ensure compatibility with existing Jenkins-based configuration
        param("env.JENKINS_URL", "%teamcity.serverUrl%")
        param("env.BUILD_URL", "%teamcity.serverUrl%/build/%teamcity.build.id%")
        param("env.JOB_NAME", "%system.teamcity.buildType.id%")
        param("env.GIT_BRANCH", "%vcsroot.branch%")
    }

    features {
        pullRequests {
            vcsRootExtId = "${DefaultRoot.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:c8d7c068-fdda-4800-92f2-106fcebbfca4"
                }
                filterTargetBranch = "+:refs/heads/${DslContext.projectName}"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
    }

    steps {
        script {
            name = "Setup Build Environment (Unix)"

            conditions {
                doesNotContain("teamcity.agent.jvm.os.name", "Windows")
            }

            scriptContent = """
                #!/usr/bin/env bash
                # drop page cache and kernel slab objects on linux
                [[ -x /usr/local/sbin/drop-caches ]] && sudo /usr/local/sbin/drop-caches

                # Copy Gradle init script to user home directory
                rm -Rfv ~/.gradle/init.d && mkdir -p ~/.gradle/init.d && cp -v .ci/teamcity.init.gradle ~/.gradle/init.d

                # Calculate number of Gradle worker threads to use
                if [ -f /proc/cpuinfo ] ; then
                   MAX_WORKERS=`grep '^cpu\scores' /proc/cpuinfo  | uniq | sed 's/\s\+//g' |  cut -d':' -f 2`
                else
                   if [[ "${'$'}OSTYPE" == "darwin"* ]]; then
                      MAX_WORKERS=`sysctl -n hw.physicalcpu | sed 's/\s\+//g'`
                   else
                      echo "Unsupported OS Type:${'$'}OSTYPE"
                      exit 1
                   fi
                fi
                if pwd | grep -v -q ^/dev/shm ; then
                   echo "Not running on a ramdisk, reducing number of workers"
                   MAX_WORKERS=${'$'}((${'$'}MAX_WORKERS*2/3))
                fi

                # Override default max workers build parameter to be used in subsequent build steps
                echo "##teamcity[setParameter name='gradle.max.workers' value='${'$'}MAX_WORKERS']"
            """.trimIndent()
        }

        script {
            name = "Setup Build Environment (Windows)"

            conditions {
                contains("teamcity.agent.jvm.os.name", "Windows")
            }

            scriptContent = """
                del /f /s /q %%USERPROFILE%%\.gradle\init.d\*.*
                mkdir %%USERPROFILE%%\.gradle\init.d
                rem copy .ci\init.gradle %%USERPROFILE%%\.gradle\init.d\
            """.trimIndent()
        }

        placeholder { }
    }
})
