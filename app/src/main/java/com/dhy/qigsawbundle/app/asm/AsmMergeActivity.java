package com.dhy.qigsawbundle.app.asm;

import android.app.Activity;
import android.content.res.Resources;

/**
 * for test asm
 */
public class AsmMergeActivity extends Activity {
    @Override
    public Resources getResources() {
        System.out.println("getResources is invoked");
        return super.getResources();
    }
}
