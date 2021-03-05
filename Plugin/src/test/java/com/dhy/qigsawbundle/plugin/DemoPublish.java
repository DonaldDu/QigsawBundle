package com.dhy.qigsawbundle.plugin;

import com.google.gson.Gson;

public class DemoPublish {
    public static void main(String[] args) {
        System.out.println(new Gson().toJson(args));
    }
}
