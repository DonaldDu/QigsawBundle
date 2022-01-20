package com.dhy.qigsaw.asm

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor

abstract class ResourcesInjectorAsmFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {
    companion object {
        @JvmStatic
        fun register(project: Project) {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                variant.transformClassesWith(ResourcesInjectorAsmFactory::class.java, InstrumentationScope.ALL) {}
                variant.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }
    }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        return ActivityClassVisitor(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return if (classData.className == componentActivity) true else classData.isSubActivity
    }
}

private const val componentActivity = "androidx.core.app.ComponentActivity"
private val ClassData.isSubActivity: Boolean
    get() {
        var isActivity = false
        superClasses.forEach {
            if (it == componentActivity) return false
            if (it == "android.app.Activity") isActivity = true
        }
        return isActivity
    }