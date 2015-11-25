package de.robv.android.xposed.installer.util;

import android.content.Context;

public class LContext {
    public static Context context;

    public static void setContext(Context mcontext) {
        if (context == null)
            context = mcontext;
    }
}
