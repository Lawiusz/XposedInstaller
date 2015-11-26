package de.robv.android.xposed.installer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.LinkedList;
import java.util.List;

import de.robv.android.xposed.installer.util.AssetUtil;
import de.robv.android.xposed.installer.util.NotificationUtil;
import de.robv.android.xposed.installer.util.RootUtil;


public class InstallerFragment extends Fragment {
	private RootUtil mRootUtil = new RootUtil();
	private MaterialDialog.Builder dlgProgress;

	private static int extractIntPart(String str) {
		int result = 0, length = str.length();
		for (int offset = 0; offset < length; offset++) {
			char c = str.charAt(offset);
			if ('0' <= c && c <= '9')
				result = result * 10 + (c - '0');
			else
				break;
		}
		return result;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		dlgProgress = new MaterialDialog.Builder(activity).progress(true, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_installer, container, false);
		TextView txtInstallError = (TextView) v
				.findViewById(R.id.framework_install_errors);
		TextView xposedThread = (TextView) v.findViewById(R.id.xposed_xda_thread);
		xposedThread.setMovementMethod(LinkMovementMethod.getInstance());
		Button btnSoftReboot = (Button) v.findViewById(R.id.btnSoftReboot);
		Button btnReboot = (Button) v.findViewById(R.id.btnReboot);
		Button btnRebootRecovery = (Button) v.findViewById(R.id.btnRebootRecovery);


		// FIXME
		/*
		 * boolean isCompatible = false; if (BINARIES_FOLDER == null) { //
		 * incompatible processor architecture } else if (Build.VERSION.SDK_INT
		 * == 15) { APP_PROCESS_NAME = BINARIES_FOLDER +
		 * "app_process_xposed_sdk15"; isCompatible = checkCompatibility();
		 *
		 * } else if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <=
		 * 19) { APP_PROCESS_NAME = BINARIES_FOLDER +
		 * "app_process_xposed_sdk16"; isCompatible = checkCompatibility();
		 *
		 * } else if (Build.VERSION.SDK_INT > 19) { APP_PROCESS_NAME =
		 * BINARIES_FOLDER + "app_process_xposed_sdk16"; isCompatible =
		 * checkCompatibility(); if (isCompatible) {
		 * txtInstallError.setText(String.format(getString(R.string.
		 * not_tested_but_compatible), Build.VERSION.SDK_INT));
		 * txtInstallError.setVisibility(View.VISIBLE); } }
		 */

		// FIXME
		// TODO: update android 6.0 permission manager when final version of
		// xposed installer is available
		/*
		 * if (isCompatible) { btnInstall.setOnClickListener(new
		 * AsyncClickListener(btnInstall.getText()) {
		 *
		 * @Override public void onAsyncClick(View v) { final boolean success =
		 * install(); getActivity().runOnUiThread(new Runnable() {
		 *
		 * @Override public void run() { refreshVersions(); if (success)
		 * ModuleUtil.getInstance().updateModulesList(false);
		 *
		 * // Start tracking the last seen version, irrespective of the
		 * installation method and the outcome. // 0 or a stale version might be
		 * registered, if a recovery installation was requested // It will get
		 * up to date when the last seen version is updated on a later panel
		 * startup
		 * XposedApp.getPreferences().edit().putInt(PREF_LAST_SEEN_BINARY,
		 * appProcessInstalledVersion).commit(); // Dismiss any warning already
		 * being displayed
		 * getView().findViewById(R.id.install_reverted_warning).setVisibility(
		 * View.GONE); } }); } }); } else { String errorText =
		 * String.format(getString(R.string.phone_not_compatible),
		 * Build.VERSION.SDK_INT, Build.CPU_ABI); if
		 * (!mCompatibilityErrors.isEmpty()) errorText += "\n\n" +
		 * TextUtils.join("\n", mCompatibilityErrors);
		 * txtInstallError.setText(errorText);
		 * txtInstallError.setVisibility(View.VISIBLE);
		 * btnInstall.setEnabled(false); }
		 *
		 * btnUninstall.setOnClickListener(new
		 * AsyncClickListener(btnUninstall.getText()) {
		 *
		 * @Override public void onAsyncClick(View v) { uninstall();
		 * getActivity().runOnUiThread(new Runnable() {
		 *
		 * @Override public void run() { refreshVersions();
		 *
		 * // Update tracking of the last seen version if
		 * (appProcessInstalledVersion == 0) { // Uninstall completed, check if
		 * an Xposed binary doesn't reappear
		 * XposedApp.getPreferences().edit().putInt(PREF_LAST_SEEN_BINARY,
		 * -1).commit(); } else { // Xposed binary still in place. // Stop
		 * tracking last seen version, as uninstall might complete later or not
		 * XposedApp.getPreferences().edit().remove(PREF_LAST_SEEN_BINARY).
		 * commit(); } // Dismiss any warning already being displayed
		 * getView().findViewById(R.id.install_reverted_warning).setVisibility(
		 * View.GONE); } }); } });
		 */

		String installedXposedVersion = XposedApp.getXposedProp()
				.get("version");
		if (installedXposedVersion == null) {
			txtInstallError.setText(R.string.installation_lollipop);
			txtInstallError
					.setTextColor(getResources().getColor(R.color.warning));
		} else {
			int installedXposedVersionInt = extractIntPart(
					installedXposedVersion);
			if (installedXposedVersionInt == XposedApp
					.getActiveXposedVersion()) {
				txtInstallError.setText(getString(R.string.installed_lollipop,
						installedXposedVersion));
				txtInstallError.setTextColor(
						getResources().getColor(R.color.darker_green));
			} else {
				txtInstallError
						.setText(getString(R.string.installed_lollipop_inactive,
								installedXposedVersion));
				txtInstallError
						.setTextColor(getResources().getColor(R.color.warning));
			}
		}
		txtInstallError.setVisibility(View.VISIBLE);


		btnReboot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				areYouSure(R.string.reboot,
						new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								super.onPositive(dialog);
								reboot(null);
							}
						});
			}
		});

		btnRebootRecovery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				areYouSure(R.string.recovery_reboot,
						new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								super.onPositive(dialog);
								reboot("recovery");
							}
						});
			}
		});

		btnSoftReboot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				areYouSure(R.string.soft_reboot,
						new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								super.onPositive(dialog);
								softReboot();
							}
						});
			}
		});

		if (!XposedApp.getPreferences().getBoolean("hide_install_warning",
				false)) {
			final View dontShowAgainView = inflater
					.inflate(R.layout.dialog_install_warning, null);

			new MaterialDialog.Builder(getActivity())
					.title(R.string.install_warning_title)
					.customView(dontShowAgainView, false)
					.positiveText(android.R.string.ok)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							super.onPositive(dialog);
							CheckBox checkBox = (CheckBox) dontShowAgainView
									.findViewById(android.R.id.checkbox);
							if (checkBox.isChecked())
								XposedApp.getPreferences().edit().putBoolean(
										"hide_install_warning", true).apply();
						}
					}).cancelable(false).show();
		}

		/*
		 * Detection of reverts to /system/bin/app_process. LastSeenBinary can
		 * be: missing - do nothing -1 - Uninstall was performed, check if an
		 * Xposed binary didn't reappear >= 0 - Make sure a downgrade or
		 * non-xposed binary doesn't occur Also auto-update the value to the
		 * latest version found
		 */
		/*
		 * int lastSeenBinary =
		 * XposedApp.getPreferences().getInt(PREF_LAST_SEEN_BINARY,
		 * Integer.MIN_VALUE); if (lastSeenBinary != Integer.MIN_VALUE) { final
		 * View vInstallRevertedWarning =
		 * v.findViewById(R.id.install_reverted_warning); final TextView
		 * txtInstallRevertedWarning = (TextView)
		 * v.findViewById(R.id.install_reverted_warning_text);
		 * vInstallRevertedWarning.setOnClickListener(new View.OnClickListener()
		 * {
		 *
		 * @Override public void onClick(View v) { // Stop tracking and dismiss
		 * the info panel
		 * XposedApp.getPreferences().edit().remove(PREF_LAST_SEEN_BINARY).
		 * commit(); vInstallRevertedWarning.setVisibility(View.GONE); } });
		 *
		 * if (lastSeenBinary < 0 && appProcessInstalledVersion > 0) { //
		 * Uninstall was previously completed but an Xposed binary has
		 * reappeared txtInstallRevertedWarning.setText(getString(R.string.
		 * uninstall_reverted, versionToText(appProcessInstalledVersion)));
		 * vInstallRevertedWarning.setVisibility(View.VISIBLE); } else if
		 * (appProcessInstalledVersion < lastSeenBinary) { // Previously
		 * installed binary was either restored to stock or downgraded, probably
		 * // following a reboot on a locked system
		 * txtInstallRevertedWarning.setText(getString(R.string.
		 * install_reverted, versionToText(lastSeenBinary),
		 * versionToText(appProcessInstalledVersion)));
		 * vInstallRevertedWarning.setVisibility(View.VISIBLE); } else if
		 * (appProcessInstalledVersion > lastSeenBinary) { // Current binary is
		 * newer, register it and keep monitoring for future downgrades
		 * XposedApp.getPreferences().edit().putInt(PREF_LAST_SEEN_BINARY,
		 * appProcessInstalledVersion).commit(); } else { // All is ok } }
		 */

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		NotificationUtil.cancel(NotificationUtil.NOTIFICATION_MODULES_UPDATED);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRootUtil.dispose();
	}

	private void showAlert(final String result) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showAlert(result);
				}
			});
			return;
		}

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.content(result).positiveText(android.R.string.ok).build();
		dialog.show();

		TextView txtMessage = (TextView) dialog
				.findViewById(android.R.id.message);
		txtMessage.setTextSize(14);
	}

	private void areYouSure(int messageTextId,
			MaterialDialog.ButtonCallback yesHandler) {
		new MaterialDialog.Builder(getActivity()).title(messageTextId)
				.content(R.string.areyousure)
				.iconAttr(android.R.attr.alertDialogIcon)
				.positiveText(android.R.string.yes)
				.negativeText(android.R.string.no).callback(yesHandler).show();
	}

	private boolean startShell() {
		if (mRootUtil.startShell())
			return true;

		showAlert(getString(R.string.root_failed));
		return false;
	}


	private void softReboot() {
		if (!startShell())
			return;

		List<String> messages = new LinkedList<String>();
		if (mRootUtil.execute(
				"setprop ctl.restart surfaceflinger; setprop ctl.restart zygote",
				messages) != 0) {
			messages.add("");
			messages.add(getString(R.string.reboot_failed));
			showAlert(TextUtils.join("\n", messages).trim());
		}
	}

	private void reboot(String mode) {
		if (!startShell())
			return;

		List<String> messages = new LinkedList<String>();

		String command = "reboot";
		if (mode != null) {
			command += " " + mode;
			if (mode.equals("recovery"))
				// create a flag used by some kernels to boot into recovery
				mRootUtil.executeWithBusybox("touch /cache/recovery/boot",
						messages);
		}

		if (mRootUtil.executeWithBusybox(command, messages) != 0) {
			messages.add("");
			messages.add(getString(R.string.reboot_failed));
			showAlert(TextUtils.join("\n", messages).trim());
		}
		AssetUtil.removeBusybox();
	}

}
