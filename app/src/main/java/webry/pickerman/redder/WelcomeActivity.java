package webry.pickerman.redder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import webry.pickerman.redder.app.App;

import static webry.pickerman.redder.constants.Constants.MY_PERMISSIONS_REQUEST_ACCESS_LOCATION;
import static webry.pickerman.redder.constants.Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout mMarkersLayout;
    private TextView[] markers;
    private int[] screens;
    private Button mButtonSkip, mButtonFinish;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle params = new Bundle();
        params.putString("activity", "WelcomeActivity");
        mFirebaseAnalytics.logEvent("app_open_activity", params);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mMarkersLayout = (LinearLayout) findViewById(R.id.layout_markers);
        mButtonSkip = (Button) findViewById(R.id.button_skip);
        mButtonFinish = (Button) findViewById(R.id.button_next);

        screens = new int[]{
                R.layout.welcome_screen_1,
                R.layout.welcome_screen_2,
                R.layout.welcome_screen_3,
                R.layout.welcome_screen_4};

        addMarkers(0);

        setStatusBarColor(this, 0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        mViewPager.beginFakeDrag();

        mButtonSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Bundle params = new Bundle();
                params.putString("action", "OnClickListener");
                params.putString("result", "-");
                params.putString("type", "SKIP_WELCOME_SCREEN");
                params.putString("activity", "WelcomeActivity");
                mFirebaseAnalytics.logEvent("app_action", params);

                startActivity();
            }
        });

        mButtonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int current = getItem(+1);

                if (current < screens.length) {

                    mViewPager.setCurrentItem(current);

                } else {

                    startActivity();
                }
            }
        });

    }

    public void startActivity() {

        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Granted

                    Bundle params = new Bundle();
                    params.putString("action", "onRequestPermissionsResult");
                    params.putString("result", "Granted");
                    params.putString("type", "WRITE_EXTERNAL_STORAGE");
                    params.putString("activity", "WelcomeActivity");
                    mFirebaseAnalytics.logEvent("app_action", params);

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Denied

                        Bundle params = new Bundle();
                        params.putString("action", "onRequestPermissionsResult");
                        params.putString("result", "Denied");
                        params.putString("type", "WRITE_EXTERNAL_STORAGE");
                        params.putString("activity", "WelcomeActivity");
                        mFirebaseAnalytics.logEvent("app_action", params);
                    }
                }

                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Granted

                    Bundle params = new Bundle();
                    params.putString("action", "onRequestPermissionsResult");
                    params.putString("result", "Granted");
                    params.putString("type", "ACCESS_LOCATION");
                    params.putString("activity", "WelcomeActivity");
                    mFirebaseAnalytics.logEvent("app_action", params);

                    // Check GPS is enabled
                    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    Log.d("GPS", "WelcomeActivity onComplete" + Double.toString(mLastLocation.getLatitude()));
                                    Log.d("GPS", "WelcomeActivity onComplete" + Double.toString(mLastLocation.getLongitude()));

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                } else {

                                    Log.d("GPS", "WelcomeActivity getLastLocation:exception", task.getException());
                                }
                            }
                        });
                    }

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Denied

                        Bundle params = new Bundle();
                        params.putString("action", "onRequestPermissionsResult");
                        params.putString("result", "Denied");
                        params.putString("type", "ACCESS_LOCATION");
                        params.putString("activity", "WelcomeActivity");
                        mFirebaseAnalytics.logEvent("app_action", params);
                    }
                }

                return;
            }

        }
    }

    public int getColorWrapper(Context context, int id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return context.getColor(id);

        } else {

            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    public void setStatusBarColor(Activity act, int index) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            switch (index) {

                case 0: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.bg_welcome_screen_1));

                    break;
                }

                case 1: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.bg_welcome_screen_2));

                    break;
                }

                case 2: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.bg_welcome_screen_3));

                    break;
                }

                case 3: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.bg_welcome_screen_4));

                    break;
                }

                default: {

                    window.setStatusBarColor(Color.TRANSPARENT);

                    break;
                }
            }
        }
    }

    private void addMarkers(int currentPage) {

        markers = new TextView[screens.length];

        mMarkersLayout.removeAllViews();

        for (int i = 0; i < markers.length; i++) {

            markers[i] = new TextView(this);
            markers[i].setText(Html.fromHtml("&#8226;"));
            markers[i].setTextSize(35);
            markers[i].setTextColor(getResources().getColor(R.color.overlay_dark_2));
            mMarkersLayout.addView(markers[i]);
        }

        if (markers.length > 0)

            markers[currentPage].setTextColor(getResources().getColor(R.color.overlay_white));
    }

    private int getItem(int i) {

        return mViewPager.getCurrentItem() + i;
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            addMarkers(position);

            if (position == screens.length - 1) {

                mButtonFinish.setText(getString(R.string.action_finish));
                mButtonSkip.setVisibility(View.GONE);

            } else {

                mButtonFinish.setText(getString(R.string.action_next));
                mButtonSkip.setVisibility(View.VISIBLE);
            }

            switch (position) {

                case 0: {

                    setStatusBarColor(WelcomeActivity.this, 0);

                    break;
                }

                case 1: {

                    setStatusBarColor(WelcomeActivity.this, 1);

                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);

                        } else {

                            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO);
                        }
                    }

                    break;
                }

                case 2: {

                    setStatusBarColor(WelcomeActivity.this, 2);

                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)){

                            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                        } else {

                            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                        }

                    }

                    break;
                }

                case 3: {

                    setStatusBarColor(WelcomeActivity.this, 3);

                    break;
                }

                default: {

                    break;
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(screens[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {

            return screens.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            View view = (View) object;
            container.removeView(view);
        }
    }
}
