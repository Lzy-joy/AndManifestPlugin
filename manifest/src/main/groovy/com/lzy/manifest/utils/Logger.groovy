package com.lzy.manifest.utils

class Logger {
    public static boolean isEnable = true

    static def log(String msg) {
        if (!isEnable) {
            return
        }
        println msg
    }
}