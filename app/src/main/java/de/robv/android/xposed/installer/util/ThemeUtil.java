package de.robv.android.xposed.installer.util;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;

import de.robv.android.xposed.installer.R;
import de.robv.android.xposed.installer.XposedApp;
import de.robv.android.xposed.installer.XposedBaseActivity;

public final class ThemeUtil {
	private static int[] THEMES = new int[]{
			R.style.Theme_XposedInstaller_Light_Teal,
			R.style.Theme_XposedInstaller_Light_Blue,
			R.style.Theme_XposedInstaller_Light_Red,
			R.style.Theme_XposedInstaller_Light_BlueGrey,
			R.style.Theme_XposedInstaller_Light_Purple,
			R.style.Theme_XposedInstaller_Light_Orange,
			R.style.Theme_XposedInstaller_Light_Pink,
			R.style.Theme_XposedInstaller_Light_Green,
			R.style.Theme_XposedInstaller_Dark_Blue,
			R.style.Theme_XposedInstaller_Dark_Teal,
			R.style.Theme_XposedInstaller_Dark_Red,
			R.style.Theme_XposedInstaller_Dark_BlueGrey,
			R.style.Theme_XposedInstaller_Dark_Purple,
			R.style.Theme_XposedInstaller_Dark_Orange,
			R.style.Theme_XposedInstaller_Dark_Pink,
			R.style.Theme_XposedInstaller_Dark_Green,
			R.style.Theme_XposedInstaller_Dark_Black,};

	private ThemeUtil() {
	}

	public static int getSelectTheme() {
		int theme = XposedApp.getPreferences().getInt("theme", 0);
		return (theme >= 0 && theme < THEMES.length) ? theme : 0;
	}

	public static void setTheme(XposedBaseActivity activity) {
		activity.mTheme = getSelectTheme();
		activity.setTheme(THEMES[activity.mTheme]);
	}

	public static void reloadTheme(XposedBaseActivity activity) {
		int theme = getSelectTheme();
		if (theme != activity.mTheme)
			activity.recreate();
	}

	public static int getThemeColor(Context context, int id) {
		Theme theme = context.getTheme();
		TypedArray a = theme.obtainStyledAttributes(new int[]{id});
		int result = a.getColor(0, 0);
		a.recycle();
		return result;
	}
}
