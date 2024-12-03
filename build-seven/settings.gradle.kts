/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
rootProject.name = "build-seven"

pluginManagement {
    repositories {
        maven { url = uri("") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("") }
        google()
        mavenCentral()
    }
}

include(":plugins")

//abstract class BuildListenerService :
//    BuildService<BuildListenerService.Params>,
//    org.gradle.tooling.events.OperationCompletionListener {
//
//    interface Params : BuildServiceParameters
//
//    override fun onFinish(event: org.gradle.tooling.events.FinishEvent) {
//        println("BuildListenerService got event $event")
//    }
//}
//
//val buildServiceListener = gradle.sharedServices.registerIfAbsent("buildServiceListener", BuildListenerService::class.java) { }
//
//abstract class Services @Inject constructor(
//    val buildEventsListenerRegistry: BuildEventsListenerRegistry
//)
//
//val services = gradle.rootProject.objects.newInstance(Services::class)
//
//services.buildEventsListenerRegistry.onTaskCompletion(buildServiceListener)
