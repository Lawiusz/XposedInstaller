package de.robv.android.xposed.installer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.system.Os;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.robv.android.xposed.installer.util.AssetUtil;
import de.robv.android.xposed.installer.util.NotificationUtil;
import de.robv.android.xposed.installer.util.RootUtil;


public class InstallerFragment extends Fragment {

	List<String> messages = new LinkedList<>();
	private static final int READ_REQUEST_CODE = 42;
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
		Button btnInstall = (Button) v.findViewById(R.id.button_install);

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
				areYouSure(R.string.reboot, new MaterialDialog.ButtonCallback() {
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


		btnInstall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InstallWarning(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								performFileSearch();
							}

						});
					}
				});

			}
		});
		return v;
	}

	public void performFileSearch() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/zip");
		startActivityForResult(intent, READ_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

			if (resultData != null) {
				Uri uri = resultData.getData();
				String resolved = null;
				try (ParcelFileDescriptor fd = getActivity().getContentResolver().openFileDescriptor(uri, "r")) {
					final File procfsFdFile = new File("/proc/self/fd/" + fd.getFd());

					resolved = Os.readlink(procfsFdFile.getAbsolutePath());

					if (TextUtils.isEmpty(resolved)
							|| resolved.charAt(0) != '/'
							|| resolved.startsWith("/proc/")
							|| resolved.startsWith("/fd/"));
				} catch (Exception errnoe) {
					Log.e(XposedApp.TAG, "ReadError");
				}
				mRootUtil.execute("cp " + resolved + " /cache/xposed.zip", messages);
				installXposedZip();
			}
		}
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


	private void areYouSure(int messageTextId,
			MaterialDialog.ButtonCallback yesHandler) {
		new MaterialDialog.Builder(getActivity()).title(messageTextId)
				.content(R.string.areyousure)
				.iconAttr(android.R.attr.alertDialogIcon)
				.positiveText(android.R.string.yes)
				.negativeText(android.R.string.no).callback(yesHandler).show();
	}

	private void InstallWarning(MaterialDialog.ButtonCallback yesHandler) {
		new MaterialDialog.Builder(getActivity())
				.title(R.string.warning)
				.content(R.string.recovery_warning)
				.iconAttr(android.R.attr.alertDialogIcon)
				.positiveText(android.R.string.yes)
				.negativeText(android.R.string.no).callback(yesHandler).show();
	}

	private boolean startShell() {
		return mRootUtil.startShell();

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
		}
		AssetUtil.removeBusybox();
	}
	private void installXposedZip() {
		mRootUtil.execute("echo 'install /cache/xposed.zip' >/cache/recovery/openrecoveryscript ", messages);
		mRootUtil.execute("sync", messages);
		reboot("recovery");
	}

}
