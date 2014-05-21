package net.lab.zenminder;

import android.os.Bundle;
import android.util.Log;

public class DonateActivity extends BaseChildActivity
{
    private static final String TAG = "ZenMinder.DonateActivity";

    int getContentViewId() { return R.layout.activity_donate; }
    int getTitleId() { return R.string.donate; }
}
