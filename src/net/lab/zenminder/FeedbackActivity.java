package net.lab.zenminder;

import android.os.Bundle;
import android.util.Log;

public class FeedbackActivity extends BaseChildActivity 
{
    private static final String TAG = "ZenMinder.FeedbackActivity";

    int getContentViewId() { return R.layout.activity_feedback; }
    int getTitleId() { return R.string.feedback; }
}
