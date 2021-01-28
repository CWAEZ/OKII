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

import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import lastGoodCommit

object EncryptionAtRestPeriodic : BuildType({
    name = "Encryption at Rest Periodic"

    requirements {
        doesNotContain("teamcity.agent.jvm.os.name", "Windows")
        contains("teamcity.agent.name", "packaging")
    }


    lastGoodCommit(Intake) {
        schedulingPolicy = cron {
            hours = "10/12"
        }
    }

    steps {
        script {
            name = "Setup Encrypted volume"

            scriptContent = """
                #!/bin/bash
                # Configure a dm-crypt volume backed by a file
                set -e
                dd if=/dev/zero of=dm-crypt.img bs=1 count=0 seek=60GB
                dd if=/dev/urandom of=key.secret bs=2k count=1
                LOOP=${'$'}(losetup -f)
                sudo losetup ${'$'}LOOP dm-crypt.img
                sudo cryptsetup luksFormat -q --key-file key.secret "${'$'}LOOP"
                sudo cryptsetup open --key-file key.secret "${'$'}LOOP" secret --verbose
                sudo mkfs.ext2 /dev/mapper/secret
                sudo mkdir /mnt/secret
                sudo mount /dev/mapper/secret /mnt/secret
                sudo chown -R jenkins /mnt/secret
                cp -r "%teamcity.build.checkoutDir%" /mnt/secret
                cd /mnt/secret/${'$'}(basename "%teamcity.build.checkoutDir%")
                touch .output.log
                rm -Rf "%teamcity.build.checkoutDir%"
                ln -s "${'$'}PWD" "%teamcity.build.checkoutDir%"
            """.trimIndent()
        }
        gradle {
            useGradleWrapper = true
            gradleParams = "%gradle.params%"
            tasks = "check"
        }
    }
})
