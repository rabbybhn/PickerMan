package webry.pickerman.redder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import github.ankushsachdeva.emojicon.EmojiconTextView;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.util.Api;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;
import webry.pickerman.redder.adapter.ImagesAdapter;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.ImageItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ViewItemFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final int PROFILE_CHAT = 7;

    private ProgressDialog pDialog;

    ArrayList<ImageItem> images;

    private Toolbar mToolbar;

    private AdView mAdView;

    private LinearLayout mDots;
    private ViewPager mViewPager;

    private SwipeRefreshLayout mSwipeRefresh;
    private NestedScrollView mNestedView;

    private TextView mActivityTitle;
    private TextView mItemPrice, mItemCurrency, mItemDate, mItemLocation, mItemStatus;
    private EmojiconTextView mItemTitle;

    private CircleImageView mAuthorPhoto, mAuthorIcon;

    private ImageView mAuthorOnlineIcon;

    private TextView mAuthorFullname, mAuthorUsername, mInactiveTitle, mInactiveSubtitle, mInactiveDescription;

    private TextView mItemViewsCount;

    private WebView mContent;
    private MaterialRippleLayout mCallButton, mMessageButton, mProfileButton, mShowAllAdsButton;

    private RelativeLayout mBannerContainer;
    private LinearLayout mActionButtonsContainer, mInfoContainer, mLocationContainer, mInactiveContainer, mInviteShareContainer;

    private Button mItemShareButton;

    private LinearLayout mItemStatContainer;
    private TextView mItemStatViews, mItemStatFavorites, mItemStatPhoneViews;

    private ProgressBar mProgressBar;
    private TextView mInfoMessage;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    Item item = new Item();

    long itemId = 0;
    int arrayLength = 0;

    private String phoneNumber = "";

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean loadingComplete = false;

    private Boolean inviteShare = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    public ViewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle params = new Bundle();
        params.putString("action", "open");
        params.putString("fragment", "ViewItemFragment");
        mFirebaseAnalytics.logEvent("app_open_fragment", params);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        itemId = i.getLongExtra("itemId", 0);
        inviteShare = i.getBooleanExtra("inviteShare", false);

        getActivity().setTitle("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_item, container, false);

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

        } else {

            restore = false;
            loading = false;
            preload = false;
        }

        if (loading) {

            showpDialog();
        }

        if (images == null) {

            images = new ArrayList<ImageItem>();
        }

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        mAdView = (AdView) rootView.findViewById(R.id.adView);

        mDots = (LinearLayout) rootView.findViewById(R.id.dots);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);

        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(this);

        // Invite share link Container

        mInviteShareContainer = (LinearLayout) rootView.findViewById(R.id.invite_share_container);
        mItemShareButton = (Button) rootView.findViewById(R.id.invite_share_button);

        mItemShareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                urlShare(item.getTitle(), item.getLink());
            }
        });

        // Inactive Container

        mInactiveContainer = (LinearLayout) rootView.findViewById(R.id.inactive_container);
        mInactiveTitle = (TextView) rootView.findViewById(R.id.inactive_title);
        mInactiveSubtitle = (TextView) rootView.findViewById(R.id.inactive_subtitle);
        mInactiveDescription = (TextView) rootView.findViewById(R.id.inactive_description);

        // Stat Container

        mItemStatContainer = (LinearLayout) rootView.findViewById(R.id.item_stats_container);
        mItemStatViews = (TextView) rootView.findViewById(R.id.itemStatViews);
        mItemStatFavorites = (TextView) rootView.findViewById(R.id.itemStatFavorites);
        mItemStatPhoneViews = (TextView) rootView.findViewById(R.id.itemStatPhoneViews);

        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        mActivityTitle = (TextView) rootView.findViewById(R.id.title);

        mInfoMessage = (TextView) rootView.findViewById(R.id.info_message);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mItemViewsCount = (TextView) rootView.findViewById(R.id.itemViewsCount);

        mItemPrice = (TextView) rootView.findViewById(R.id.itemPrice);
        mItemCurrency = (TextView) rootView.findViewById(R.id.itemCurrency);
        mItemLocation = (TextView) rootView.findViewById(R.id.itemLocation);
        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.location_container);
        mItemStatus = (TextView) rootView.findViewById(R.id.itemStatus);
        mItemDate = (TextView) rootView.findViewById(R.id.itemDate);
        mItemTitle = (EmojiconTextView) rootView.findViewById(R.id.itemTitle);
        mContent = (WebView) rootView.findViewById(R.id.content);
        mCallButton = (MaterialRippleLayout) rootView.findViewById(R.id.call_button);
        mMessageButton = (MaterialRippleLayout) rootView.findViewById(R.id.msg_button);
        mProfileButton = (MaterialRippleLayout) rootView.findViewById(R.id.profile_button);
        mShowAllAdsButton = (MaterialRippleLayout) rootView.findViewById(R.id.show_all_ads_button);

        mAuthorPhoto = (CircleImageView) rootView.findViewById(R.id.itemAuthorPhoto);
        mAuthorIcon = (CircleImageView) rootView.findViewById(R.id.itemAuthorIcon);
        mAuthorOnlineIcon = (ImageView) rootView.findViewById(R.id.itemAuthorOnlineIcon);

        mAuthorFullname = (TextView) rootView.findViewById(R.id.itemAuthorFullname);
        mAuthorUsername = (TextView) rootView.findViewById(R.id.itemAuthorUsername);

        mActionButtonsContainer = (LinearLayout) rootView.findViewById(R.id.action_buttons_container);
        mInfoContainer = (LinearLayout) rootView.findViewById(R.id.info_container);

        mBannerContainer = (RelativeLayout) rootView.findViewById(R.id.banner_container);

        mProfileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                showAuthorProfile();
            }
        });

        mShowAllAdsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                showAuthorProfile();
            }
        });

        mMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().getId() != 0) {

                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatId", 0);
                    i.putExtra("profileId", item.getFromUserId());
                    i.putExtra("withProfile", item.getFromUserFullname());

                    i.putExtra("with_user_username", item.getFromUserUsername());
                    i.putExtra("with_user_fullname", item.getFromUserFullname());
                    i.putExtra("with_user_photo_url", item.getFromUserPhotoUrl());

                    i.putExtra("with_user_state", ACCOUNT_STATE_ENABLED);
                    i.putExtra("with_user_verified", 0);

                    i.putExtra("itemTitle", item.getTitle());
                    i.putExtra("itemId", item.getId());

                    startActivityForResult(i, PROFILE_CHAT);

                } else {

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.putExtra("pageId", "profile");
                    startActivityForResult(i, ACTION_LOGIN);
                }
            }
        });

        mCallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Increase phone number views

                Api api = new Api(getActivity());
                api.itemPhone(item.getId());


                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

                b.setTitle(phoneNumber);

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
                arrayAdapter.add(getString(R.string.action_phone_call));

                b.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.CALL_PHONE)) {

                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);

                            } else {

                                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
                            }

                        } else {

                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                            startActivity(intent);
                        }

                    }
                });

                b.setNegativeButton(getText(R.string.action_cancel), null);

                AlertDialog d = b.create();
                d.show();
            }
        });

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getItem();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    loadingComplete();
                    updateItem();

                } else {

                    showLoadingScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    private void showAuthorProfile() {

        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("profileId", item.getFromUserId());
        getActivity().startActivity(intent);
    }

    private void showAdBanner() {

        mBannerContainer.setVisibility(View.GONE);

        if (App.getInstance().getAdmob() == ADMOB_ENABLED) {

            AdRequest adRequest = new AdRequest.Builder().build();

            mAdView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {

                    super.onAdLoaded();

                    mBannerContainer.setVisibility(View.VISIBLE);

                    Log.e("ADMOB", "onAdLoaded");
                }

//                @Override
//                public void onAdFailedToLoad(int i) {
//
//                    super.onAdFailedToLoad(i);
//
//                    mBannerContainer.setVisibility(View.GONE);
//
//                    Log.e("ADMOB", "onAdFailedToLoad");
//                }
            });

            mAdView.loadAd(adRequest);

        } else {

            Log.e("ADMOB", "ADMOB_DISABLED");

            mBannerContainer.setVisibility(View.GONE);
        }
    }

    private void showSlider() {


        final ImagesAdapter adapterSlider = new ImagesAdapter(getActivity(), new ArrayList<ImageItem>());

        adapterSlider.setItems(images);
        mViewPager.setAdapter(adapterSlider);

        mViewPager.setCurrentItem(0);
        addBottomDots(mDots, images.size(), 0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {

                addBottomDots(mDots, images.size(), pos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        adapterSlider.setOnItemClickListener(new ImagesAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, ImageItem obj, int pos) {

                Intent i = new Intent(getActivity(), ImagesViewerActivity.class);
                i.putExtra("position", pos);
                i.putParcelableArrayListExtra("images", images);
                startActivity(i);
            }
        });
    }

    private void addBottomDots(LinearLayout layout_dots, int size, int current) {

        ImageView[] dots = new ImageView[size];

        layout_dots.removeAllViews();

        for (int i = 0; i < dots.length; i++) {

            dots[i] = new ImageView(getActivity());
            int width_height = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.dark_overlay));
            layout_dots.addView(dots[i]);
        }

        if (dots.length > 0)

            dots[current].setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getFromUserPhone()));
                    startActivity(intent);

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.CALL_PHONE)) {

                        showNoPhoneCallPermissionSnackbar();
                    }
                }

                return;
            }
        }
    }

    public void showNoPhoneCallPermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_phone_call_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_phone_call_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            images = data.getParcelableArrayListExtra("images");

            item.setAllowComments(data.getIntExtra("itemAllowComments", 1));

            item.setCategoryId(data.getIntExtra("categoryId", 1));
            item.setCategoryTitle(data.getStringExtra("categoryTitle"));
            item.setSubcategoryId(data.getIntExtra("subcategoryId", 0));
            item.setPrice(data.getIntExtra("itemPrice", 0));
            item.setCurrency(data.getIntExtra("itemCurrency", 0));
            item.setTitle(data.getStringExtra("itemTitle"));
            item.setContent(data.getStringExtra("itemDescription"));

            item.setCity(data.getStringExtra("itemCity"));
            item.setCountry(data.getStringExtra("itemCountry"));
            item.setArea(data.getStringExtra("itemArea"));

            item.setPhoneNumber(data.getStringExtra("phoneNumber"));

            item.setLat(data.getDoubleExtra("lat", 0.000000));
            item.setLng(data.getDoubleExtra("lng", 0.000000));

            item.setInactiveAt(0);
            item.setRejectedAt(0);
            item.setRejectedId(0);

            updateItem();

            loadingComplete();

        } else if (requestCode == ACTION_LOGIN && resultCode == getActivity().RESULT_OK) {

            // ACTION_LOGIN
        }
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            mSwipeRefresh.setRefreshing(true);
            getItem();

        } else {

            mSwipeRefresh.setRefreshing(false);
        }
    }

    public void updateItem() {

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        // Invite share container

        mInviteShareContainer.setVisibility(View.GONE);

        if (inviteShare) {

            mInviteShareContainer.setVisibility(View.VISIBLE);
        }

        // Stats container

        mItemStatContainer.setVisibility(View.GONE);

        if (item.getFromUserId() == App.getInstance().getId() && !inviteShare) {

            mItemStatViews.setText(String.format(Locale.getDefault(), "%d", item.getViewsCount()));
            mItemStatFavorites.setText(String.format(Locale.getDefault(), "%d", item.getLikesCount()));
            mItemStatPhoneViews.setText(String.format(Locale.getDefault(), "%d", item.getPhoneViewsCount()));

            mItemStatContainer.setVisibility(View.VISIBLE);
        }

        // Show|Hide message about inactive item

        hideInactiveMessage();

        if (item.getInactiveAt() != 0) {

            showInactiveMessage();
        }

        // Get phone number for phone call function

        if (item.getPhoneNumber().length() != 0) {

            phoneNumber = item.getPhoneNumber();

        } else {

            phoneNumber = item.getFromUserPhone();
        }

        // Show AdMob Banner

        showAdBanner();

        // Show Images Pager (Slider)

        showSlider();

        // Phone Call button

//        PackageManager pm = getActivity().getPackageManager();
//
//        if (item.getFromUserPhone().length() > 0 && pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
//
//            mCallButton.setVisibility(View.VISIBLE);
//
//        } else {
//
//            mCallButton.setVisibility(View.GONE);
//        }


        // Show views count

        mItemViewsCount.setVisibility(View.VISIBLE);
        mItemViewsCount.setText(String.format(Locale.getDefault(), "%d", item.getViewsCount()));

        // Update Item Author data

        mAuthorFullname.setText(item.getFromUserFullname());
        mAuthorUsername.setText("@" +item.getFromUserUsername());

        mAuthorOnlineIcon.setVisibility(View.GONE);
        mAuthorIcon.setVisibility(View.GONE);

        if (item.getFromUserPhotoUrl().length() > 0) {

            try {

                Glide.with(getActivity()).load(item.getFromUserPhotoUrl())
                        .transition(withCrossFade())
                        .into(mAuthorPhoto);

            } catch (Exception e) {

                Log.e("Cant load image", e.toString());
            }

        } else {

            mAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
        }

        // Set Activity title

        mActivityTitle.setText(item.getTitle());

        getActivity().setTitle("");

        // Set item title

        if (item.getTitle().length() > 0) {

            mItemTitle.setText(item.getTitle());

            mItemTitle.setVisibility(View.VISIBLE);

        } else {

            mItemTitle.setVisibility(View.GONE);
        }

        // Show item status

        mItemStatus.setVisibility(View.GONE);

        // Show item location

        if (item.getCity().length() > 0 || item.getCountry().length() > 0) {

            mLocationContainer.setVisibility(View.VISIBLE);
            mItemLocation.setText(item.getCountry() + " " + item.getCity());

        } else {

            mLocationContainer.setVisibility(View.GONE);
        }

        // Show item date

        mItemDate.setText(item.getDate());

        // Show price and currency

        mItemPrice.setVisibility(View.GONE);
        mItemCurrency.setVisibility(View.GONE);

        Helper helper = new Helper();

        if (item.getCurrency() > 2) {

            mItemPrice.setVisibility(View.VISIBLE);
            mItemPrice.setText(helper.getCurrency(getActivity(), item.getCurrency(), item.getPrice()));

            mItemCurrency.setVisibility(View.VISIBLE);
            mItemCurrency.setText(App.getInstance().getCurrencyList().get(item.getCurrency() - 3).getName());

        } else {

            mItemPrice.setVisibility(View.VISIBLE);
            mItemPrice.setText(helper.getCurrency(getActivity(), item.getCurrency(), item.getPrice()));

            mItemCurrency.setVisibility(View.GONE);
        }

        // Show main item content in webview

        String hexColor = Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorTitle));
        final String hexColor2 = "#" + hexColor.substring(2) + hexColor.substring(0, 2);

        String html_data = "<html><head><style type=\"text/css\"> img{max-width:100%;height:auto;} iframe{width:100%;}</style> </head><body>" + item.getContent() + "</body></html>";

        //html_data += item.getContent();
        mContent.getSettings().setJavaScriptEnabled(true);
        mContent.getSettings().setBuiltInZoomControls(true);
        mContent.setBackgroundColor(Color.TRANSPARENT);
        mContent.setWebChromeClient(new WebChromeClient());
        mContent.loadData(html_data, "text/html; charset=UTF-8", null);

        mContent.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

                mContent.loadUrl(
                        "javascript:document.body.style.setProperty(\"color\", \"" + hexColor2 + "\");"
                );
            }
        });

        // disable scroll on touch
        mContent.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
    }


    public void getItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                            return;
                        }

                        try {

                            arrayLength = 0;

                            images.clear();

                            // Load item data

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            item = new Item(itemObj);
                                        }
                                    }
                                }

                                // Load images data

                                if (response.has("images")) {

                                    JSONArray imagesObj = response.getJSONArray("images");

                                    if (imagesObj.length() > 0) {

                                        JSONObject imgObj = (JSONObject) imagesObj.get(0);

                                        if (imgObj.has("items")) {

                                            JSONArray imagesArray = imgObj.getJSONArray("items");

                                            arrayLength = imagesArray.length();

                                            if (arrayLength > 0) {

                                                for (int i = 0; i < imagesArray.length(); i++) {

                                                    JSONObject iObj = (JSONObject) imagesArray.get(i);

                                                    images.add(new ImageItem(null, iObj.getString("imgUrl")));
                                                }

                                                Collections.reverse(images);
                                            }
                                        }
                                    }
                                }

                                if (images.size() > 0) {

                                    images.add(0, new ImageItem(null, item.getImgUrl()));

                                } else {

                                    images.add(new ImageItem(null, item.getImgUrl()));
                                }

                                updateItem();

                                loadingComplete();

                            } else {

                                showErrorScreen();
                            }

                        } catch (JSONException e) {

                            showErrorScreen();

                            e.printStackTrace();

                        } finally {

                            Log.e("test", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                    return;
                }

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("clientId", Constants.CLIENT_ID);

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("itemId", Long.toString(itemId));
                params.put("lang", App.getInstance().getLanguage());

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void onItemEdit(final int position) {

        Intent i = new Intent(getActivity(), NewItemActivity.class);

        i.putExtra("categoryId", item.getCategoryId());
        i.putExtra("subcategoryId", item.getSubcategoryId());

        i.putExtra("mode", MODE_EDIT);
        i.putParcelableArrayListExtra("images", images);

        i.putExtra("itemId", item.getId());
        i.putExtra("itemPrice", item.getPrice());
        i.putExtra("itemCurrency", item.getCurrency());

        i.putExtra("itemTitle", item.getTitle());
        i.putExtra("itemDescription", item.getContent());

        i.putExtra("itemAllowComments", item.getAllowComments());

        i.putExtra("itemCity", item.getCity());
        i.putExtra("itemCountry", item.getCountry());
        i.putExtra("itemArea", item.getArea());

        i.putExtra("phoneNumber", phoneNumber);

        i.putExtra("lat", item.getLat());
        i.putExtra("lng", item.getLng());

        startActivityForResult(i, ITEM_EDIT);
    }

    public void onItemRemove(int position) {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        b.setTitle(getText(R.string.label_delete));
        b.setMessage(getText(R.string.label_delete_msg));

        b.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Api api = new Api(getActivity());

                api.itemDelete(item.getId());

                getActivity().finish();
            }
        });

        b.setNegativeButton(getText(R.string.action_no), null);

        AlertDialog d = b.create();
        d.show();
    }

    public void onItemInactivate(int position) {

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        b.setTitle(getText(R.string.action_item_inactivate));

        b.setMessage(getText(R.string.msg_action_inactivate));

        b.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_INACTIVATE, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                if (!isAdded() || getActivity() == null) {

                                    Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                                    return;
                                }

                                try {

                                    if (!response.getBoolean("error")) {

                                        item.setInactiveAt(1);

                                        updateItem();
                                    }

                                } catch (JSONException e) {

                                    e.printStackTrace();

                                } finally {

                                    getActivity().invalidateOptionsMenu();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                            return;
                        }
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("accountId", Long.toString(App.getInstance().getId()));
                        params.put("accessToken", App.getInstance().getAccessToken());
                        params.put("itemId", Long.toString(item.getId()));

                        return params;
                    }
                };

                App.getInstance().addToRequestQueue(jsonReq);
            }
        });

        b.setNegativeButton(getText(R.string.action_no), null);

        AlertDialog d = b.create();
        d.show();
    }

    public void report(int position) {

        String[] profile_report_categories = new String[] {

                getText(R.string.label_profile_report_0).toString(),
                getText(R.string.label_profile_report_1).toString(),
                getText(R.string.label_profile_report_2).toString(),
                getText(R.string.label_profile_report_4).toString(),

        };

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        b.setTitle(getText(R.string.label_post_report_title));

        b.setSingleChoiceItems(profile_report_categories, 0, null);

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (App.getInstance().isConnected()) {

                    Api api = new Api(getActivity());

                    api.itemReport(item.getId(), which);

                } else {

                    Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** Setting a positive button and its listener */
        b.setNegativeButton(getText(R.string.action_cancel), null);

        /** Creating the alert dialog window using the builder class */
        AlertDialog d = b.create();
        d.show();
    }

    public void loadingComplete() {

        if (item.getId() == 0) {

            showEmptyScreen();

        } else {

            showContentScreen();
        }

        if (mSwipeRefresh.isRefreshing()) {

            mSwipeRefresh.setRefreshing(false);
        }
    }

    public void showLoadingScreen() {

        preload = true;

        mNestedView.setVisibility(View.GONE);
        mActionButtonsContainer.setVisibility(View.GONE);

        mInfoContainer.setVisibility(View.VISIBLE);

        mInfoMessage.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showEmptyScreen() {

        mNestedView.setVisibility(View.GONE);
        mActionButtonsContainer.setVisibility(View.GONE);

        mInfoContainer.setVisibility(View.VISIBLE);

        mInfoMessage.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        mInfoMessage.setText(getString(R.string.label_empty_list));

        loadingComplete = false;
    }

    public void showErrorScreen() {

        mNestedView.setVisibility(View.GONE);
        mActionButtonsContainer.setVisibility(View.GONE);

        mInfoContainer.setVisibility(View.VISIBLE);

        mInfoMessage.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        mInfoMessage.setText(getString(R.string.error_data_loading));

        loadingComplete = false;
    }

    public void showContentScreen() {

        preload = false;

        // Buttons container

        if (App.getInstance().getId() == item.getFromUserId()) {

            mActionButtonsContainer.setVisibility(View.GONE);

        } else {

            if (item.getInactiveAt() == 0) {

                mActionButtonsContainer.setVisibility(View.VISIBLE);

            } else {

                mActionButtonsContainer.setVisibility(View.GONE);
            }
        }

        //

        mNestedView.setVisibility(View.VISIBLE);

        mInfoContainer.setVisibility(View.GONE);

        loadingComplete = true;

        getActivity().invalidateOptionsMenu();
    }


    public void modifyFavorites() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_LIKE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                item.setLikesCount(response.getInt("likesCount"));
                                item.setMyLike(response.getBoolean("myLike"));
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                    return;
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(item.getId()));

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void itemActivate() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_ACTIVATE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                item.setInactiveAt(0);

                                updateItem();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            getActivity().invalidateOptionsMenu();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ViewItemFragment Not Added to Activity");

                    return;
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(item.getId()));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_view_item, menu);

//        MainMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (App.getInstance().getId() != item.getFromUserId()) {

                menu.removeItem(R.id.action_edit);
                menu.removeItem(R.id.action_activate);
                menu.removeItem(R.id.action_inactivate);
                menu.removeItem(R.id.action_remove);
            }

            if (this.item.getInactiveAt() == 0) {

                // ad active | delete "Make active" menu item

                menu.removeItem(R.id.action_activate);

            } else {

                // ad inactive | delete "Make inactive" menu item

                menu.removeItem(R.id.action_inactivate);

                if (this.item.getRejectedAt() != 0) {

                    // ad rejected | delete "Make active" menu item

                    menu.removeItem(R.id.action_activate);
                }
            }

            // Setup favorites button

            if (this.item.isMyLike()) {

                menu.getItem(1).setIcon(R.drawable.ic_action_important);
                menu.getItem(1).setTitle(R.string.action_favorites_delete);

            } else {

                menu.getItem(1).setIcon(R.drawable.ic_action_not_important);
                menu.getItem(1).setTitle(R.string.action_favorites_add);
            }

            // If site not available - hide items

            if (!WEB_SITE_AVAILABLE) {

                menu.removeItem(R.id.action_open_url);
                menu.removeItem(R.id.action_copy_url);
            }

            //show all menu items
            hideMenuItems(menu, true);

        } else {

            //hide all menu items
            hideMenuItems(menu, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_favorites: {

                // Modify (add|delete) favorites

                if (App.getInstance().getId() != 0) {

                    if (this.item.isMyLike()) {

                        // Delete from favorites

                        item.setIcon(R.drawable.ic_action_not_important);
                        item.setTitle(R.string.action_favorites_add);

                        this.item.setMyLike(false);

                        Toast.makeText(getActivity(), getText(R.string.message_favorites_deleted), Toast.LENGTH_SHORT).show();

                    } else {

                        // Add to favorites

                        item.setIcon(R.drawable.ic_action_important);
                        item.setTitle(R.string.action_favorites_delete);

                        this.item.setMyLike(true);

                        Toast.makeText(getActivity(), getText(R.string.message_favorites_added), Toast.LENGTH_SHORT).show();
                    }

                    modifyFavorites();

                } else {

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.putExtra("pageId", "profile");
                    startActivityForResult(i, ACTION_LOGIN);
                }

                return true;
            }

            case R.id.action_edit: {

                // edit item

                onItemEdit(0);

                return true;
            }

            case R.id.action_activate: {

                // Activate|show item

                itemActivate();

                return true;
            }

            case R.id.action_inactivate: {

                // inactivate|hide item

                onItemInactivate(0);

                return true;
            }

            case R.id.action_remove: {

                // remove item

                onItemRemove(0);

                return true;
            }

            case R.id.action_report: {

                // report item

                report(0);

                return true;
            }

            case R.id.action_share: {

                // share item

                urlShare(this.item.getTitle(), this.item.getLink());

                return true;
            }

            case R.id.action_open_url: {

                urlOpen(this.item.getLink());

                return true;
            }

            case R.id.action_copy_url: {

                setClipboard(getActivity(), this.item.getLink());

                Toast.makeText(getActivity(), getText(R.string.msg_copied_to_clipboard), Toast.LENGTH_SHORT).show();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void setClipboard(Context context, String text) {

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {

            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);

        } else {

            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void urlOpen(String url) {

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void urlShare(String subject, String url) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(i, getActivity().getString(R.string.action_share)));

        // Increase share link counter

        Api api = new Api(getActivity());
        api.itemShare(item.getId());
    }

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    private void showInactiveMessage() {

        mInactiveContainer.setVisibility(View.VISIBLE);

        mInactiveSubtitle.setVisibility(View.GONE);
        mInactiveDescription.setVisibility(View.GONE);

        if (item.getFromUserId() == App.getInstance().getId()) {

            mInactiveSubtitle.setVisibility(View.VISIBLE);

            if (item.getRejectedId() != 0) {

                mInactiveDescription.setText(getString(R.string.label_item_inactive_description));
                mInactiveDescription.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideInactiveMessage() {

        mInactiveContainer.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}