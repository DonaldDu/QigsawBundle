package com.dhy.qigsawbundle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class QigsawBundlePlugin implements Plugin<Project> {
    private def QIGSAW = "qigsaw"
    private Project project
    private AutoPluginDelegate autoPlugin = new AutoPluginDelegate()

    @Override
    void apply(Project project) {
        this.project = project
        autoPlugin.apply(project)
        project.extensions.create("qigsawBundleOption", QigsawBundleOption)
        createTask()
    }

    private void createTask() {
        createBundleApkTask(true)
        createBundleApkTask(false)
    }

    private void createBundleApkTask(boolean bundle) {
        if (!project.hasProperty('BUNDLE_TOOL_PATH')) {
            throw new IllegalArgumentException('You must set BUNDLE_TOOL_PATH first, eg: project.ext.BUNDLE_TOOL_PATH')
        }
        project.extensions.android.applicationVariants.all { baseVariant ->
            QigsawBundleOption option = project.qigsawBundleOption
            def name = bundle ? "bundle" : "gen"
            BundleApkTask task = project.tasks.create("$name${baseVariant.name.capitalize()}Apks", BundleApkTask)
            task.aabFolder = new File(project.buildDir, option.aabFolder.call(baseVariant))
            task.apks = new File(task.aabFolder, "${project.name}.apks")
            task.bundleTool = project.BUNDLE_TOOL_PATH
            task.bundleOption = project.findProperty('qigsawBundleOption')
            if (bundle) task.dependsOn('bundle')
            task.setGroup(QIGSAW)
        }
    }
}