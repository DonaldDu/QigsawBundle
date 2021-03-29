package com.dhy.qigsawbundle.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

val projectRootDir: File by lazy {
    File(File("").absolutePath).parentFile
}