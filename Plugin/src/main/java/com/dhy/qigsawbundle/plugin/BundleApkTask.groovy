package com.dhy.qigsawbundle.plugin


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class BundleApkTask extends DefaultTask {
    @Input
    File aabFolder
    @Input
    File apks
    @Input
    boolean log = true
    @Input
    QigsawBundleOption bundleOption
    private String bundleTool

    @TaskAction
    void generate() throws IOException {
        bundleTool = project.findProperty('BUNDLE_TOOL_PATH')
        if (bundleTool == null) {
            throw new IllegalArgumentException('You must set BUNDLE_TOOL_PATH first, eg: project.ext.BUNDLE_TOOL_PATH')
        }
        if (bundleOption.apkFileHost == null) {
            throw new IllegalArgumentException('You should set QigsawBundleOption.apkFileHost for apk url')
        }
        def aab = findAAB()
        if (aab == null || !aab.exists()) {
            throw new IllegalArgumentException('aab file not found, please fix aabFolder')
        }
        if (apks.exists()) apks.delete()
        def cmd = "java -jar $bundleTool build-apks --bundle=$aab --output=$apks "
        if (bundleOption.options != null && bundleOption.options.size() > 0) {
            cmd += bundleOption.options.join(' ')
        }
        println 'bundle to apks ...'
        if (log) println 'cmd: ' + cmd
        BundleApksUtil.INSTANCE.runCommand(cmd)
        BundleApksUtil.INSTANCE.bundleApks(bundleOption.apkFileHost, apks, bundleOption.keepLanguageConfigApks, bundleOption.copyToDirectory)
    }

    private File findAAB() {
        def aabs = aabFolder.listFiles().findAll { it.name.endsWith('.aab') }
        if (aabs.size() > 1) throw new IllegalArgumentException("More than one aab files were found, please delete the errors then restart task")
        else if (aabs.size() == 1) return aabs.first()
        else return null
    }
}