package com.dhy.qigsawbundle.plugin


import org.gradle.api.Project

class AutoPluginDelegate {

    void apply(Project baseProject) {
        baseProject.rootProject.subprojects { project ->
            project.afterEvaluate {
                if (project.plugins.hasPlugin("com.android.dynamic-feature")) {
                    insertModuleVersion(project)
                }
            }
        }

        baseProject.with {
            apply plugin: 'dynamic-provider-switch'
        }
    }

    private void insertModuleVersion(Project project) {
        project.with {
            apply plugin: 'insert-meta-inf'
            insertMetaInf {
                metaInfName = "module_version_" + project.name
                def versionName = project.android.defaultConfig.versionName
                def versionCode = project.android.defaultConfig.versionCode
                metaInfContent = "${versionName}@${versionCode}"
            }
        }
    }
}