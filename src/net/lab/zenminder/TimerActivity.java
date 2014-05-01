package net.lab.zenminder;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class TimerActivity extends ActionBarActivity
{
    private static final String TAG = "ZenMinder.TimerActivity";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.timer_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                // TODO: Open settings activity
                return true;
            case R.id.menu_help:
                // TODO: Open help activity
                return true;
            case R.id.menu_about:
                // TODO: Open about activity
                return true;
            case R.id.menu_license:
                // TODO: Open license activity
                return true;
            case R.id.menu_donate:
                // TODO: Open donate activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
