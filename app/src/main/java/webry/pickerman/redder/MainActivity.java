package webry.pickerman.redder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.IdRes;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import de.hdodenhof.circleimageview.CircleImageView;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.common.ActivityBase;

public class MainActivity extends ActivityBase {

    private Toolbar mToolbar;
    private NavigationView mNavView;
    private ActionBar mActionBar;
    private CardView mSearchBar;
    private DrawerLayout mDrawerLayout;

    public FloatingActionButton mFabButton;

    private LinearLayout mAppBarMainLayout;

    public Menu mNavMenu;

    private View mNavHeaderLayout;

    private TextView mNavHeaderFullname, mNavHeaderUsername;
    private CircleImageView mNavHeaderPhoto, mNavHeaderIcon;
    private ImageView mNavHeaderCover;

    private Boolean isSearchBarHide = false;

    // used to store app title
    private CharSequence mTitle;

    LinearLayout mContainerAdmob, mSearchContainer;

    EditText mSearchBox;
    ImageButton mSearchClear;

    Fragment fragment;
    Boolean action = false;

    private int pageId = PAGE_FLOW;

    private Boolean restore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_primary);

        // Get intent data

        Intent i = getIntent();

        pageId = i.getIntExtra("pageId", PAGE_FLOW);

        // Initialize Google Admob

        MobileAds.initialize(this, new OnInitializationCompleteListener() {

            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        // Load categories list from server if categories not loaded earlier

        if (App.getInstance().getCategoriesList().size() == 0) {

            App.getInstance().getCategories();
        }

        if (savedInstanceState != null) {

            //Restore the fragment's instance
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

            restore = savedInstanceState.getBoolean("restore");
            mTitle = savedInstanceState.getString("mTitle");

            pageId = savedInstanceState.getInt("pageId");

        } else {

            fragment = new Fragment();

            restore = false;
            mTitle = getString(R.string.app_name);
        }

        if (fragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }

        mFabButton = (FloatingActionButton) findViewById(R.id.fabButton);

        mFabButton.setVisibility(View.GONE);

        mAppBarMainLayout = (LinearLayout) findViewById(R.id.appbar_main_layout);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchBar = (CardView) findViewById(R.id.search_bar);

        // Search Box

        mSearchBox = (EditText) findViewById(R.id.search_box);
        mSearchClear = (ImageButton) findViewById(R.id.search_clear);

        mSearchContainer = (LinearLayout) findViewById(R.id.search_container);
        mSearchContainer.setVisibility(View.GONE);

        initToolbar();
        initDrawerMenu();

        mContainerAdmob = (LinearLayout) findViewById(R.id.container_admob);

        mAppBarMainLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, APP_BAR_WITHOUT_ADMOB_HEIGHT, getResources().getDisplayMetrics());;
        mAppBarMainLayout.requestLayout();

        if (App.getInstance().getAdmob() == ADMOB_ENABLED) {

            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();

            mAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {

                    super.onAdLoaded();

                    mAppBarMainLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, APP_BAR_WITH_ADMOB_HEIGHT, getResources().getDisplayMetrics());;
                    mAppBarMainLayout.requestLayout();

                    mContainerAdmob.setVisibility(View.VISIBLE);

                    Log.e("ADMOB", "onAdLoaded");
                }

//                @Override
//                public void onAdFailedToLoad(int i) {
//
//                    super.onAdFailedToLoad(i);
//
//                    mAppBarMainLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, APP_BAR_WITHOUT_ADMOB_HEIGHT, getResources().getDisplayMetrics());;
//                    mAppBarMainLayout.requestLayout();
//
//                    mContainerAdmob.setVisibility(View.GONE);
//
//                    Log.e("ADMOB", "onAdFailedToLoad");
//                }
            });

            mAdView.loadAd(adRequest);

        } else {

            Log.e("ADMOB", "ADMOB_DISABLED");

            mContainerAdmob.setVisibility(View.GONE);
        }

        if (!restore) {

            switch (pageId) {

                case PAGE_NOTIFICATIONS: {

                    displayFragment(mNavMenu.findItem(R.id.nav_notifications).getItemId(), mNavMenu.findItem(R.id.nav_notifications).getTitle().toString());

                    break;
                }

                case PAGE_MESSAGES: {

                    displayFragment(mNavMenu.findItem(R.id.nav_messages).getItemId(), mNavMenu.findItem(R.id.nav_messages).getTitle().toString());

                    break;
                }

                default: {

                    // Show default section "Flow"

                    displayFragment(mNavMenu.findItem(R.id.nav_flow).getItemId(), mNavMenu.findItem(R.id.nav_flow).getTitle().toString());

                    break;
                }
            }
        }
    }

    private void initToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(mTitle);
    }

    private void initDrawerMenu() {

        mNavView = (NavigationView) findViewById(R.id.nav_view);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.nav_view_open, R.string.nav_view_close) {

            public void onDrawerOpened(View drawerView) {

                refreshMenu();

                hideKeyboard();

                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {

                displayFragment(menuItem.getItemId(), menuItem.getTitle().toString());

                mDrawerLayout.closeDrawers();

                return true;
            }
        });

        mNavMenu = mNavView.getMenu();

        mNavView.setItemIconTintList(getResources().getColorStateList(R.color.nav_state_list));

        mNavHeaderLayout = mNavView.getHeaderView(0);
        mNavHeaderFullname = (TextView) mNavHeaderLayout.findViewById(R.id.userFullname);
        mNavHeaderUsername = (TextView) mNavHeaderLayout.findViewById(R.id.userUsername);

        mNavHeaderPhoto = (CircleImageView) mNavHeaderLayout.findViewById(R.id.userPhoto);
        mNavHeaderIcon = (CircleImageView) mNavHeaderLayout.findViewById(R.id.verified);
        mNavHeaderCover = (ImageView) mNavHeaderLayout.findViewById(R.id.userCover);
    }

    private void refreshMenu() {

        if (App.getInstance().getVerify() == 1) {

            mNavHeaderIcon.setVisibility(View.VISIBLE);

        } else {

            mNavHeaderIcon.setVisibility(View.GONE);
        }

        if (App.getInstance().getId() > 0) {

            updateNavItemCounter(mNavView, R.id.nav_notifications, App.getInstance().getNotificationsCount());
            updateNavItemCounter(mNavView, R.id.nav_messages, App.getInstance().getMessagesCount());

            mNavHeaderCover.setScaleType(ImageView.ScaleType.CENTER_CROP);

            mNavHeaderFullname.setVisibility(View.VISIBLE);
            mNavHeaderUsername.setVisibility(View.VISIBLE);
            mNavHeaderPhoto.setVisibility(View.VISIBLE);

            mNavHeaderFullname.setText(App.getInstance().getFullname());
            mNavHeaderUsername.setText("@" + App.getInstance().getUsername());

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                ImageLoader imageLoader = App.getInstance().getImageLoader();

                imageLoader.get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mNavHeaderPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mNavHeaderPhoto.setImageResource(R.drawable.profile_default_photo);
            }

            if (App.getInstance().getCoverUrl() != null && App.getInstance().getCoverUrl().length() > 0) {

                ImageLoader imageLoader = App.getInstance().getImageLoader();

                imageLoader.get(App.getInstance().getCoverUrl(), ImageLoader.getImageListener(mNavHeaderCover, R.drawable.profile_default_cover, R.drawable.profile_default_cover));

            } else {

                mNavHeaderCover.setImageResource(R.drawable.profile_default_cover);
            }

        } else {

            mNavHeaderFullname.setVisibility(View.GONE);
            mNavHeaderUsername.setVisibility(View.GONE);
            mNavHeaderPhoto.setVisibility(View.GONE);

            mNavHeaderCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mNavHeaderCover.setImageResource(R.drawable.profile_default_cover);
        }
    }

    private void updateNavItemCounter(NavigationView nav, @IdRes int itemId, int count) {

        TextView view = (TextView) nav.getMenu().findItem(itemId).getActionView().findViewById(R.id.counter);
        view.setText(String.valueOf(count));

        if (count <= 0) {

            view.setVisibility(View.GONE);

        } else {

            view.setVisibility(View.VISIBLE);
        }
    }

    public void displayFragment(int id, String title) {

        action = false;

        switch (id) {

            case R.id.nav_flow: {

                fragment = new FlowFragment();
                getSupportActionBar().setTitle(R.string.page_flow);

                action = true;

                break;
            }

            case R.id.nav_search: {

                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                i.putExtra("pageId", PAGE_SEARCH);
                i.putExtra("query", "");
                startActivityForResult(i, 1001);

                break;
            }

            case R.id.nav_favorites: {

                if (App.getInstance().getId() != 0){

                    fragment = new FavoritesFragment();
                    getSupportActionBar().setTitle(R.string.page_5);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", PAGE_FAVORITES);
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case R.id.nav_notifications: {

                if (App.getInstance().getId() != 0){

                    fragment = new NotificationsFragment();
                    getSupportActionBar().setTitle(R.string.page_6);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", PAGE_NOTIFICATIONS);
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case R.id.nav_messages: {

                if (App.getInstance().getId() != 0){

                    fragment = new DialogsFragment();
                    getSupportActionBar().setTitle(R.string.page_7);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", PAGE_MESSAGES);
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            case R.id.nav_profile: {

                if (App.getInstance().getId() != 0){

                    fragment = new ProfileFragment();
                    getSupportActionBar().setTitle(R.string.page_8);

                    action = true;

                } else {

                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.putExtra("pageId", PAGE_PROFILE);
                    startActivityForResult(i, ACTION_LOGIN);
                }

                break;
            }

            default: {

                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);

                break;
            }
        }

        if (action && fragment != null) {

            mFabButton.setVisibility(View.GONE);
            mSearchContainer.setVisibility(View.GONE);

            getSupportActionBar().setDisplayShowCustomEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putString("mTitle", getSupportActionBar().getTitle().toString());

        outState.putInt("pageId", pageId);

        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_LOGIN && resultCode == RESULT_OK && null != data) {

            pageId = data.getIntExtra("pageId", PAGE_UNKNOWN);

            switch (pageId) {

                case PAGE_FAVORITES: {

                    displayFragment(mNavMenu.findItem(R.id.nav_favorites).getItemId(), mNavMenu.findItem(R.id.nav_favorites).getTitle().toString());

                    break;
                }

                case PAGE_NOTIFICATIONS: {

                    displayFragment(mNavMenu.findItem(R.id.nav_notifications).getItemId(), mNavMenu.findItem(R.id.nav_notifications).getTitle().toString());

                    break;
                }

                case PAGE_MESSAGES: {

                    displayFragment(mNavMenu.findItem(R.id.nav_messages).getItemId(), mNavMenu.findItem(R.id.nav_messages).getTitle().toString());

                    break;
                }

                case PAGE_PROFILE: {

                    displayFragment(mNavMenu.findItem(R.id.nav_profile).getItemId(), mNavMenu.findItem(R.id.nav_profile).getTitle().toString());

                    break;
                }

                case PAGE_FLOW: {

                    displayFragment(mNavMenu.findItem(R.id.nav_flow).getItemId(), mNavMenu.findItem(R.id.nav_flow).getTitle().toString());

                    break;
                }

                default: {

                    break;
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();

        refreshMenu();
    }

    @Override
    public void setTitle(CharSequence title) {

        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void hideKeyboard() {

        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            View v = getCurrentFocus();

            if ( v instanceof EditText) {

                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {

                    v.clearFocus();

                    // hideKeyboard();
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public void animateSearchBar(final boolean hide) {

        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;

        isSearchBarHide = hide;

        int moveY = hide ? -(2 * mSearchBar.getHeight()) : 0;
        mSearchBar.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    public void hideAds() {

        if (App.getInstance().getAdmob() == ADMOB_DISABLED) {

            mContainerAdmob.setVisibility(View.GONE);
        }
    }
}
