package net.lab.zenminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

abstract class BaseMainFragment extends Fragment {
    private static final String TAG = "ZenMinder.BaseMainFragment";

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
