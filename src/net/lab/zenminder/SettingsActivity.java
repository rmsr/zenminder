package net.lab.zenminder;

import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends BaseChildActivity
{
    private static final String TAG = "ZenMinder.SettingsActivity";

    int getContentViewId() { return R.layout.activity_settings; }
    int getTitleId() { return R.string.settings; }
}
