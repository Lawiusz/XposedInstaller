package de.robv.android.xposed.installer;

import android.os.Bundle;
import android.view.View;


import com.github.paolorotolo.appintro.AppIntro2;

import de.robv.android.xposed.installer.intro.CustomIntro;
import de.robv.android.xposed.installer.intro.Intro1;
import de.robv.android.xposed.installer.intro.IntroXposedActive;


public class XposedIntro extends AppIntro2 {

    String installedXposedVersion = XposedApp.getXposedProp()
            .get("version");

    @Override
    public void init(Bundle savedInstanceState) {
        this.getWindow().setStatusBarColor(this.getResources().getColor(R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.getWindow().setNavigationBarColor(this.getResources().getColor(R.color.teal_900));
        addSlide(Intro1.newInstance(R.layout.intro1));
        addSlide(CustomIntro.newInstance(R.layout.intro_capabilities));
        addSlide(CustomIntro.newInstance(R.layout.intro_modules));
        addSlide(CustomIntro.newInstance(R.layout.intro_repo));

        if (installedXposedVersion == null) {
            addSlide(Intro1.newInstance(R.layout.intro_noxposed));
         } else {
           addSlide(IntroXposedActive.newInstance(R.layout.intro_xposed_active));

       }
        showStatusBar(true);
        setVibrate(true);
        setVibrateIntensity(30);
        setFadeAnimation();
    }


    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged() {
        // Do something when slide is changed
    }
}
