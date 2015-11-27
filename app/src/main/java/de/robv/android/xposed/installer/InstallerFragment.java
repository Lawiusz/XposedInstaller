package de.robv.android.xposed.installer;

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

		List<String> messages = new LinkedList<>();
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

		List<String> messages = new LinkedList<>();

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
