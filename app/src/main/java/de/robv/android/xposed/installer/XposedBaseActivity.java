package de.robv.android.xposed.installer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import de.robv.android.xposed.installer.util.NavUtil;
import de.robv.android.xposed.installer.util.ThemeUtil;


public abstract class XposedBaseActivity extends AppCompatActivity {
	public boolean leftActivityWithSlideAnim = false;
	public int mTheme = -1;


	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		ThemeUtil.setTheme(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		XposedApp.setColors(getSupportActionBar(), XposedApp.getColor(this),
				this);
		ThemeUtil.reloadTheme(this);

		if (leftActivityWithSlideAnim)
			NavUtil.setTransitionSlideLeave(this);
		leftActivityWithSlideAnim = false;
	}

	public void setLeftWithSlideAnim(boolean newValue) {
		this.leftActivityWithSlideAnim = newValue;
	}
}
