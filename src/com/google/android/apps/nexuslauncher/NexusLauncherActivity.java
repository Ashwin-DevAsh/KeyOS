package com.google.android.apps.nexuslauncher;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

public class NexusLauncherActivity extends Launcher {
    private NexusLauncher mLauncher;

    public NexusLauncherActivity() {
        mLauncher = new NexusLauncher(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = Utilities.getPrefs(this);
//        if (!FeedBridge.Companion.getInstance(this).isInstalled()) {
//            prefs.edit().putBoolean(SettingsActivity.ENABLE_MINUS_ONE_PREF, false).apply();
//        }
    }

    @Nullable
    public LauncherClient getGoogleNow() {
        return mLauncher.mClient;
    }


}
