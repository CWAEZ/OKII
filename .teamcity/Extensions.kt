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

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule

fun BuildType.dependsOn(buildType: BuildType, init: SnapshotDependency.() -> Unit) {
    dependencies {
        snapshot(buildType) {
            reuseBuilds = ReuseBuilds.SUCCESSFUL
            onDependencyCancel = FailureAction.CANCEL
            onDependencyFailure = FailureAction.CANCEL
            synchronizeRevisions = true
            init()
        }
    }
}

fun BuildType.lastGoodCommit(lgcBuildType: BuildType, init: ScheduleTrigger.() -> Unit) {
    dependsOn(lgcBuildType)
    triggers {
        schedule {
            triggerBuild = onWatchedBuildChange {
                buildType = "${lgcBuildType.id}"
                watchedBuildRule = ScheduleTrigger.WatchedBuildRule.LAST_SUCCESSFUL
            }
            branchFilter = "+:<default>"
            init()
        }
    }
}

fun BuildType.dependsOn(vararg buildTypes: BuildType, init: SnapshotDependency.() -> Unit) {
    buildTypes.forEach { dependsOn(it, init) }
}

fun BuildType.dependsOn(buildTypes: List<BuildType>, init: SnapshotDependency.() -> Unit) {
    buildTypes.forEach { dependsOn(it, init) }
}

fun BuildType.dependsOn(buildType: BuildType) {
    dependsOn(buildType) {}
}

fun Project.buildTypes(buildTypes: List<BuildType>) {
    buildTypes.forEach(this::buildType)
    buildTypesOrder = buildTypes
}
