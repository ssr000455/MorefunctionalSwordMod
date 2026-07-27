package com.qidai.morefunctionalswordmod.detect;

public class NativeDetect {
    static {
        // 由NativeLoader加载，这里不需要重复加载
    }

    public static native void triggerScan();
    public static native int getCheatCount();
    public static native int getAnticheatCount();
    public static native boolean isDebuggerDetected();
}