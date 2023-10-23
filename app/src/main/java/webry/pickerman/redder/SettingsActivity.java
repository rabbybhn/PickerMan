package webry.pickerman.redder;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import android.view.MenuItem;

import webry.pickerman.redder.common.ActivityBase;


public class SettingsActivity extends ActivityBase {

    Toolbar mToolbar;

    PreferenceFragmentCompat fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {

            fragment = (PreferenceFragmentCompat) getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

        } else {

            fragment = new SettingsFragment();
            getSupportActionBar().setTitle(R.string.title_activity_settings);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.\

        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }
}
