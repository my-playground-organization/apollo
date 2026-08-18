/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    `java-library`
    alias(libs.plugins.docker)
    alias(libs.plugins.edc.build)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.edc.build.get().pluginId)
}

val shadowPluginId = libs.plugins.shadow.get().pluginId
subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin(shadowPluginId) &&
            file("${project.projectDir}/src/main/docker/Dockerfile").exists()
        ) {
            //actually apply the plugin to the (sub-)project
            apply(plugin = libs.plugins.docker.get().pluginId)

            tasks.register("dockerize", DockerBuildImage::class) {
                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
                images.add("ghcr.io/my-playground-organization/apollo/${project.name}:${project.version}")
                images.add("ghcr.io/my-playground-organization/apollo/${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null) {
                    platform.set(System.getProperty("platform"))
                }
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                inputDir.set(file(dockerContextDir))
                dependsOn("shadowJar")
            }
        }
    }
}