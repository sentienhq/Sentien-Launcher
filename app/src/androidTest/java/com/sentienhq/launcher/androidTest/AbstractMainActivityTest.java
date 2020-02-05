package com.sentienhq.launcher.androidTest;

import android.os.Build;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Rule;

import androidx.test.rule.ActivityTestRule;
import com.sentienhq.launcher.LauncherApplication;
import com.sentienhq.launcher.MainActivity;
import com.sentienhq.launcher.R;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

abstract class AbstractMainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Before
    public void setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + mActivityRule.getActivity().getPackageName()
                            + " android.permission.READ_CONTACTS");
        }

        mActivityRule.getActivity();

        // Initialize to default preferences
        LauncherApplication.getApplication(mActivityRule.getActivity()).getDataHandler().clearHistory();
        PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity()).edit().clear().apply();
        PreferenceManager.setDefaultValues(mActivityRule.getActivity(), R.xml.preferences, true);

        // Remove lock screen
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                mActivityRule.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        mActivityRule.getActivity().runOnUiThread(wakeUpDevice);
    }
}
