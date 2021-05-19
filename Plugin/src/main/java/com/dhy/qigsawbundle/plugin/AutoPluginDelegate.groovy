package com.dhy.qigsawbundle.plugin


import org.gradle.api.Project

class AutoPluginDelegate {

    void apply(Project baseProject) {
        baseProject.rootProject.subprojects { Project project ->
            if (project.hasProperty('android')) {
                insertModuleVersion(project)
            } else project.afterEvaluate {
                insertModuleVersion(project)
            }
        }

        baseProject.with {
            apply plugin: 'dynamic-provider-switch'
        }
    }

    private void insertModuleVersion(Project project) {
        if (project.plugins.hasPlugin("com.android.dynamic-feature")) {
            project.with {
                android.defaultConfig {
                    resValue "string", "module_version_${project.name}", "module_version_${versionName}@${versionCode}"
                }
            }
        }
    }
}