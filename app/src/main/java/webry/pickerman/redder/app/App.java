package webry.pickerman.redder.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import webry.pickerman.redder.R;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Category;
import webry.pickerman.redder.model.Currency;
import webry.pickerman.redder.model.FlowFilters;
import webry.pickerman.redder.model.SearchFilters;
import webry.pickerman.redder.model.Tooltips;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.LruBitmapCache;

public class App extends MultiDexApplication implements Constants {

    private static final String TAG = App.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST = 1;

    private SearchFilters mSearchFilters;
    private FlowFilters mFlowFilters;

    private Tooltips mTooltips;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private static App mInstance;

    private SharedPreferences sharedPref;

    private ArrayList<Currency> currencies;

    private ArrayList<Category> categories;

    private List<Map<String, String>> languages = new ArrayList<>();

    private String new_item_phone = "", new_item_city = "", new_item_country = "";
    private Double new_item_lat = 0.000000, new_item_lng = 0.000000;

    private String language = "";
    private String username, fullname, accessToken, fcm_token = "", fb_id = "", gl_id = "", photoUrl = "", coverUrl = "", area = "", country = "", city = "", phone = "";
    private Double lat = 0.000000, lng = 0.000000;
    private long id;
    private int first_run = 1, distance = 50, popular = 0, state, admob = 1, verify, balance, allowMessages, allowReviewsGCM, allowCommentsGCM, allowMessagesGCM, allowCommentReplyGCM, errorCode, currentChatId = 0;
    private int notificationsCount = 0, messagesCount = 0;
    private int lastNotifyView = 0;
    private int nightMode = 0;

	@Override
	public void onCreate() {

		super.onCreate();

        mInstance = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        sharedPref = this.getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE);

        // Read app saved settings
        this.readData();

        // Get app languages

        initLanguages();

        // Set App language by locale

        setLocale(getLanguage());

        // Get Categories From server

        categories = new ArrayList<Category>();

        // Get Currency list From server

        currencies = new ArrayList<Currency>();

        // Get Search Filters

        mSearchFilters = new SearchFilters();
        this.readSearchFilters();

        // Get Flow Filters

        mFlowFilters = new FlowFilters();
        this.readFlowFilters();

        // Get Tooltips settings

        mTooltips = new Tooltips();
        this.readTooltipsSettings();
	}
	
	private void initLanguages() {

        Map<String, String> map = new HashMap<String, String>();

        map.put("lang_id", "");
        map.put("lang_name", getString(R.string.language_default));

	    this.languages.add(map);

        DisplayMetrics metrics = new DisplayMetrics();

        Resources r = getResources();
        Configuration c = r.getConfiguration();
        String[] loc = r.getAssets().getLocales();

        for (String s : loc) {

            String sz_lang_id = "id"; // id and in the same for indonesian language. id must be deleted from list

            c.locale = new Locale(s);
            Resources res = new Resources(getAssets(), metrics, c);
            String s1 = res.getString(R.string.app_lang_code);

            String language = c.locale.getDisplayLanguage();

            c.locale = new Locale("");
            Resources res2 = new Resources(getAssets(), metrics, c);
            String s2 = res2.getString(R.string.app_lang_code);

            if (!s1.equals(s2) && !s.equals(sz_lang_id)) {

                map = new HashMap<String, String>();

                map.put("lang_id", s);
                map.put("lang_name", language);

                this.languages.add(map);
            }
        }
    }

    public List<Map<String, String>> getLanguages() {

	    return this.languages;
    }

    public void setLocale(String lang) {

        Locale myLocale;

        if (lang.length() == 0) {

            myLocale = new Locale("");

        } else {

            myLocale = new Locale(lang);
        }

        Resources res = getBaseContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = new Configuration();

        conf.setLocale(myLocale);
        conf.setLayoutDirection(myLocale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            getApplicationContext().createConfigurationContext(conf);

        } else {

            res.updateConfiguration(conf, dm);
        }
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    public String getLanguage() {

        if (this.language == null) {

            this.setLanguage("");
        }

        return this.language;
    }

    public String getLanguageNameByCode(String langCode) {

        String language = getString(R.string.language_default);

        for (int i = 1; i < App.getInstance().getLanguages().size(); i++) {

            if (App.getInstance().getLanguages().get(i).get("lang_id").equals(langCode)) {

                language = App.getInstance().getLanguages().get(i).get("lang_name");
            }
        }

        return language;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        setLocale(getLanguage());
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        
        MultiDex.install(this);
    }

    public void updateLocation() {

        if (App.getInstance().isConnected()) {

            if (App.getInstance().getId() != 0) {

                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_GEO_LOCATION, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                try {

                                    // check to error
                                    if (!response.getBoolean("error")) {

                                        // Code here if need
                                    }

                                } catch (JSONException e) {

                                    e.printStackTrace();

                                } finally {

                                    Log.d("GEO SAVE", response.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e(TAG, "ERROR SAVE GEO LOCATION DATA TO SERVER");
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("accountId", Long.toString(App.getInstance().getId()));
                        params.put("accessToken", App.getInstance().getAccessToken());
                        params.put("lat", Double.toString(App.getInstance().getLat()));
                        params.put("lng", Double.toString(App.getInstance().getLng()));

                        return params;
                    }
                };

                App.getInstance().addToRequestQueue(jsonReq);
            }
        }
    }
    
    public boolean isConnected() {
    	
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	NetworkInfo netInfo = cm.getActiveNetworkInfo();
    	
    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    		
    		return true;
    	}
    	
    	return false;
    }

    public void getCurrencies() {

        if (App.getInstance().isConnected()) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GET_CURRENCIES, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("items")) {

                                        JSONArray itemsArray = response.getJSONArray("items");

                                        if (itemsArray.length() > 0) {

                                            currencies.clear();

                                            for (int i = 0; i < itemsArray.length(); i++) {

                                                JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                                Currency c = new Currency(itemObj);

                                                currencies.add(c);
                                            }
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                Log.d("App getCurrencies", "Loaded");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("App getCurrencies", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("lang", App.getInstance().getLanguage());

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void getCategories() {

        if (App.getInstance().isConnected()) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GET_CATEGORIES, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("items")) {

                                        JSONArray itemsArray = response.getJSONArray("items");

                                        if (itemsArray.length() > 0) {

                                            categories.clear();

                                            for (int i = 0; i < itemsArray.length(); i++) {

                                                JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                                Category c = new Category(itemObj);

                                                categories.add(c);
                                            }
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                Log.d("App getCategories", "Loaded");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("App getCategories", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("lang", App.getInstance().getLanguage());

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void getCounters() {

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GET_SETTINGS, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (!response.getBoolean("error")) {

                                    if (response.has("messagesCount")) {

                                        App.getInstance().setMessagesCount(response.getInt("messagesCount"));
                                    }

                                    if (response.has("notificationsCount")) {

                                        App.getInstance().setNotificationsCount(response.getInt("notificationsCount"));
                                    }
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                saveData();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("APP getCounters", error.toString());
                }
            }) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();

                    params.put("clientId", CLIENT_ID);

                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("lastNotifyView", Integer.toString(App.getInstance().getLastNotifyView()));

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public Boolean authorize(JSONObject authObj) {

        try {

            // Get Error Code

            if (authObj.has("error_code")) {

                this.setErrorCode(authObj.getInt("error_code"));
            }

            // If error true

            if (authObj.getBoolean("error")) {

                return false;
            }

            // If response not have account data

            if (!authObj.has("account")) {

                return false;
            }

            // Read account data

            JSONArray accountArray = authObj.getJSONArray("account");

            if (accountArray.length() > 0) {

                JSONObject accountObj = (JSONObject) accountArray.get(0);

                if (accountObj.has("state")) {

                    this.setState(accountObj.getInt("state"));

                } else {

                    return false;
                }

                // if account is active

                if (this.getState() == ACCOUNT_STATE_ENABLED) {

                    this.setPhone(accountObj.getString("phone"));

                    this.setUsername(accountObj.getString("username"));
                    this.setFullname(accountObj.getString("fullname"));
                    this.setAdmob(accountObj.getInt("admob"));
                    this.setVerify(accountObj.getInt("verify"));
                    this.setBalance(accountObj.getInt("balance"));
                    this.setFacebookId(accountObj.getString("fb_id"));
                    this.setGoogleId(accountObj.getString("gl_id"));
                    this.setAllowMessages(accountObj.getInt("allowMessages"));

                    this.setPhotoUrl(accountObj.getString("lowPhotoUrl"));
                    this.setCoverUrl(accountObj.getString("coverUrl"));

                    this.setLastNotifyView(accountObj.getInt("lastNotifyView"));

                    if (App.getInstance().getLat() == 0.000000 && App.getInstance().getLng() == 0.000000) {

                        this.setLat(accountObj.getDouble("lat"));
                        this.setLng(accountObj.getDouble("lng"));
                    }

                    this.setId(authObj.getLong("accountId"));
                    this.setAccessToken(authObj.getString("accessToken"));

                    this.saveData();

                    // Get counters values
                    this.getCounters();
                }
            }

            return true;

        } catch (JSONException e) {

            e.printStackTrace();

            return false;
        }
    }

    public void setDistance(int distance) {

        this.distance = distance;
    }

    public int getDistance() {

        return this.distance;
    }

    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    // Save FCM Token to Server

    public void update_fcm_token() {

	    if (App.getInstance().get_fcm_token().length() > 0) {

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_FCM_TOKEN, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                // if error

                                if (response.getBoolean("error")) {

                                    Log.e("APP update_fcm_token", "ERROR");
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {


                    Log.e("APP update_fcm_token", "ERROR");
                }
            }) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();

                    params.put("clientId", CLIENT_ID);
                    params.put("appType", Integer.toString(APP_TYPE_ANDROID));

                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("fcm_regId", App.getInstance().get_fcm_token());
                    params.put("lang", App.getInstance().getLanguage());

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void set_fcm_token(final String fcm_token) {

        this.fcm_token = fcm_token;
    }

    public String get_fcm_token() {

        if (this.fcm_token == null) {

            this.fcm_token = "";
        }

        return this.fcm_token;
    }

    public void setFacebookId(String fb_id) {

        this.fb_id = fb_id;
    }

    public String getFacebookId() {

        return this.fb_id;
    }

    public void setGoogleId(String gl_id) {

        this.gl_id = gl_id;
    }

    public String getGoogleId() {

        return this.gl_id;
    }

    public void setState(int state) {

        this.state = state;
    }

    public int getState() {

        return this.state;
    }

    public void setFirstRun(int first_run) {

        this.first_run = first_run;
    }

    public int getFirstRun() {

        return this.first_run;
    }

    public void setNotificationsCount(int notificationsCount) {

        this.notificationsCount = notificationsCount;
    }

    public int getNotificationsCount() {

        return this.notificationsCount;
    }

    public void setMessagesCount(int messagesCount) {

        this.messagesCount = messagesCount;
    }

    public int getMessagesCount() {

        return this.messagesCount;
    }

    public void setAllowMessagesGCM(int allowMessagesGCM) {

        this.allowMessagesGCM = allowMessagesGCM;
    }

    public int getAllowMessagesGCM() {

        return this.allowMessagesGCM;
    }

    public void setAllowCommentReplyGCM(int allowCommentReplyGCM) {

        this.allowCommentReplyGCM = allowCommentReplyGCM;
    }

    public int getAllowCommentReplyGCM() {

        return this.allowCommentReplyGCM;
    }

    public void setAllowCommentsGCM(int allowCommentsGCM) {

        this.allowCommentsGCM = allowCommentsGCM;
    }

    public int getAllowCommentsGCM() {

        return this.allowCommentsGCM;
    }

    public void setAllowReviewsGCM(int allowReviewsGCM) {

        this.allowReviewsGCM = allowReviewsGCM;
    }

    public int getAllowReviewsGCM() {

        return this.allowReviewsGCM;
    }

    public void setAllowMessages(int allowMessages) {

        this.allowMessages = allowMessages;
    }

    public int getAllowMessages() {

        return this.allowMessages;
    }

    public void setAdmob(int admob) {

        this.admob = admob;
    }

    public int getAdmob() {

        return this.admob;
    }

    public void setCurrentChatId(int currentChatId) {

        this.currentChatId = currentChatId;
    }

    public int getCurrentChatId() {

        return this.currentChatId;
    }

    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;
    }

    public int getErrorCode() {

        return this.errorCode;
    }

    public String getUsername() {

        if (this.username == null) {

            this.username = "";
        }

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPhone() {

        if (this.phone == null) {

            this.phone = "";
        }

        return this.phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getAccessToken() {

        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {

        this.accessToken = accessToken;
    }

    public void setFullname(String fullname) {

        this.fullname = fullname;
    }

    public String getFullname() {

        if (this.fullname == null) {

            this.fullname = "";
        }

        return this.fullname;
    }

    public void setVerify(int verify) {

        this.verify = verify;
    }

    public int getVerify() {

        return this.verify;
    }

    public void setBalance(int balance) {

        this.balance = balance;
    }

    public int getBalance() {

        return this.balance;
    }

    public void setLastNotifyView(int lastNotifyView) {

        this.lastNotifyView = lastNotifyView;
    }

    public int getLastNotifyView() {

        return this.lastNotifyView;
    }

    public void setPhotoUrl(String photoUrl) {

        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {

        if (this.photoUrl == null) {

            this.photoUrl = "";
        }

        return this.photoUrl;
    }

    public void setCoverUrl(String coverUrl) {

        this.coverUrl = coverUrl;
    }

    public String getCoverUrl() {

        if (coverUrl == null) {

            this.coverUrl = "";
        }

        return this.coverUrl;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCountry() {

        if (this.country == null) {

            this.setCountry("");
        }

        return this.country;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCity() {

        if (this.city == null) {

            this.setCity("");
        }

        return this.city;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getArea() {

        if (this.area == null) {

            this.setArea("");
        }

        return this.area;
    }

    public void setLat(Double lat) {

        this.lat = lat;
    }

    public Double getLat() {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        return this.lat;
    }

    public void setLng(Double lng) {

        this.lng = lng;
    }

    public Double getLng() {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        return this.lng;
    }

    // New Item settings

    public void setNewItemLat(Double lat) {

        this.new_item_lat = lat;
    }

    public Double getNewItemLat() {

        if (this.new_item_lat == null) {

            this.new_item_lat = 0.000000;
        }

        return this.new_item_lat;
    }

    public void setNewItemLng(Double lng) {

        this.new_item_lng = lng;
    }

    public Double getNewItemLng() {

        if (this.new_item_lng == null) {

            this.new_item_lng = 0.000000;
        }

        return this.new_item_lng;
    }

    public void setNewItemCountry(String country) {

        this.new_item_country = country;
    }

    public String getNewItemCountry() {

        if (this.new_item_country == null) {

            this.setNewItemCountry("");
        }

        return this.new_item_country;
    }

    public void setNewItemCity(String city) {

        this.new_item_city = city;
    }

    public String getNewItemCity() {

        if (this.new_item_city == null) {

            this.setNewItemCity("");
        }

        return this.new_item_city;
    }

    public void setNewItemPhone(String phone) {

        this.new_item_phone = phone;
    }

    public String getNewItemPhone() {

        if (this.new_item_phone == null) {

            this.setNewItemPhone("");
        }

        return this.new_item_phone;
    }

    //

    public void setNightMode(int nightMode) {

        this.nightMode = nightMode;
    }

    public int getNightMode() {

        return this.nightMode;
    }

    // End New Item settings

    public SharedPreferences getSharedPref() {

	    return this.sharedPref;
    }

    public SearchFilters getSearchFilters() {

        return this.mSearchFilters;
    }

    public FlowFilters getFlowFilters() {

        return this.mFlowFilters;
    }

    public Tooltips getTooltipsSettings() {

        return this.mTooltips;
    }

    public ArrayList<Currency> getCurrencyList() {

        if (this.currencies == null) {

            this.currencies = new ArrayList<Currency>();
        }

        return this.currencies;
    }

    public void clearCategoriesList() {

	    this.categories.clear();
    }

    public ArrayList<Category> getCategoriesList() {

        if (this.categories == null) {

            this.categories = new ArrayList<Category>();
        }

	    ArrayList<Category> list = new ArrayList<Category>();

        for (int i = 0; i < categories.size(); i++) {

            Category item = categories.get(i);

            if (item.getMainCategoryId() == 0) {

                list.add(categories.get(i));
            }
        }

        return list;
    }

    public ArrayList<Category> getSubcategoriesList(int categoryId) {

        if (this.categories == null) {

            this.categories = new ArrayList<Category>();
        }

        ArrayList<Category> list = new ArrayList<Category>();

        for (int i = 0; i < categories.size(); i++) {

            Category item = categories.get(i);

            if (item.getMainCategoryId() == categoryId) {

                list.add(categories.get(i));
            }
        }

        return list;
    }

    public void readTooltipsSettings() {

        this.mTooltips.setShowFlowTooltip(sharedPref.getBoolean(getString(R.string.settings_tooltips_flow), true));
        this.mTooltips.setShowSelectLocationTooltip(sharedPref.getBoolean(getString(R.string.settings_tooltips_select_location), true));
        this.mTooltips.setShowSelectLocationPromoTooltip(sharedPref.getBoolean(getString(R.string.settings_tooltips_select_location_promo), true));
    }

    public void saveTooltipsSettings() {

        sharedPref.edit().putBoolean(getString(R.string.settings_tooltips_flow), this.mTooltips.isAllowShowFlowTooltip()).apply();
        sharedPref.edit().putBoolean(getString(R.string.settings_tooltips_select_location), this.mTooltips.isAllowShowSelectLocationTooltip()).apply();
        sharedPref.edit().putBoolean(getString(R.string.settings_tooltips_select_location_promo), this.mTooltips.isAllowShowSelectLocationPromoTooltip()).apply();
    }

    public void readFlowFilters() {

        this.mFlowFilters.setLat(Double.parseDouble(sharedPref.getString(getString(R.string.settings_flow_filters_lat), "0.000000")));
        this.mFlowFilters.setLng(Double.parseDouble(sharedPref.getString(getString(R.string.settings_flow_filters_lng), "0.000000")));

        this.mFlowFilters.setCategoryId(sharedPref.getInt(getString(R.string.settings_flow_filters_category_id), 0));
        this.mFlowFilters.setCurrency(sharedPref.getInt(getString(R.string.settings_flow_filters_currency), 0));
        this.mFlowFilters.setDistance(sharedPref.getInt(getString(R.string.settings_flow_filters_distance), 25));

        this.mFlowFilters.setLocation(sharedPref.getString(getString(R.string.settings_flow_filters_location), ""));

        this.mFlowFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_flow_filters_moderation_type), 0));

        if (App.getInstance().getFirstRun() == 1) {

            if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT) {

                this.mFlowFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_flow_filters_moderation_type), 1));

            } else {

                this.mFlowFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_flow_filters_moderation_type), 0));
            }

            this.saveFlowFilters();
        }
    }

    public void saveFlowFilters() {

        sharedPref.edit().putString(getString(R.string.settings_flow_filters_lat), Double.toString(this.mFlowFilters.getLat())).apply();
        sharedPref.edit().putString(getString(R.string.settings_flow_filters_lng), Double.toString(this.mFlowFilters.getLng())).apply();

        sharedPref.edit().putInt(getString(R.string.settings_flow_filters_category_id), this.mFlowFilters.getCategoryId()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_flow_filters_currency), this.mFlowFilters.getCurrency()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_flow_filters_distance), this.mFlowFilters.getDistance()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_flow_filters_moderation_type), this.mFlowFilters.getModerationType()).apply();

        sharedPref.edit().putString(getString(R.string.settings_flow_filters_location), this.mFlowFilters.getLocation()).apply();
    }

    public void readSearchFilters() {

        this.mSearchFilters.setLat(Double.parseDouble(sharedPref.getString(getString(R.string.settings_search_filters_lat), "0.000000")));
        this.mSearchFilters.setLng(Double.parseDouble(sharedPref.getString(getString(R.string.settings_search_filters_lng), "0.000000")));

        this.mSearchFilters.setSortType(sharedPref.getInt(getString(R.string.settings_search_filters_sort_type), 0));
        this.mSearchFilters.setCategoryId(sharedPref.getInt(getString(R.string.settings_search_filters_category_id), 0));
        this.mSearchFilters.setDistance(sharedPref.getInt(getString(R.string.settings_search_filters_distance), 25));

        this.mSearchFilters.setLocation(sharedPref.getString(getString(R.string.settings_search_filters_location), ""));

        this.mSearchFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_search_filters_moderation_type), 0));

        if (App.getInstance().getFirstRun() == 1) {

            if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT) {

                this.mSearchFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_search_filters_moderation_type), 1));

            } else {

                this.mSearchFilters.setModerationType(sharedPref.getInt(getString(R.string.settings_search_filters_moderation_type), 0));
            }

            this.saveSearchFilters();
        }
    }

    public void saveSearchFilters() {

        sharedPref.edit().putString(getString(R.string.settings_search_filters_lat), Double.toString(this.mSearchFilters.getLat())).apply();
        sharedPref.edit().putString(getString(R.string.settings_search_filters_lng), Double.toString(this.mSearchFilters.getLng())).apply();

        sharedPref.edit().putInt(getString(R.string.settings_search_filters_sort_type), this.mSearchFilters.getSortType()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_category_id), this.mSearchFilters.getCategoryId()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_moderation_type), this.mSearchFilters.getModerationType()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_distance), this.mSearchFilters.getDistance()).apply();

        sharedPref.edit().putString(getString(R.string.settings_search_filters_location), this.mSearchFilters.getLocation()).apply();
    }

    public void readData() {

        this.setNightMode(sharedPref.getInt(getString(R.string.settings_night_mode), 0));

        this.setId(sharedPref.getLong(getString(R.string.settings_account_id), 0));
        this.setUsername(sharedPref.getString(getString(R.string.settings_account_username), ""));
        this.setAccessToken(sharedPref.getString(getString(R.string.settings_account_access_token), ""));

        this.setAllowReviewsGCM(sharedPref.getInt(getString(R.string.settings_account_allow_reviews_gcm), 1));
        this.setAllowMessagesGCM(sharedPref.getInt(getString(R.string.settings_account_allow_messages_gcm), 1));
        this.setAllowCommentsGCM(sharedPref.getInt(getString(R.string.settings_account_allow_comments_gcm), 1));
        this.setAllowCommentReplyGCM(sharedPref.getInt(getString(R.string.settings_account_allow_comments_reply_gcm), 1));

        this.setNotificationsCount(sharedPref.getInt(getString(R.string.settings_account_notifications_count), 0));
        this.setMessagesCount(sharedPref.getInt(getString(R.string.settings_account_messages_count), 0));

        this.setFullname(sharedPref.getString(getString(R.string.settings_account_fullname), ""));
        this.setPhotoUrl(sharedPref.getString(getString(R.string.settings_account_photo_url), ""));
        this.setCoverUrl(sharedPref.getString(getString(R.string.settings_account_cover_url), ""));

        this.setLat(Double.parseDouble(sharedPref.getString(getString(R.string.settings_account_lat), "0.000000")));
        this.setLng(Double.parseDouble(sharedPref.getString(getString(R.string.settings_account_lng), "0.000000")));

        this.setFirstRun(sharedPref.getInt(getString(R.string.settings_first_run), 1));

        this.setLanguage(sharedPref.getString(getString(R.string.settings_language), ""));

        // For New Item

        this.setNewItemLat(Double.parseDouble(sharedPref.getString(getString(R.string.settings_new_item_lat), "0.000000")));
        this.setNewItemLng(Double.parseDouble(sharedPref.getString(getString(R.string.settings_new_item_lng), "0.000000")));

        this.setNewItemCity(sharedPref.getString(getString(R.string.settings_new_item_city), ""));
        this.setNewItemCountry(sharedPref.getString(getString(R.string.settings_new_item_country), ""));
        this.setNewItemPhone(sharedPref.getString(getString(R.string.settings_new_item_phone), ""));

    }

    public void saveData() {

        sharedPref.edit().putInt(getString(R.string.settings_night_mode), this.getNightMode()).apply();

        sharedPref.edit().putLong(getString(R.string.settings_account_id), this.getId()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), this.getUsername()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), this.getAccessToken()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_account_allow_reviews_gcm), this.getAllowReviewsGCM()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_messages_gcm), this.getAllowMessagesGCM()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_comments_gcm), this.getAllowCommentsGCM()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_allow_comments_reply_gcm), this.getAllowCommentReplyGCM()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_account_notifications_count), this.getNotificationsCount()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_account_messages_count), this.getMessagesCount()).apply();

        sharedPref.edit().putString(getString(R.string.settings_account_fullname), this.getFullname()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_photo_url), this.getPhotoUrl()).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_cover_url), this.getCoverUrl()).apply();

        sharedPref.edit().putString(getString(R.string.settings_account_lat), Double.toString(this.getLat())).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_lng), Double.toString(this.getLng())).apply();

        sharedPref.edit().putInt(getString(R.string.settings_first_run), this.getFirstRun()).apply();

        sharedPref.edit().putString(getString(R.string.settings_language), this.getLanguage()).apply();

        // For New Item

        sharedPref.edit().putString(getString(R.string.settings_new_item_lat), Double.toString(this.getNewItemLat())).apply();
        sharedPref.edit().putString(getString(R.string.settings_new_item_lng), Double.toString(this.getNewItemLng())).apply();

        sharedPref.edit().putString(getString(R.string.settings_new_item_city), this.getNewItemCity()).apply();
        sharedPref.edit().putString(getString(R.string.settings_new_item_country), this.getNewItemCountry()).apply();
        sharedPref.edit().putString(getString(R.string.settings_new_item_phone), this.getNewItemPhone()).apply();
    }

    public void removeData() {

        sharedPref.edit().putLong(getString(R.string.settings_account_id), 0).apply();
        sharedPref.edit().putString(getString(R.string.settings_account_username), "").apply();
        sharedPref.edit().putString(getString(R.string.settings_account_access_token), "").apply();


        // Delete language settings
        // sharedPref.edit().putString(getString(R.string.settings_language), "").apply();

        // Delete Search Filters

        sharedPref.edit().putString(getString(R.string.settings_search_filters_lat), "0.000000").apply();
        sharedPref.edit().putString(getString(R.string.settings_search_filters_lng), "0.000000").apply();

        sharedPref.edit().putInt(getString(R.string.settings_search_filters_sort_type), 0).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_category_id), 0).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_moderation_type), 0).apply();
        sharedPref.edit().putInt(getString(R.string.settings_search_filters_distance), 25).apply();

        sharedPref.edit().putString(getString(R.string.settings_search_filters_location), "").apply();
    }

    public static synchronized App getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {

		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue,
					new LruBitmapCache());
		}
		return this.mImageLoader;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
}