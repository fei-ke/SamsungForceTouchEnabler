package com.fei_ke.forcetouchenabler;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;

public class XposedMod implements IXposedHookLoadPackage {
    private static final String TAG = "XposedMod";

    private static final int EVENT_ID = 2;

    private static final int MSG_TAP = 1;
    private static final int MSG_LONG_PRESS = 2;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            hookNaviBarForceTouchManager(lpparam);
            hookPhoneWindowManager(lpparam);
        }
    }

    private void hookPhoneWindowManager(LoadPackageParam lpparam) {
        Class<?> clazz = findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader);
        findAndHookMethod(clazz, "getNavigationBarHeight", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) getObjectField(param.thisObject, "mContext");
                ContentResolver resolver = context.getContentResolver();

                int isCustom = Settings.Global.getInt(resolver, BuildConfig.APPLICATION_ID + ".is_custom_navi_bar_height", 0);
                if (isCustom == 1) {
                    int heightDp = Settings.Global.getInt(resolver, BuildConfig.APPLICATION_ID + ".custom_navi_bar_height", 160);
                    int heightPx = (int) (heightDp * context.getResources().getDisplayMetrics().density + 0.5f);
                    param.setResult(heightPx);
                }
            }
        });
    }

    private void hookNaviBarForceTouchManager(LoadPackageParam lpparam) {
        final Class<?> clazz = findClass("com.android.server.policy.NaviBarForceTouchManager", lpparam.classLoader);

        findAndHookMethod(clazz, "getEventId", int.class, int.class, boolean.class, XC_MethodReplacement.returnConstant(EVENT_ID));
        findAndHookMethod(clazz, "onForceReleased", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                setBooleanField(param.thisObject, "mNaviBarVisible", false);
            }
        });
    }

}