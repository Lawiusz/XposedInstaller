package de.robv.android.xposed.installer;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.installer.util.RepoLoader;
import de.robv.android.xposed.installer.util.ThemeUtil;

public class SettingsActivity extends XposedBaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemeUtil.setTheme(this);
		setContentView(R.layout.activity_container);

			int acolor = XposedApp.lcolor(this);
			this.getWindow().setStatusBarColor(darkenColor(acolor, 0.85f));


		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setTitle(R.string.nav_item_settings);
			ab.setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment())
					.commit();
		}
	}

	public static class SettingsFragment extends PreferenceFragment {
		private static final File mDisableResourcesFlag = new File(XposedApp.BASE_DIR + "conf/disable_resources");

		public SettingsFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.prefs);

			findPreference("release_type_global").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					RepoLoader.getInstance().setReleaseTypeGlobal((String) newValue);
					return true;
				}
			});

			CheckBoxPreference prefDisableResources = (CheckBoxPreference) findPreference("disable_resources");
			prefDisableResources.setChecked(mDisableResourcesFlag.exists());
			prefDisableResources.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean enabled = (Boolean) newValue;
					if (enabled) {
						try {
							mDisableResourcesFlag.createNewFile();
						} catch (IOException e) {
							Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					} else {
						mDisableResourcesFlag.delete();
					}
					return (enabled == mDisableResourcesFlag.exists());
				}
			});

			Preference prefTheme = findPreference("theme");
			prefTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					getActivity().recreate();
					getActivity().finish(); // prevents 2 instances of settings from opening
					return true;
				}
			});
		}
	}
	public static int darkenColor(int color, float factor) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= factor;
		return Color.HSVToColor(hsv);
	}
}