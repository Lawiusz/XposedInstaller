package de.robv.android.xposed.installer.util;

import android.app.Activity;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import de.robv.android.xposed.installer.R;
import de.robv.android.xposed.installer.XposedApp;
import de.robv.android.xposed.installer.XposedBaseActivity;

public final class NavUtil {
	public static final String FINISH_ON_UP_NAVIGATION = "finish_on_up_navigation";

	public static void setTransitionSlideEnter(Activity activity) {
		activity.overridePendingTransition(R.anim.slide_in_right,
				R.anim.slide_out_left);

		if (activity instanceof XposedBaseActivity)
			((XposedBaseActivity) activity).setLeftWithSlideAnim(true);
	}

	public static void setTransitionSlideLeave(Activity activity) {
		activity.overridePendingTransition(R.anim.slide_in_left,
				R.anim.slide_out_right);
	}

	public static Uri parseURL(String str) {
		if (str == null || str.isEmpty())
			return null;

		Spannable spannable = new SpannableString(str);
		Linkify.addLinks(spannable, Linkify.ALL);
		URLSpan spans[] = spannable.getSpans(0, spannable.length(),
				URLSpan.class);
		return (spans.length > 0) ? Uri.parse(spans[0].getURL()) : null;
	}

	public static void startURL(Activity activity, Uri uri) {

		CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
		customTabsIntent.setShowTitle(true);
		customTabsIntent.setToolbarColor(XposedApp.getColor(activity));
		customTabsIntent.build().launchUrl(activity, uri);
	}

	public static void startURL(Activity activity, String url) {
		startURL(activity, parseURL(url));
	}
}
