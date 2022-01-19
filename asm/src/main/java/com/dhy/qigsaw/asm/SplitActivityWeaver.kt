package com.dhy.qigsaw.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*

private const val CLASS_WOVEN = "com/google/android/play/core/splitinstall/SplitInstallHelper"

private const val METHOD_WOVEN = "loadResources"

internal class ActivityClassVisitor(mv: ClassVisitor) : ClassVisitor(ASM5, mv), Opcodes {
    private lateinit var superName: String
    private var needInsert = true

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.superName = superName
    }

    override fun visitMethod(access: Int, name: String, descriptor: String?, signature: String?, exceptions: Array<String>?): MethodVisitor {
        if (name == "getResources") {
            needInsert = false
            return MergeGetResourcesMethod(ASM5, cv.visitMethod(access, name, descriptor, signature, exceptions), superName)
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        if (needInsert) insertGetResourcesMethod(cv)
        super.visitEnd()
    }

    private fun insertGetResourcesMethod(cw: ClassVisitor) {
        println("ActivityClassVisitor>>>insertGetResourcesMethod")
        val mv = cw.visitMethod(ACC_PUBLIC, "getResources", "()Landroid/content/res/Resources;", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false)
        mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, METHOD_WOVEN, "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, superName, "getResources", "()Landroid/content/res/Resources;", false)
        mv.visitInsn(ARETURN)
        mv.visitMaxs(2, 1)
        mv.visitEnd()
    }
}

private class MergeGetResourcesMethod(api: Int, mv: MethodVisitor, private val superClassName: String) : MethodVisitor(api, mv) {
    override fun visitCode() {
        println("ChangeOnCreateMethodVisitor>>>visitCode")
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, superClassName, "getResources", "()Landroid/content/res/Resources;", false)
        mv.visitMethodInsn(INVOKESTATIC, CLASS_WOVEN, METHOD_WOVEN, "(Landroid/app/Activity;Landroid/content/res/Resources;)V", false)
        mv.visitCode()
    }
}