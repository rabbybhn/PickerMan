package webry.pickerman.redder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.common.ActivityBase;
import webry.pickerman.redder.util.CustomRequest;

public class AppActivity extends ActivityBase {

    Button mStartBtn, mLanguageBtn;

    ProgressBar mProgressBar;

    Boolean restore = false;
    Boolean loading = false;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app);

        FacebookSdk.sdkInitialize(getApplicationContext());

        // Load currency list from server if currency list not loaded earlier

        if (App.getInstance().getCurrencyList().size() == 0) {

            App.getInstance().getCurrencies();
        }

        // Get Firebase token


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {

            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (!task.isSuccessful()) {

                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                App.getInstance().set_fcm_token(token);

                // Send token to server if this first app run

                if (App.getInstance().getFirstRun() == 1) {

                    App.getInstance().update_fcm_token();
                }

                Log.d("FCM Token", token);
            }
        });


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful() && task.getResult() != null) {

                        mLastLocation = task.getResult();

                        Log.d("GPS", "AppActivity onComplete" + Double.toString(mLastLocation.getLatitude()));
                        Log.d("GPS", "AppActivity onComplete" + Double.toString(mLastLocation.getLongitude()));

                        // Set geo data to App class

                        App.getInstance().setLat(mLastLocation.getLatitude());
                        App.getInstance().setLng(mLastLocation.getLongitude());

                    } else {

                        Log.d("GPS", "AppActivity getLastLocation:exception", task.getException());
                    }
                }
            });
        }

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");

        } else {

            restore = false;
            loading = false;
        }

        // Night mode

        if (App.getInstance().getNightMode() == 1) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //

        mStartBtn = (Button) findViewById(R.id.startBtn);
        mLanguageBtn = (Button) findViewById(R.id.languageBtn);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mLanguageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> language_names = new ArrayList<String>();

                Resources r = getResources();
                Configuration c = r.getConfiguration();

                for (int i = 0; i < App.getInstance().getLanguages().size(); i++) {

                    language_names.add(App.getInstance().getLanguages().get(i).get("lang_name"));
                }

                AlertDialog.Builder b = new AlertDialog.Builder(AppActivity.this);
                b.setTitle(getText(R.string.title_select_language));

                b.setItems(language_names.toArray(new CharSequence[language_names.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        App.getInstance().setLanguage(App.getInstance().getLanguages().get(which).get("lang_id"));

                        App.getInstance().saveData();

                        // Set App Language

                        App.getInstance().setLocale(App.getInstance().getLanguage());

                        setLanguageBtnTitle();
                    }
                });

                b.setNegativeButton(getText(R.string.action_cancel), null);

                AlertDialog d = b.create();
                d.show();
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity();
            }
        });

        showContentScreen();

        if (loading) showLoadingScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (App.getInstance().getFirstRun() == 1) {

            showContentScreen();

        } else {

            if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

                if (!loading) {

                    showLoadingScreen();

                    loading = true;

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    if (App.getInstance().authorize(response)) {

                                        // If account not enabled

                                        if (App.getInstance().getState() != ACCOUNT_STATE_ENABLED) {

                                            // Clear settings
                                            // Remove data to avoid unnecessary follow-up requests

                                            App.getInstance().removeData();
                                            App.getInstance().readData();
                                        }

                                    } else {

                                        // if authorize error

                                        // Clear settings
                                        // Remove data to avoid unnecessary follow-up requests

                                        App.getInstance().removeData();
                                        App.getInstance().readData();
                                    }

                                    loading = false;

                                    startActivity();

                                    Log.d("AppActivity authorize", response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Log.e("AppActivity authorize", error.toString());

                            loading = false;

                            startActivity();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {

                            Map<String, String> params = new HashMap<String, String>();

                            params.put("clientId", CLIENT_ID);
                            params.put("appType", Integer.toString(APP_TYPE_ANDROID));

                            params.put("accountId", Long.toString(App.getInstance().getId()));
                            params.put("accessToken", App.getInstance().getAccessToken());

                            params.put("gcm_regId", App.getInstance().get_fcm_token()); // For old server engine
                            params.put("fcm_regId", App.getInstance().get_fcm_token()); // For new server engine

                            return params;
                        }
                    };

                    RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                    jsonReq.setRetryPolicy(policy);

                    App.getInstance().addToRequestQueue(jsonReq);
                }

            } else {

                startActivity();
            }
        }
    }

    public void setLanguageBtnTitle() {

        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(new Locale(App.getInstance().getLanguage()));

        mLanguageBtn.setText(createConfigurationContext(config).getText(R.string.settings_language_label).toString() + ": " + App.getInstance().getLanguageNameByCode(App.getInstance().getLanguage()));

        mStartBtn.setText(createConfigurationContext(config).getText(R.string.action_continue).toString());
    }

    public void startActivity() {

        if (App.getInstance().getFirstRun() == 1) {

            App.getInstance().setFirstRun(0);
            App.getInstance().saveData();
        }

        Intent intent = new Intent(AppActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void showContentScreen() {

        setLanguageBtnTitle();

        mLanguageBtn.setVisibility(View.VISIBLE);
        mStartBtn.setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.GONE);
    }

    public void showLoadingScreen() {

        mLanguageBtn.setVisibility(View.GONE);
        mStartBtn.setVisibility(View.GONE);

        mProgressBar.setVisibility(View.VISIBLE);
    }
}
