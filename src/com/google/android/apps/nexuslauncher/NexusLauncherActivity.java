package com.google.android.apps.nexuslauncher;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import tech.DevAsh.KeyOS.Database.RealmHelper;
import tech.DevAsh.keyOS.Database.User;

public class NexusLauncherActivity extends Launcher {
    private NexusLauncher mLauncher;

    public NexusLauncherActivity() {
        mLauncher = new NexusLauncher(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    public LauncherClient getGoogleNow() {
        return mLauncher.mClient;
    }


}
