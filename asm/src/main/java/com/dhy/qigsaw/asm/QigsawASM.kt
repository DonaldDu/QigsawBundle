package com.dhy.qigsaw.asm

import org.gradle.api.Plugin
import org.gradle.api.Project

class QigsawASM : Plugin<Project> {
    override fun apply(project: Project) {
        ResourcesInjectorAsmFactory.register(project)
    }
}