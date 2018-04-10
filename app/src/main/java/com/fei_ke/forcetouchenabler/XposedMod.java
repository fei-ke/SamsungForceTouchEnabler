package com.fei_ke.forcetouchenabler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class XposedMod implements IXposedHookLoadPackage {
    private static final int EVENT_ID = 2;

    private static final int MSG_TAP        = 1;
    private static final int MSG_LONG_PRESS = 2;


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("android")) return;

        final Class<?> clazz = findClass("com.android.server.policy.NaviBarForceTouchManager", lpparam.classLoader);
        final Method forceClickImmersive = findMethodBestMatch(clazz, "forceClickImmersive", int.class);

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_LONG_PRESS) {
                    try {
                        forceClickImmersive.invoke(msg.obj, EVENT_ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        findAndHookMethod(clazz, "getEventId", int.class, int.class, boolean.class, XC_MethodReplacement.returnConstant(EVENT_ID));
        findAndHookMethod(clazz, "onForcePressed", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Message msg = handler.obtainMessage(MSG_LONG_PRESS, param.thisObject);
                handler.sendMessageDelayed(msg, 3000);

            }
        });
        findAndHookMethod(clazz, "onForceReleased", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (handler.hasMessages(MSG_LONG_PRESS)) {
                    handler.removeMessages(MSG_LONG_PRESS);
                }

                if (handler.hasMessages(MSG_TAP)) {
                    handler.removeMessages(MSG_TAP);
                    //double tap

                    Context context = (Context) getObjectField(param.thisObject, "mContext");
                    Intent cameraIntent = context.getPackageManager().getLaunchIntentForPackage("com.sec.android.app.camera");
                    cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    context.startActivity(cameraIntent);

                    param.setResult(null);
                } else {
                    handler.sendEmptyMessageDelayed(MSG_TAP, 600);
                }
            }
        });
    }

}