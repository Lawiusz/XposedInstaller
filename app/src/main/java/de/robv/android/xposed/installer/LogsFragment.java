package de.robv.android.xposed.installer;

import static de.robv.android.xposed.installer.XposedApp.WRITE_EXTERNAL_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.robv.android.xposed.installer.util.LoadLog;

public class LogsFragment extends Fragment {
	private File mFileErrorLog = new File(XposedApp.BASE_DIR + "log/error.log");
	private File mFileErrorLogOld = new File(
			XposedApp.BASE_DIR + "log/error.log.old");
	private TextView mTxtLog;
	private ScrollView mSVLog;
	private HorizontalScrollView mHSVLog;



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tab_logs, container, false);
		mTxtLog = (TextView) v.findViewById(R.id.txtLog);
		mTxtLog.setTextIsSelectable(true);
		mSVLog = (ScrollView) v.findViewById(R.id.svLog);
		mHSVLog = (HorizontalScrollView) v.findViewById(R.id.hsvLog);
		LoadLog task = new LoadLog(getActivity(), v);
		task.execute(mFileErrorLog);
		reloadErrorLog();
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_logs, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				LoadLog task = new LoadLog(getActivity(), getView());
				task.execute(mFileErrorLog);
				reloadErrorLog();
				return true;
			case R.id.menu_send:
				send();
				return true;
			case R.id.menu_save:
				save();
				return true;
			case R.id.menu_clear:
				clear();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void reloadErrorLog(){
		mSVLog.post(new Runnable() {
			@Override
			public void run() {
				mSVLog.scrollTo(0, mTxtLog.getHeight());
			}
		});
		mHSVLog.post(new Runnable() {
			@Override
			public void run() {
				mHSVLog.scrollTo(0, 0);
			}
		});
	}

	private void clear() {
		try {
			new FileOutputStream(mFileErrorLog).close();
			mFileErrorLogOld.delete();
			Toast.makeText(getActivity(), R.string.logs_cleared,
					Toast.LENGTH_SHORT).show();
			LoadLog task = new LoadLog(getActivity(), getView());
			task.execute(mFileErrorLog);
			reloadErrorLog();
		} catch (IOException e) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.logs_clear_failed) + "\n"
							+ e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void send() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mFileErrorLog));
		sendIntent.setType("application/text"); // text/plain is handled wrongly
												// by too many apps
		startActivity(Intent.createChooser(sendIntent,
				getResources().getString(R.string.menuSend)));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
			@NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions,
				grantResults);
		if (requestCode == WRITE_EXTERNAL_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(getActivity(), "Permission granted",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity(),
						"This feature will not work without permission to write external storage.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@SuppressLint("DefaultLocale")
	private void save() {
		if (ActivityCompat.checkSelfPermission(getActivity(),
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(),
					new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
					WRITE_EXTERNAL_PERMISSION);
			return;
		}

		if (!Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getActivity(), R.string.sdcard_not_writable,
					Toast.LENGTH_LONG).show();
			return;
		}

		Calendar now = Calendar.getInstance();
		String filename = String.format(
				"xposed_%s_%04d%02d%02d_%02d%02d%02d.log", "error",
				now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
		File targetFile = new File(getActivity().getExternalFilesDir(null),
				filename);

		try {
			FileInputStream in = new FileInputStream(mFileErrorLog);
			FileOutputStream out = new FileOutputStream(targetFile);
		byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.logs_save_failed) + "\n"
							+ e.getMessage(),
					Toast.LENGTH_LONG).show();
			return;
		}

		Toast.makeText(getActivity(), targetFile.toString(), Toast.LENGTH_LONG)
				.show();
	}

}
