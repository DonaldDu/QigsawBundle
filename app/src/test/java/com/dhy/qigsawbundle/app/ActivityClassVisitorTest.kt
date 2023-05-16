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
        Assert.assertEquals("58f8f8ec9f09c166764a6deaad17c046", tmp.md5())
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
        Assert.assertEquals("4fbb2006b0038af8f677c4daf94e8ecc", tmp.md5())
    }
}

fun String.classNametoResourceStream(): InputStream? {
    val classLoader = Thread.currentThread().contextClassLoader!!
    return classLoader.getResourceAsStream(replace('.', '/') + ".class")
}