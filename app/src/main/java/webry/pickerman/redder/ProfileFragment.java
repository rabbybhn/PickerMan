package webry.pickerman.redder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import webry.pickerman.redder.adapter.ProductListAdapter;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.model.Profile;
import webry.pickerman.redder.util.Api;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;
import webry.pickerman.redder.view.SpacingItemDecoration;

public class ProfileFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";

    private ProgressDialog pDialog;

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 1;
    private static final int SELECT_COVER = 2;
    private static final int PROFILE_EDIT = 3;
    private static final int PROFILE_NEW_POST = 4;
    private static final int CREATE_PHOTO = 5;
    private static final int CREATE_COVER = 6;
    private static final int PROFILE_CHAT = 7;

    Toolbar mToolbar;

    Button mProfileActionBtn, mProfileMessageBtn;

    ProgressBar mProgressBar;

    TextView mMessage;
    ImageView mSplash;

    TextView profileFullname, profileUsername, profileItemsCount, profileCommentsCount;
    TextView profileLocation, mProfileFacebookPage, mProfileInstagramPage, mProfileBio;

    SwipeRefreshLayout mRefreshView;
    NestedScrollView mNestedView;
    RecyclerView mRecyclerView;

    LinearLayout mProfileContainer, mLocationContainer;


    ImageView profileCover;
    CircleImageView profilePhoto, profileVerified, profileOnline;

    Profile profile;

    private ArrayList<Item> itemsList;

    private ProductListAdapter itemsAdapter;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private Boolean loadingComplete = false;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;

    private String profile_mention;
    public long profile_id;
    int pageId = 0;
    int upload_mode = 0;
    int moderationType = 0;
    int arrayLength = 0;
    int accessMode = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;

    private Boolean isMainScreen = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle params = new Bundle();
        params.putString("action", "open");
        params.putString("fragment", "ProfileFragment");
        mFirebaseAnalytics.logEvent("app_open_fragment", params);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        profile_id = i.getLongExtra("profileId", 0);
        profile_mention = i.getStringExtra("profileMention");

        if (profile_id == 0 && (profile_mention == null || profile_mention.length() == 0)) {

            profile_id = App.getInstance().getId();
            isMainScreen = true;
        }

        profile = new Profile();
        profile.setId(profile_id);

        itemsList = new ArrayList<Item>();
        itemsAdapter = new ProductListAdapter(getActivity(), itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new ProductListAdapter(getActivity(), itemsList);

            pageId = savedInstanceState.getInt("pageId");
            upload_mode = savedInstanceState.getInt("upload_mode");

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

            profile = savedInstanceState.getParcelable("profileObj");

        } else {

            itemsList = new ArrayList<Item>();
            itemsAdapter = new ProductListAdapter(getActivity(), itemsList);

            pageId = 0;
            upload_mode = 0;

            restore = false;
            loading = false;
            preload = false;
        }

        if (loading) {


            showpDialog();
        }

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    if (result.getData() != null) {

                        selectedImage = result.getData().getData();

                        selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName);

                        switch (upload_mode) {

                            case 1: {

                                uploadFile(METHOD_PROFILE_UPLOADCOVER, f, UPLOAD_TYPE_COVER);

                                break;
                            }

                            default: {

                                uploadFile(METHOD_PROFILE_UPLOADPHOTO, f, UPLOAD_TYPE_PHOTO);

                                break;
                            }
                        }
                    }
                }
            }
        });

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        selectedImage = result.getData().getData();

                        newImageFileName = Helper.randomString(6) + ".jpg";

                        Helper helper = new Helper(getContext());
                        helper.saveImg(selectedImage, newImageFileName);

                        selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName);

                        switch (upload_mode) {

                            case 1: {

                                uploadFile(METHOD_PROFILE_UPLOADCOVER, f, UPLOAD_TYPE_COVER);

                                break;
                            }

                            default: {

                                uploadFile(METHOD_PROFILE_UPLOADPHOTO, f, UPLOAD_TYPE_PHOTO);

                                break;
                            }
                        }
                    }
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                showChooseImageDialog();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                showChooseImageDialog();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        mProfileContainer = (LinearLayout) rootView.findViewById(R.id.container_body);
        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.location_container);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mProfileActionBtn = (Button) rootView.findViewById(R.id.action_btn);
        mProfileMessageBtn = (Button) rootView.findViewById(R.id.message_btn);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_view);
        mRefreshView.setOnRefreshListener(this);

        mNestedView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getGridSpanCount(getActivity()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SpacingItemDecoration(2, Helper.dpToPx(getActivity(), 4), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Item item, int position) {

                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }
        });

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY < oldScrollY) { // up

//                    animateFab(false);
                    if (isMainScreen) ((MainActivity)getActivity()).animateSearchBar(false);
//                    animateSearchBar(false);
                }

                if (scrollY > oldScrollY) { // down

//                    animateFab(true);
                    if (isMainScreen) ((MainActivity)getActivity()).animateSearchBar(true);
//                    animateSearchBar(true);
                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                    if (!loadingMore && (viewMore) && !(mRefreshView.isRefreshing())) {

                        mRefreshView.setRefreshing(true);

                        loadingMore = true;

                        getItems();
                    }
                }
            }
        });

        profileFullname = (TextView) rootView.findViewById(R.id.fullname);
        profileUsername = (TextView) rootView.findViewById(R.id.username);
        profileLocation = (TextView) rootView.findViewById(R.id.location);
        profileItemsCount = (TextView) rootView.findViewById(R.id.itemsCount);
        profileCommentsCount = (TextView) rootView.findViewById(R.id.commentsCount);

//        mProfileFacebookPage = (TextView) profileListViewHeader.findViewById(R.id.profileFacebookPage);
//        mProfileInstagramPage = (TextView) profileListViewHeader.findViewById(R.id.profileInstagramPage);
//        mProfileBio = (TextView) profileListViewHeader.findViewById(R.id.profileBio);

//        mItemsCount = (TextView) profileListViewHeader.findViewById(R.id.itemsCount);
//        mCommentsCount = (TextView) profileListViewHeader.findViewById(R.id.commentsCount);

//        mProfileFacebookPage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!profile.getFacebookPage().startsWith("https://") && !profile.getFacebookPage().startsWith("http://")){
//
//                    profile.setFacebookPage("http://" + profile.getFacebookPage());
//                }
//
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(profile.getFacebookPage()));
//                startActivity(i);
//            }
//        });
//
//        mProfileInstagramPage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!profile.getInstagramPage().startsWith("https://") && !profile.getInstagramPage().startsWith("http://")){
//
//                    profile.setInstagramPage("http://" + profile.getInstagramPage());
//                }
//
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(profile.getInstagramPage()));
//                startActivity(i);
//            }
//        });

        profileVerified = (CircleImageView) rootView.findViewById(R.id.verified);
        profileOnline = (CircleImageView) rootView.findViewById(R.id.online);

        profilePhoto = (CircleImageView) rootView.findViewById(R.id.photo);
        profileCover = (ImageView) rootView.findViewById(R.id.cover);

        mProfileActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() == profile.getId()) {

                    Intent i = new Intent(getActivity(), AccountSettingsActivity.class);
                    i.putExtra("profileId", App.getInstance().getId());
                    i.putExtra("sex", profile.getSex());
                    i.putExtra("year", profile.getYear());
                    i.putExtra("month", profile.getMonth());
                    i.putExtra("day", profile.getDay());
                    i.putExtra("phone", profile.getPhone());
                    i.putExtra("fullname", profile.getFullname());
                    i.putExtra("location", profile.getLocation());
                    i.putExtra("facebookPage", profile.getFacebookPage());
                    i.putExtra("instagramPage", profile.getInstagramPage());
                    i.putExtra("bio", profile.getBio());
                    startActivityForResult(i, PROFILE_EDIT);
                }
            }
        });

        mProfileMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() != 0) {

                    if (App.getInstance().getId() == profile.getId()) {

                        Intent intent = new Intent(getActivity(), NewItemActivity.class);
                        startActivityForResult(intent, PROFILE_NEW_POST);

                    } else {

                        if (profile.getAllowMessages() == 0) {

                            Toast.makeText(getActivity(), getString(R.string.error_disable_messages), Toast.LENGTH_SHORT).show();

                        } else {

                            if (!profile.isInBlackList()) {

                                Intent i = new Intent(getActivity(), ChatActivity.class);
                                i.putExtra("chatId", 0);
                                i.putExtra("profileId", profile.getId());
                                i.putExtra("withProfile", profile.getFullname());

                                i.putExtra("with_user_username", profile.getUsername());
                                i.putExtra("with_user_fullname", profile.getFullname());
                                i.putExtra("with_user_photo_url", profile.getLowPhotoUrl());

                                i.putExtra("with_user_state", profile.getState());
                                i.putExtra("with_user_verified", profile.getVerify());

                                i.putExtra("itemTitle", "");
                                i.putExtra("itemId", Long.valueOf(0));

                                startActivityForResult(i, PROFILE_CHAT);

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.error_action), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_signup_messages), Toast.LENGTH_SHORT).show();
                }
            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (profile.getNormalPhotoUrl().length() > 0) {

                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("imgUrl", profile.getNormalPhotoUrl());
                    startActivity(i);
                }
            }
        });

        if (profile.getFullname() == null || profile.getFullname().length() == 0) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getData();

                Log.e("Profile", "OnReload");

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (profile.getState() == ACCOUNT_STATE_ENABLED) {

                    showContentScreen();

                    loadingComplete();
                    updateProfile();

                } else {

                    showDisabledScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("pageId", pageId);
        outState.putInt("upload_mode", upload_mode);

        outState.putBoolean("restore", restore);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);

        outState.putParcelable("profileObj", profile);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_EDIT && resultCode == getActivity().RESULT_OK) {

            profile.setPhone(data.getStringExtra("phone"));
            profile.setFullname(data.getStringExtra("fullname"));
            profile.setLocation(data.getStringExtra("location"));
            profile.setFacebookPage(data.getStringExtra("facebookPage"));
            profile.setInstagramPage(data.getStringExtra("instagramPage"));
            profile.setBio(data.getStringExtra("bio"));

            profile.setSex(data.getIntExtra("sex", 0));

            profile.setYear(data.getIntExtra("year", 0));
            profile.setMonth(data.getIntExtra("month", 0));
            profile.setDay(data.getIntExtra("day", 0));

            updateProfile();

        } else if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            Item item = itemsList.get(position);

            item.setContent(data.getStringExtra("post"));
            item.setImgUrl(data.getStringExtra("imgUrl"));

            itemsAdapter.notifyDataSetChanged();
        }
    }

    public void showChooseImageDialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");

                        imgFromGalleryActivityResultLauncher.launch(intent);

                        break;
                    }

                    default: {

                        Helper helper = new Helper(getActivity());

                        if (helper.checkPermission(Manifest.permission.CAMERA)) {

                            try {

                                newImageFileName = Helper.randomString(6) + ".jpg";

                                selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));

                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                imgFromCameraActivityResultLauncher.launch(cameraIntent);

                            } catch (Exception e) {

                                Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            requestCameraPermission();
                        }

                        break;
                    }
                }

            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            getData();

        } else {

            mRefreshView.setRefreshing(false);
        }
    }

    public void updateProfile() {

        if (profile.getLastActive() != 0) {

            if (profile.isOnline()) {

                profileOnline.setVisibility(View.VISIBLE);

            } else {

                profileOnline.setVisibility(View.GONE);

                //mProfileActive.setText(profile.getLastActiveTimeAgo());
            }

        } else {

            profileOnline.setVisibility(View.GONE);
        }

        if (profile.isVerify()) {

            profileVerified.setVisibility(View.VISIBLE);

        } else {

            profileVerified.setVisibility(View.GONE);
        }

        profileUsername.setText("@" + profile.getUsername());
        profileLocation.setText(profile.getLocation());
//        mProfileFacebookPage.setText(profile.getFacebookPage());
//        mProfileInstagramPage.setText(profile.getInstagramPage());
//        mProfileBio.setText(profile.getBio().replaceAll("<br>", "\n"));

        // Show settings button is your profile
        if (profile.getId() == App.getInstance().getId()) {

            mProfileActionBtn.setText(R.string.action_profile_edit);
            mProfileMessageBtn.setText(R.string.action_add_classified);

            mProfileActionBtn.setVisibility(View.VISIBLE);
            mProfileMessageBtn.setVisibility(View.VISIBLE);

        } else {

            mProfileActionBtn.setVisibility(View.GONE);
            mProfileMessageBtn.setVisibility(View.VISIBLE);
        }

        if (profile.getLocation() != null && profile.getLocation().length() != 0) {

            mLocationContainer.setVisibility(View.VISIBLE);

        } else {

            mLocationContainer.setVisibility(View.GONE);
        }

//        if (profile.getFacebookPage() != null && profile.getFacebookPage().length() != 0) {
//
//            mProfileFacebookPage.setVisibility(View.VISIBLE);
//
//        } else {
//
//            mProfileFacebookPage.setVisibility(View.GONE);
//        }
//
//        if (profile.getInstagramPage() != null && profile.getInstagramPage().length() != 0) {
//
//            mProfileInstagramPage.setVisibility(View.VISIBLE);
//
//        } else {
//
//            mProfileInstagramPage.setVisibility(View.GONE);
//        }
//
//        if (profile.getBio() != null && profile.getBio().length() != 0) {
//
//            mProfileBio.setVisibility(View.VISIBLE);
//
//        } else {
//
//            mProfileBio.setVisibility(View.GONE);
//        }

        updateFullname();
        updateItemsCount();


        profileCommentsCount.setVisibility(View.GONE);

        showPhoto(profile.getLowPhotoUrl());
        showCover(profile.getNormalCoverUrl());

        showContentScreen();

        if (this.isVisible()) {

            try {

                getActivity().invalidateOptionsMenu();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void updateFullname() {

        if (profile.getFullname().length() == 0) {

            profileFullname.setText(profile.getUsername());
            if (!isMainScreen) getActivity().setTitle(profile.getUsername());

        } else {

            profileFullname.setText(profile.getFullname());
            if (!isMainScreen) getActivity().setTitle(profile.getFullname());
        }

        if (!profile.isVerify()) {

            profileFullname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private void updateItemsCount() {

        profileItemsCount.setText(Integer.toString(profile.getItemsCount()) + " " + getString(R.string.label_items));
    }

    public void getData() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                profile = new Profile(response);

                                if (profile.getItemsCount() > 0) {

                                    getItems();

                                } else {

                                    itemsList.clear();

                                    itemsAdapter.notifyDataSetChanged();
                                }

                                if (profile.getState() == ACCOUNT_STATE_ENABLED) {

                                    showContentScreen();

                                    updateProfile();

                                } else {

                                    showDisabledScreen();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                showErrorScreen();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(profile_id));

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }


    public void showPhoto(String photoUrl) {

        if (photoUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(photoUrl, ImageLoader.getImageListener(profilePhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
        }
    }

    public void showCover(String coverUrl) {

        if (coverUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(profileCover, R.drawable.profile_default_cover, R.drawable.profile_default_cover));

            if (Build.VERSION.SDK_INT > 15) {

                profileCover.setImageAlpha(200);
            }
        }
    }

    public void getItems() {

        if (loadingMore) {

            mRefreshView.setRefreshing(true);

        } else{

            pageId = 0;
        }

        if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT && App.getInstance().getId() != profile_id) {

            moderationType = 1;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_WALL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ProfileFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Item item = new Item(itemObj);

                                            itemsList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ProfileFragment Not Added to Activity");

                    return;
                }

                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("clientId", Constants.CLIENT_ID);

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profile.getId()));
                params.put("pageId", Integer.toString(pageId));
                params.put("moderationType", Integer.toString(moderationType));

                params.put("page", "profile");

                params.put("lang", App.getInstance().getLanguage());

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

            pageId++;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();

        mRefreshView.setRefreshing(false);

        loadingMore = false;

//        if (this.isVisible()) getActivity().invalidateOptionsMenu();
    }

    public void showMessage(String message) {

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);
    }

    public void showLoadingScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

        mProgressBar.setVisibility(View.VISIBLE);

        mSplash.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);

        mProfileContainer.setVisibility(View.GONE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        if (!isMainScreen) getActivity().setTitle(getText(R.string.title_activity_profile));

        showMessage(getString(R.string.msg_network_error));

        mProgressBar.setVisibility(View.GONE);

        mSplash.setVisibility(View.VISIBLE);
        mMessage.setVisibility(View.VISIBLE);

        mProfileContainer.setVisibility(View.GONE);

        loadingComplete = false;
    }

    public void showDisabledScreen() {

        if (profile.getState() != ACCOUNT_STATE_ENABLED) {

            showMessage(getString(R.string.msg_account_blocked));
        }

        getActivity().setTitle(getText(R.string.label_account_disabled));

        mProgressBar.setVisibility(View.GONE);

        mSplash.setVisibility(View.VISIBLE);
        mMessage.setVisibility(View.VISIBLE);

        mProfileContainer.setVisibility(View.GONE);

        loadingComplete = false;
    }

    public void showContentScreen() {

        if (!isMainScreen) {

            getActivity().setTitle(profile.getFullname());

        } else {

            getActivity().setTitle(getText(R.string.title_activity_profile));
        }

        mProgressBar.setVisibility(View.GONE);

        mSplash.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);

        mProfileContainer.setVisibility(View.VISIBLE);

        mRefreshView.setRefreshing(false);

        loadingComplete = true;
        restore = true;
    }

    public void action(int position) {

        final Item item = itemsList.get(position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (loadingComplete) {

            if (profile.getState() != ACCOUNT_STATE_ENABLED) {

                //hide all menu items
                hideMenuItems(menu, false);
            }

            if (App.getInstance().getId() != profile.getId()) {

                MenuItem menuItem = menu.findItem(R.id.action_profile_block);

                if (profile.isBlocked()) {

                    menuItem.setTitle(getString(R.string.action_unblock));

                } else {

                    menuItem.setTitle(getString(R.string.action_block));
                }

                menu.removeItem(R.id.action_profile_edit_photo);
                menu.removeItem(R.id.action_profile_edit_cover);

            } else {

                // your profile

                menu.removeItem(R.id.action_profile_report);
                menu.removeItem(R.id.action_profile_block);
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

            case R.id.action_profile_refresh: {

                mRefreshView.setRefreshing(true);
                onRefresh();

                return true;
            }

            case R.id.action_profile_report: {

                profileReport();

                return true;
            }

            case R.id.action_profile_block: {

                profileBlock();

                return true;
            }

            case R.id.action_profile_edit_photo: {

                Helper helper = new Helper(getActivity());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    upload_mode = 0;

                    showChooseImageDialog();
                }

                return true;
            }

            case R.id.action_profile_edit_cover: {

                Helper helper = new Helper(getActivity());

                if (!helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    upload_mode = 1;

                    showChooseImageDialog();
                }

                return true;
            }

            case R.id.action_open_url: {

                urlOpen(WEB_SITE + "/" + this.profile.getUsername());

                return true;
            }

            case R.id.action_copy_url: {

                setClipboard(getActivity(), WEB_SITE + "/" + this.profile.getUsername());

                Toast.makeText(getActivity(), getText(R.string.msg_profile_link_copied), Toast.LENGTH_SHORT).show();

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

    private void hideMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void profileReport() {

        String[] profile_report_categories = new String[] {

                getText(R.string.label_profile_report_0).toString(),
                getText(R.string.label_profile_report_1).toString(),
                getText(R.string.label_profile_report_2).toString(),
                getText(R.string.label_profile_report_3).toString(),
        };

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        b.setTitle(getText(R.string.label_post_report_title));

        b.setSingleChoiceItems(profile_report_categories, 0, null);

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Api api = new Api(getActivity());

                api.profileReport(profile.getId(), which);
            }
        });

        b.setNegativeButton(getText(R.string.action_cancel), null);

        AlertDialog d = b.create();
        d.show();
    }

    public void profileBlock() {

        if (!profile.isBlocked()) {

            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

            b.setTitle(getText(R.string.label_block) + " @" + profile.getUsername() + "?");

            b.setMessage("@" + profile.getUsername() + " " + getText(R.string.label_block_msg) + " @" + profile.getUsername() + ".");

            b.setPositiveButton(getText(R.string.action_block), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    loading = true;

                    showpDialog();

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_ADD, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    if (!isAdded() || getActivity() == null) {

                                        Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                        return;
                                    }

                                    try {

                                        if (!response.getBoolean("error")) {

                                            profile.setBlocked(true);

                                            Toast.makeText(getActivity(), getString(R.string.msg_profile_added_to_blacklist), Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {

                                        e.printStackTrace();

                                    } finally {

                                        loading = false;

                                        hidepDialog();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                return;
                            }

                            loading = false;

                            hidepDialog();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("accountId", Long.toString(App.getInstance().getId()));
                            params.put("accessToken", App.getInstance().getAccessToken());
                            params.put("profileId", Long.toString(profile.getId()));
                            params.put("reason", "example");

                            return params;
                        }
                    };

                    App.getInstance().addToRequestQueue(jsonReq);
                }
            });

            b.setNegativeButton(getText(R.string.action_cancel), null);

            AlertDialog d = b.create();
            d.show();

        } else {

            loading = true;

            showpDialog();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_BLACKLIST_REMOVE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ProfileFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    profile.setBlocked(false);

                                    Toast.makeText(getActivity(), getString(R.string.msg_profile_removed_from_blacklist), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                loading = false;

                                hidepDialog();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "ProfileFragment Not Added to Activity");

                        return;
                    }

                    loading = false;

                    hidepDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("profileId", Long.toString(profile.getId()));

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public Boolean uploadFile(String serverURL, File file, final int type) {

        loading = true;

        showpDialog();

        final OkHttpClient client = new OkHttpClient();

        try {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                    loading = false;

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            switch (type) {

                                case 0: {

                                    profile.setLowPhotoUrl(result.getString("lowPhotoUrl"));
                                    profile.setBigPhotoUrl(result.getString("bigPhotoUrl"));
                                    profile.setNormalPhotoUrl(result.getString("normalPhotoUrl"));

                                    App.getInstance().setPhotoUrl(result.getString("lowPhotoUrl"));

                                    break;
                                }

                                default: {

                                    profile.setNormalCoverUrl(result.getString("normalCoverUrl"));

                                    App.getInstance().setCoverUrl(result.getString("normalCoverUrl"));

                                    break;
                                }
                            }
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + response.body().string() + "\"");

                    } finally {

                        loading = false;

                        hidepDialog();

                        getData();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});

        } else {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
}