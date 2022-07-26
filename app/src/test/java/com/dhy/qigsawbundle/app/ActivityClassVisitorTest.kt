package com.dhy.qigsawbundle.app

import com.dhy.qigsaw.asm.ActivityClassVisitor
import com.dhy.qigsawbundle.apkmd5.md5
import org.junit.Assert
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream


class ActivityClassVisitorTest {
    @Test
    fun testAsmInsert() {
        val inputStream = "com.dhy.qigsawbundle.app.asm.AsmInsertActivity".classNametoResourceStream()
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val cv = ActivityClassVisitor(cw)
        cr.accept(cv, Opcodes.ASM5)
        val tmp = File("AsmInsertActivity.class")
        tmp.writeBytes(cw.toByteArray())
        println(tmp.absolutePath)
        Assert.assertEquals("f9541f4768e6eaeb37eb76e6b38e0e30", tmp.md5())
    }

    @Test
    fun testAsmMerge() {
        val inputStream = "com.dhy.qigsawbundle.app.asm.AsmMergeActivity".classNametoResourceStream()
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val cv = ActivityClassVisitor(cw)
        cr.accept(cv, Opcodes.ASM5)
        val tmp = File("AsmMergeActivity.class")
        tmp.writeBytes(cw.toByteArray())
        println(tmp.absolutePath)
        Assert.assertEquals("bce3be74fb84d28eb42993e653b47bdd", tmp.md5())
    }
}

fun String.classNametoResourceStream(): InputStream? {
    val classLoader = Thread.currentThread().contextClassLoader!!
    return classLoader.getResourceAsStream(replace('.', '/') + ".class")
}