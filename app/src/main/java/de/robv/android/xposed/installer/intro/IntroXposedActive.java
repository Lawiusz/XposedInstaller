package de.robv.android.xposed.installer.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.robv.android.xposed.installer.R;
import de.robv.android.xposed.installer.XposedApp;

public class IntroXposedActive extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    String installedXposedVersion = XposedApp.getXposedProp().get("version");

    public static IntroXposedActive newInstance(int layoutResId) {
        IntroXposedActive intro = new IntroXposedActive();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        intro.setArguments(args);
        return intro;
    }

    private int layoutResId;

    public IntroXposedActive() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(layoutResId, container, false);
        TextView xactive = (TextView) v.findViewById(R.id.tv_xposed_ready);
        xactive.setText(getString(R.string.xposed_ready,
                installedXposedVersion));
        xactive.setVisibility(View.VISIBLE);
        return v;

    }

}
