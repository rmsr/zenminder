package net.lab.zenminder;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

abstract public class BaseChildActivity extends ActionBarActivity
{
    protected int mContentViewId;
    protected int mTitleId;

    abstract int getContentViewId();
    abstract int getTitleId();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(getContentViewId());

        ActionBar bar = getSupportActionBar();
        bar.setTitle(getTitleId());
        // these seem redundant...?
        //bar.setDisplayHomeAsUpEnabled(true);
        //bar.setHomeButtonEnabled(true);
    }
}
