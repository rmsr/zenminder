package net.lab.zenminder;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity
{
    private static final String TAG = "ZenMinder.MainActivity";

    abstract class NavDrawerEntry {
        private String mLabel;

        NavDrawerEntry(int label) {
            mLabel = getResources().getString(label);
        }

        public String getLabel() { return mLabel; }
        public String getTitle() { return mLabel; }

        abstract public boolean onSelected();
    }

    class IntentEntry extends NavDrawerEntry {
        private Intent mIntent;

        IntentEntry(int label, Intent intent) {
            super(label);
            mIntent = intent;
        }

        public boolean onSelected() {
            startActivity(mIntent);
            return false;
        }
    }

    class FragmentEntry<T extends BaseMainFragment> extends NavDrawerEntry {
        private Class<T> mFragmentClass;
        private String mTitle;

        FragmentEntry(int label, Class<T> cls) {
            this(label, label, cls);
        }

        FragmentEntry(int label, int title, Class<T> cls) {
            super(label);
            mTitle = getResources().getString(title);
            mFragmentClass = cls;
        }

        @Override
        public String getTitle() { return mTitle; }

        public boolean onSelected() {
            BaseMainFragment fragment;
            try {
                fragment = mFragmentClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e); // java exceptions are stupidly designed
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            setCurrentFragment(fragment);
            return true;
        }
    }

    // bundle keys
    private static final String KEY_DRAWER_OPEN = "drawer_open";
    private static final String KEY_DRAWER_POSITION = "drawer_position";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private List<NavDrawerEntry> mDrawerEntries;
    private ArrayAdapter<String> mDrawerLabels;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentEntry mCurrentDrawerEntry;
    private int mCurrentDrawerPosition;
    private BaseMainFragment mCurrentFragment;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
        setupDrawer();

        addDrawerEntry(new FragmentEntry(
                    R.string.meditate, R.string.app_name, MeditateFragment.class));
        addDrawerEntry(new FragmentEntry(
                    R.string.history, HistoryFragment.class));
        addDrawerEntry(new FragmentEntry(
                    R.string.reminders, RemindersFragment.class));
        addDrawerEntry(new IntentEntry(
                    R.string.settings, new Intent(this, SettingsActivity.class)));
        addDrawerEntry(new IntentEntry(
                    R.string.donate, new Intent(this, DonateActivity.class)));
        addDrawerEntry(new IntentEntry(
                    R.string.feedback, new Intent(this, FeedbackActivity.class)));
        addDrawerEntry(new IntentEntry(
                    R.string.help, new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getResources().getString(R.string.help_url)))));

        if (bundle == null) {
            onDrawerItemClicked(0);
        } else {
            setCurrentEntry(bundle.getInt(KEY_DRAWER_POSITION));
            if (bundle.getBoolean(KEY_DRAWER_OPEN)) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(KEY_DRAWER_OPEN, mDrawerLayout.isDrawerOpen(mDrawerList));
        bundle.putInt(KEY_DRAWER_POSITION, mCurrentDrawerPosition);
    }

    private void onDrawerItemClicked(int position) {
        mDrawerLayout.closeDrawer(mDrawerList);

        NavDrawerEntry entry = mDrawerEntries.get(position);
        if (entry.onSelected()) {
            setCurrentEntry(position);
            getSupportActionBar().setTitle(entry.getTitle());
            supportInvalidateOptionsMenu();
        }
    }

    private void setupDrawer() {
        mDrawerEntries = new ArrayList<NavDrawerEntry>();
        mDrawerLabels = new ArrayAdapter(this, R.layout.nav_drawer_item);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.nav_drawer_open, R.string.nav_drawer_close) {
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu();
            }
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mCurrentDrawerEntry.getTitle());
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList = (ListView) findViewById(R.id.nav_drawer_list);
        mDrawerList.setAdapter(mDrawerLabels);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDrawerItemClicked(position);
            }
        });
    }

    private void addDrawerEntry(NavDrawerEntry entry) {
        mDrawerEntries.add(entry);
        mDrawerLabels.add(entry.getLabel());
    }

    private void setCurrentEntry(int position) {
        mCurrentDrawerPosition = position;
        mCurrentDrawerEntry = (FragmentEntry) mDrawerEntries.get(position);
        mDrawerList.setItemChecked(position, true);
    }

    private void setCurrentFragment(BaseMainFragment fragment) {
        mCurrentFragment = fragment;
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mDrawerToggle.onConfigurationChanged(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) return true;
        return mCurrentFragment.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;
        return mCurrentFragment.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

}
