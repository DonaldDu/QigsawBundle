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
    String bundleTool
    @Input
    QigsawBundleOption bundleOption
    @Input
    boolean log = true

    @TaskAction
    void generate() throws IOException {
        if (bundleOption == null) {
            println 'You should set QigsawBundleOption'
            return
        }
        def aab = findAAB()
        if (aab == null || !aab.exists()) {
            println 'aab file not found, please fix aabFolder'
            return
        }
        if (log) println 'BundleApkTask ' + aab.absolutePath
        if (bundleOption.apkFileHost == null) {
            if (log) println 'You should QigsawBundleOption.apkFileHost for apk url'
            return
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