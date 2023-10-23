package webry.pickerman.redder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;
import webry.pickerman.redder.view.SquareImageView;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Category;
import webry.pickerman.redder.model.ImageItem;

public class NewItemFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private static final int SELECT_LOCATION = 1;

    private ProgressDialog pDialog;

    ArrayList<ImageItem> images;

    ScrollView mScrollView;
    LinearLayout mCategoryContainer, mSubcategoryContainer, mPhotosContainer, mLocationContainer, mTitleContainer, mPriceContainer;

    EmojiconEditText mItemEdit;
    EditText mItemTitle, mItemPrice, mItemPhoneNumber;
    SquareImageView mChoiceItemImg, mChoiceItemImg2, mChoiceItemImg3, mChoiceItemImg4, mChoiceItemImg5;
    CheckBox mAllowComments;

    TextView mCategoryErrorLabel, mSubcategoryErrorLabel, mPhotoErrorLabel, mLocationErrorLabel, mTitleErrorLabel, mPriceErrorLabel, mDescriptionErrorLabel, mPhoneNumberErrorLabel;

    Button mItemLocation, mItemPublish;

    Spinner mChoiceCategory, mChoiceSubcategory, mChoiceCurrency;

    ImageView mSubcategoryCheckboxStatus, mPhotoCheckboxStatus, mTitleCheckboxStatus, mPriceCheckboxStatus, mDescriptionCheckboxStatus, mLocationCheckboxStatus, mCategoryCheckboxStatus, mPhoneNumberCheckboxStatus;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;

    double lat = 0.0, lng = 0.0;

    String title = "", description = "", postArea = "", postCountry = "", postCity = "";

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private int mode = 0;
    private long itemId = 0;

    private int price = 1, allowComments = 0, btn_number = 0, currency = 0;
    private String phoneNumber = "";

    private int categoryId = 0, subcategoryId = 0;
    private int spinnerCategoryIndex = 0, spinnerSubcategoryIndex = 0;

    private Boolean loading = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    public NewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());

        setRetainInstance(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        mode = i.getIntExtra("mode", 0);

        if (mode == MODE_EDIT) {

            Bundle params = new Bundle();
            params.putString("action", "edit");
            params.putString("fragment", "NewItemFragment");
            mFirebaseAnalytics.logEvent("app_open_fragment", params);

            images = i.getParcelableArrayListExtra("images");

            categoryId = i.getIntExtra("categoryId", 0);
            subcategoryId = i.getIntExtra("subcategoryId", 0);

            itemId = i.getLongExtra("itemId", 0);
            price = i.getIntExtra("itemPrice", 0);
            currency = i.getIntExtra("itemCurrency", 0);

            description = i.getStringExtra("itemDescription");
            title = i.getStringExtra("itemTitle");

            postCity = i.getStringExtra("itemCity");
            postCountry = i.getStringExtra("itemCountry");
            postArea = i.getStringExtra("itemArea");

            phoneNumber = i.getStringExtra("phoneNumber");

            allowComments = i.getIntExtra("itemAllowComments", 0);

            lat = i.getDoubleExtra("lat", 0.000000);
            lng = i.getDoubleExtra("lng", 0.000000);

        } else {

            lat = App.getInstance().getNewItemLat();
            lng = App.getInstance().getNewItemLng();

            postCity = App.getInstance().getNewItemCity();
            postCountry = App.getInstance().getNewItemCountry();
            phoneNumber = App.getInstance().getNewItemPhone();

            Bundle params = new Bundle();
            params.putString("action", "new");
            params.putString("fragment", "NewItemFragment");
            mFirebaseAnalytics.logEvent("app_open_fragment", params);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_item, container, false);

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

                        images.add(new ImageItem(selectedImagePath));

                        updateImages();
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

                        images.add(new ImageItem(selectedImagePath));

                        updateImages();
                    }
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                addImage(btn_number);

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

                addImage(btn_number);

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

        mScrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        mCategoryContainer = (LinearLayout) rootView.findViewById(R.id.category_container);

        mSubcategoryContainer = (LinearLayout) rootView.findViewById(R.id.subcategory_container);
        mSubcategoryContainer.setVisibility(View.GONE);

        mPhotosContainer = (LinearLayout) rootView.findViewById(R.id.photos_container);
        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.location_container);
        mTitleContainer = (LinearLayout) rootView.findViewById(R.id.title_container);
        mPriceContainer = (LinearLayout) rootView.findViewById(R.id.price_container);

        mItemEdit = (EmojiconEditText) rootView.findViewById(R.id.itemDescription);
        mItemTitle = (EditText) rootView.findViewById(R.id.itemTitle);
        mItemPrice = (EditText) rootView.findViewById(R.id.itemPrice);
        mItemPhoneNumber = (EditText) rootView.findViewById(R.id.itemPhoneNumber);

        mAllowComments = (CheckBox) rootView.findViewById(R.id.allowComments);

        mChoiceCategory = (Spinner) rootView.findViewById(R.id.choiceCategory);
        mChoiceSubcategory = (Spinner) rootView.findViewById(R.id.choiceSubcategory);
        mChoiceCurrency = (Spinner) rootView.findViewById(R.id.choiceCurrency);

        mItemLocation = (Button) rootView.findViewById(R.id.itemLocation);
        mItemPublish = (Button) rootView.findViewById(R.id.itemPublish);

        mCategoryErrorLabel = (TextView) rootView.findViewById(R.id.categoryErrorLabel);
        mSubcategoryErrorLabel = (TextView) rootView.findViewById(R.id.subcategoryErrorLabel);
        mPhotoErrorLabel = (TextView) rootView.findViewById(R.id.photoErrorLabel);
        mLocationErrorLabel = (TextView) rootView.findViewById(R.id.locationErrorLabel);
        mTitleErrorLabel = (TextView) rootView.findViewById(R.id.titleErrorLabel);
        mPriceErrorLabel = (TextView) rootView.findViewById(R.id.priceErrorLabel);
        mDescriptionErrorLabel = (TextView) rootView.findViewById(R.id.descriptionErrorLabel);
        mPhoneNumberErrorLabel = (TextView) rootView.findViewById(R.id.phoneNumberErrorLabel);

        mChoiceItemImg = (SquareImageView) rootView.findViewById(R.id.choiceItemImg);
        mChoiceItemImg2 = (SquareImageView) rootView.findViewById(R.id.choiceItemImg2);
        mChoiceItemImg3 = (SquareImageView) rootView.findViewById(R.id.choiceItemImg3);
        mChoiceItemImg4 = (SquareImageView) rootView.findViewById(R.id.choiceItemImg4);
        mChoiceItemImg5 = (SquareImageView) rootView.findViewById(R.id.choiceItemImg5);

        mPhotoCheckboxStatus = (ImageView) rootView.findViewById(R.id.photo_checkbox_status);
        mTitleCheckboxStatus = (ImageView) rootView.findViewById(R.id.title_checkbox_status);
        mPriceCheckboxStatus = (ImageView) rootView.findViewById(R.id.price_checkbox_status);
        mDescriptionCheckboxStatus = (ImageView) rootView.findViewById(R.id.description_checkbox_status);
        mLocationCheckboxStatus = (ImageView) rootView.findViewById(R.id.location_checkbox_status);
        mCategoryCheckboxStatus = (ImageView) rootView.findViewById(R.id.category_checkbox_status);
        mSubcategoryCheckboxStatus = (ImageView) rootView.findViewById(R.id.subcategory_checkbox_status);
        mPhoneNumberCheckboxStatus = (ImageView) rootView.findViewById(R.id.phone_number_checkbox_status);

        if (images == null) {

            images = new ArrayList<ImageItem>();
        }

        if (price != 0 && currency > 2) {

            mItemPrice.setText(String.format(Locale.getDefault(), "%d", price));

        } else {

            mItemPrice.setText("");
        }

        mItemPrice.clearFocus();

        mItemTitle.setText(title);
        mItemTitle.clearFocus();
        mItemEdit.setText(description.replaceAll("<br>", "\n"));
        mItemEdit.clearFocus();

        mItemPhoneNumber.setText(phoneNumber);

        mAllowComments.setVisibility(View.GONE);

        setEditTextMaxLength(POST_CHARACTERS_LIMIT);

        mItemPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                publish();
            }
        });

        mChoiceItemImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (images.size() == 0) {

                    btn_number = 1;

                    addImage(0);

                } else {

                    deleteImage(0);
                }
            }
        });

        mChoiceItemImg2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (images.size() < 2) {

                    btn_number = 2;

                    addImage(1);

                } else {

                    deleteImage(1);
                }
            }
        });

        mChoiceItemImg3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (images.size() < 3) {

                    btn_number = 3;

                    addImage(2);

                } else {

                    deleteImage(2);
                }
            }
        });

        mChoiceItemImg4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (images.size() < 4) {

                    btn_number = 4;

                    addImage(3);

                } else {

                    deleteImage(3);

                }
            }
        });

        mChoiceItemImg5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (images.size() < 5) {

                    btn_number = 5;

                    addImage(4);

                } else {

                    deleteImage(4);

                }
            }
        });

        mItemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lat == 0.0 || lng == 0.0) {

                    // Set by Default | San Francisco, CA, USA

                    lat = 37.773972;
                    lng = -122.431297;

                    if (App.getInstance().getLat() == 0.0 || App.getInstance().getLng() == 0.0) {

                        // If Flow Filters has geo location data

                        if (App.getInstance().getFlowFilters().getLat() != 0.000000) {

                            lat = App.getInstance().getFlowFilters().getLat();
                        }

                        if (App.getInstance().getFlowFilters().getLng() != 0.000000) {

                            lng = App.getInstance().getFlowFilters().getLng();
                        }


                    } else {

                        // If App has geo location data

                        if (App.getInstance().getLat() != 0.000000) {

                            lat = App.getInstance().getLat();
                        }

                        if (App.getInstance().getLng() != 0.000000) {

                            lng = App.getInstance().getLng();
                        }
                    }
                }

                Intent i = new Intent(getActivity(), SelectLocationActivity.class);
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);
                i.putExtra("action_new_item", true);
                startActivityForResult(i, SELECT_LOCATION);
            }
        });

        mItemEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                updateStatusCheckboxes();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                updateCharactersCount();
            }

        });

        mItemEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (isAdded()) {

                    if (!hasFocus) {

                        setMode(mode);

                    } else {

                        updateCharactersCount();
                    }
                }
            }
        });

        mItemTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                updateStatusCheckboxes();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setMode(mode);
            }

        });

        mItemPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                updateStatusCheckboxes();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setMode(mode);
            }

        });

        mItemPhoneNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                updateStatusCheckboxes();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                setMode(mode);
            }

        });

        // Initialize Spinner | add categories list

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceCategory.setAdapter(spinnerAdapter);

        spinnerAdapter.add(getString(R.string.label_choice_category));

        for (int i = 0; i < App.getInstance().getCategoriesList().size(); i++) {

            Category category = App.getInstance().getCategoriesList().get(i);

            spinnerAdapter.add(category.getTitle());

            if (categoryId == category.getId()) {

                spinnerCategoryIndex = i + 1;
            }
        }

        spinnerAdapter.notifyDataSetChanged();

        mChoiceCategory.setSelection(spinnerCategoryIndex);

        mChoiceCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (mChoiceCategory.getSelectedItemPosition() != 0) {

                    Category category = App.getInstance().getCategoriesList().get(mChoiceCategory.getSelectedItemPosition() - 1);

                    if (categoryId != category.getId()) {

                        subcategoryId = 0;
                        spinnerSubcategoryIndex = 0;
                    }

                    categoryId = category.getId();

                } else {

                    categoryId = 0;
                    subcategoryId = 0;
                    spinnerSubcategoryIndex = 0;
                }

                spinnerCategoryIndex = mChoiceCategory.getSelectedItemPosition();

                updateSubcategories();

                updateStatusCheckboxes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Initialize Spinner | add subcategories list

        updateSubcategories();

        mChoiceSubcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (mChoiceSubcategory.getSelectedItemPosition() != 0) {

                    Category category = App.getInstance().getSubcategoriesList(categoryId).get(mChoiceSubcategory.getSelectedItemPosition() - 1);

                    subcategoryId = category.getId();

                } else {

                    subcategoryId = 0;
                }

                spinnerSubcategoryIndex = mChoiceSubcategory.getSelectedItemPosition();

                updateStatusCheckboxes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Initialize Currency Spinner | add currency list

        ArrayAdapter<String> currencySpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        currencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceCurrency.setAdapter(currencySpinnerAdapter);

        currencySpinnerAdapter.add(getString(R.string.currency_select));
        currencySpinnerAdapter.add(getString(R.string.currency_free));
        currencySpinnerAdapter.add(getString(R.string.currency_contractual));

        for (int i = 0; i < App.getInstance().getCurrencyList().size() - 1; i++) {

            currencySpinnerAdapter.add(App.getInstance().getCurrencyList().get(i).getCode() + " (" +App.getInstance().getCurrencyList().get(i).getName() + ")");
        }

        currencySpinnerAdapter.notifyDataSetChanged();

        mChoiceCurrency.setSelection(currency);

        mChoiceCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                currency = mChoiceCurrency.getSelectedItemPosition();

                updateStatusCheckboxes();

                updateView();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setMode(mode);

        // Update View

        updateImages();
        updateLocation();
        updateStatusCheckboxes();
        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateCharactersCount() {

        int cnt = mItemEdit.getText().toString().trim().length();

        if (cnt == 0) {

            setMode(mode);

        } else {

            getActivity().setTitle(String.format(Locale.getDefault(), "%d", NEW_ITEM_DESCRIPTION_CHARACTERS_LIMIT - cnt));
        }
    }

    public void updateSubcategories() {

        if (categoryId != 0) {

            ArrayAdapter<String> subcategoriesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
            subcategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mChoiceSubcategory.setAdapter(subcategoriesAdapter);

            subcategoriesAdapter.add(getString(R.string.label_choice_subcategory));

            for (int i = 0; i < App.getInstance().getSubcategoriesList(categoryId).size(); i++) {

                Category category = App.getInstance().getSubcategoriesList(categoryId).get(i);

                subcategoriesAdapter.add(category.getTitle());

                if (subcategoryId == category.getId()) {

                    spinnerSubcategoryIndex = i + 1;
                }
            }

            subcategoriesAdapter.notifyDataSetChanged();

            mChoiceSubcategory.setSelection(spinnerSubcategoryIndex);

            mSubcategoryContainer.setVisibility(View.VISIBLE);

        } else {

            mSubcategoryContainer.setVisibility(View.GONE);
        }
    }

    public void setMode(int mode) {

        if (mode == MODE_NEW) {

            mItemPublish.setText(getString(R.string.action_publish));
            getActivity().setTitle(getText(R.string.title_activity_new_classified));

        } else {

            mItemPublish.setText(getString(R.string.action_save_item));
            getActivity().setTitle(getText(R.string.title_activity_edit_item));
        }
    }

    public void updateView() {

        if (currency < 3) {

            mItemPrice.setVisibility(View.GONE);
            mItemPrice.clearFocus();

        } else {

            mItemPrice.setVisibility(View.VISIBLE);
            mItemPrice.requestFocus();
        }
    }

    public void updateStatusCheckboxes() {

        mCategoryErrorLabel.setVisibility(View.GONE);
        mSubcategoryErrorLabel.setVisibility(View.GONE);
        mPhotoErrorLabel.setVisibility(View.GONE);
        mLocationErrorLabel.setVisibility(View.GONE);
        mTitleErrorLabel.setVisibility(View.GONE);
        mPriceErrorLabel.setVisibility(View.GONE);
        mDescriptionErrorLabel.setVisibility(View.GONE);
        mPhoneNumberErrorLabel.setVisibility(View.GONE);

        if (spinnerCategoryIndex > 0) {

            mCategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mCategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (spinnerSubcategoryIndex > 0) {

            mSubcategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mSubcategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (images.size() > 0) {

            mPhotoCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mPhotoCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (currency > 0 && currency < 3) {

            mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            if (currency != 0) {

                if (mItemPrice.getText().toString().trim().length() > 0) {

                    try {

                        price = Integer.parseInt(mItemPrice.getText().toString().trim());

                    } catch (NumberFormatException nfe) {

                        price = 0;
                    }

                    if (price > 0) {

                        mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

                    } else {

                        mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
                    }

                } else {

                    mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
                }

            } else {

                mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
            }
        }

        if (mItemTitle.getText().toString().trim().length() >= 5) {

            mTitleCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mTitleCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (mItemEdit.getText().toString().trim().length() >= 10) {

            mDescriptionCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mDescriptionCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (lat != 0.0 && lng != 0.0) {

            mLocationCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mLocationCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (mItemPhoneNumber.getText().toString().trim().length() > 0) {

            // error phone format

            Helper helper = new Helper();

            if (!helper.isValidPhoneNew(mItemPhoneNumber.getText().toString().trim())) {

                mPhoneNumberCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);

            } else {

                mPhoneNumberCheckboxStatus.setImageResource(R.drawable.ic_checkbox_green);
            }

        } else {

            mPhoneNumberCheckboxStatus.setImageResource(R.drawable.ic_checkbox_gray);
        }
    }

    public void updateLocation() {

        if (postCountry != null || postArea != null || postCity != null) {

            String location = "";

            if (postCountry != null && postCountry.length() > 0) {

                location = postCountry;
            }

            if (postArea != null && postArea.length() > 0) {

                location = location + ", " + postArea;
            }

            if (postCity != null && postCity.length() > 0) {

                location = location + ", " + postCity;
            }

            if (location.length() > 0) {

                mItemLocation.setText(location);

            } else {

                mItemLocation.setText(getString(R.string.label_item_location_placeholder));
            }
        }

        updateStatusCheckboxes();
    }

    public void updateImages() {

        mChoiceItemImg.setImageResource(R.drawable.ic_add_image);
        mChoiceItemImg2.setImageResource(R.drawable.ic_add_image);
        mChoiceItemImg3.setImageResource(R.drawable.ic_add_image);
        mChoiceItemImg4.setImageResource(R.drawable.ic_add_image);
        mChoiceItemImg5.setImageResource(R.drawable.ic_add_image);

        updateStatusCheckboxes();

        for (int i = 0; i < images.size(); i++) {

            switch (i) {

                case 0: {

                    if (images.get(i).getSelectedImageFileName() != null) {

                        mChoiceItemImg.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(images.get(i).getSelectedImageFileName())));

                    } else {

                        Glide.with(getActivity())
                                .load(images.get(i).getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        return false;
                                    }
                                })
                                .into(mChoiceItemImg);
                    }

                    break;
                }

                case 1: {

                    if (images.get(i).getSelectedImageFileName() != null) {

                        mChoiceItemImg2.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(images.get(i).getSelectedImageFileName())));

                    } else {

                        Glide.with(getActivity())
                                .load(images.get(i).getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        return false;
                                    }
                                })
                                .into(mChoiceItemImg2);
                    }

                    break;
                }

                case 2: {

                    if (images.get(i).getSelectedImageFileName() != null) {

                        mChoiceItemImg3.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(images.get(i).getSelectedImageFileName())));

                    } else {

                        Glide.with(getActivity())
                                .load(images.get(i).getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        return false;
                                    }
                                })
                                .into(mChoiceItemImg3);
                    }

                    break;
                }

                case 3: {

                    if (images.get(i).getSelectedImageFileName() != null) {

                        mChoiceItemImg4.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(images.get(i).getSelectedImageFileName())));

                    } else {

                        Glide.with(getActivity())
                                .load(images.get(i).getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        return false;
                                    }
                                })
                                .into(mChoiceItemImg4);
                    }

                    break;
                }

                case 4: {

                    if (images.get(i).getSelectedImageFileName() != null) {

                        mChoiceItemImg5.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(images.get(i).getSelectedImageFileName())));

                    } else {

                        Glide.with(getActivity())
                                .load(images.get(i).getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        return false;
                                    }
                                })
                                .into(mChoiceItemImg5);
                    }

                    break;
                }

                default: {

                    break;
                }
            }
        }
    }

    public void addImage(int index) {

        Helper helper = new Helper(getActivity());

        if (!helper.checkStoragePermission()) {

            requestStoragePermission();

        } else {

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
                            intent.setType("image/*");

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
    }

    public void deleteImage(final int index) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.action_remove));

        alertDialog.setMessage(getText(R.string.label_delete_img));
        alertDialog.setCancelable(true);

        alertDialog.setNeutralButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_remove), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (images.size() > index) {

                    images.remove(index);
                }

                updateImages();

                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    addImage(btn_number);

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showNoStoragePermissionSnackbar();
                    }
                }

                return;
            }
        }
    }

    public void showNoStoragePermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void setEditTextMaxLength(int length) {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        mItemEdit.setFilters(FilterArray);
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
    }

    private Boolean getError() {

        mItemTitle.clearFocus();
        mItemPrice.clearFocus();
        mItemEdit.clearFocus();
        mItemPhoneNumber.clearFocus();

        Boolean error = false;
        Boolean scroll = false;

        allowComments = 0;

        phoneNumber = mItemPhoneNumber.getText().toString().trim();

        description = mItemEdit.getText().toString().trim();

        title = mItemTitle.getText().toString().trim();

        // error category

        if (categoryId == 0) {

            error = true;

            mCategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mCategoryErrorLabel.setText(R.string.msg_choice_category);
            mCategoryErrorLabel.setVisibility(View.VISIBLE);

            mScrollView.post(new Runnable() {

                @Override
                public void run() {

                    mScrollView.smoothScrollTo(0, 0);
                }
            });

            scroll = true;
        }

        // error subcategory

        if (subcategoryId == 0) {

            error = true;

            mSubcategoryCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mSubcategoryErrorLabel.setText(R.string.msg_choice_subcategory);
            mSubcategoryErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {

                        mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight());
                    }
                });
            }
        }

        // error images

        if (images.size() < 1) {

            error = true;

            mPhotoCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mPhotoErrorLabel.setText(R.string.msg_item_select_img);
            mPhotoErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {

                        mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight() + mSubcategoryContainer.getHeight());
                    }
                });
            }
        }

        // error location

        if (lat == 0.0 && lng == 0.0) {

            error = true;

            mLocationCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mLocationErrorLabel.setText(R.string.msg_item_select_location);
            mLocationErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {

                        mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight() + mSubcategoryContainer.getHeight() + mPhotosContainer.getHeight());
                    }
                });
            }
        }

        // error price

        if (mItemPrice.getText().toString().trim().length() > 0) {

            try {

                price = Integer.parseInt(mItemPrice.getText().toString().trim());

            } catch (NumberFormatException nfe) {

                price = 0;
            }

        } else {

            price = 0;
        }

        if (currency != 0) {

            if (currency > 2 && price < 1) {

                error = true;

                mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

                mPriceErrorLabel.setText(R.string.msg_item_select_price);
                mPriceErrorLabel.setVisibility(View.VISIBLE);

                if (!scroll) {

                    scroll = true;

                    mScrollView.post(new Runnable() {

                        @Override
                        public void run() {

                            mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight() + mSubcategoryContainer.getHeight() + mPhotosContainer.getHeight() + mLocationContainer.getHeight());
                            mItemPrice.requestFocus();
                        }
                    });
                }
            }

        } else {

            error = true;

            mPriceCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mPriceErrorLabel.setText(R.string.msg_item_select_currency);
            mPriceErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {

                        mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight() + mSubcategoryContainer.getHeight() + mPhotosContainer.getHeight() + mLocationContainer.getHeight());
                    }
                });
            }
        }

        // error title

        if (title.length() < 5) {

            error = true;

            mTitleCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mTitleErrorLabel.setText(R.string.msg_item_select_title);
            mTitleErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {

                        mScrollView.smoothScrollTo(0, mCategoryContainer.getHeight() + mSubcategoryContainer.getHeight() + mPhotosContainer.getHeight() + mLocationContainer.getHeight() + mPriceContainer.getHeight());
                        mItemTitle.requestFocus();
                    }
                });
            }
        }

        // error description

        if (description.length() < 10) {

            error = true;

            mDescriptionCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mDescriptionErrorLabel.setText(R.string.msg_item_select_description);
            mDescriptionErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mItemEdit.requestFocus();
            }
        }

        // error phone number

        if (phoneNumber.length() < 1) {

            error = true;

            mPhoneNumberCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

            mPhoneNumberErrorLabel.setText(R.string.msg_item_select_phone);
            mPhoneNumberErrorLabel.setVisibility(View.VISIBLE);

            if (!scroll) {

                scroll = true;

                mItemPhoneNumber.requestFocus();
            }

        } else {

            // error phone format

            Helper helper = new Helper();

            if (!helper.isValidPhoneNew(phoneNumber)) {

                error = true;

                mPhoneNumberCheckboxStatus.setImageResource(R.drawable.ic_checkbox_alert);

                mPhoneNumberErrorLabel.setText(R.string.error_wrong_format);
                mPhoneNumberErrorLabel.setVisibility(View.VISIBLE);

                if (!scroll) {

                    scroll = true;

                    mItemPhoneNumber.requestFocus();
                }
            }
        }

        return error;
    }

    private void publish() {

        if (App.getInstance().isConnected()) {

            if (!getError()) {

                loading = true;

                if (currency < 3) price = 0;

                showpDialog();

                uploadFile(METHOD_ITEMS_UPLOAD_IMG, 0);
            }

        } else {

            Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_LOCATION && resultCode == getActivity().RESULT_OK && null != data) {

            lat =  data.getDoubleExtra("lat", 0.000000);
            lng =  data.getDoubleExtra("lng", 0.000000);

            postCountry =  data.getStringExtra("countryName");
            postArea =  data.getStringExtra("stateName");
            postCity =  data.getStringExtra("cityName");

            updateLocation();

        }
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri, String fileName) {

        InputStream is = null;

        if (uri.getAuthority() != null) {

            try {

                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                return writeToTempImageAndGetPathUri(context, bmp, fileName).toString();

            } catch (FileNotFoundException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (is != null) {

                        is.close();
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage, String fileName) {

        String file_path = Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER;
        File dir = new File(file_path);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        try {

            FileOutputStream fos = new FileOutputStream(file);

            inImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {

            Log.e("Image", "writeToTempImageAndGetPathUri: FileNotFoundException " + e.toString());

        } catch (IOException e) {

            e.printStackTrace();
        }

        return Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER + File.separator + fileName;
    }

    public void prepare() {

        if (images.size() > 1 && images.get(1).getImageUrl().length() == 0) {

            uploadFile(METHOD_ITEMS_UPLOAD_IMG, 1);

        } else if (images.size() > 2 && images.get(2).getImageUrl().length() == 0) {

            uploadFile(METHOD_ITEMS_UPLOAD_IMG, 2);

        } else if (images.size() > 3 && images.get(3).getImageUrl().length() == 0) {

            uploadFile(METHOD_ITEMS_UPLOAD_IMG, 3);

        } else if (images.size() > 4 && images.get(4).getImageUrl().length() == 0) {

            uploadFile(METHOD_ITEMS_UPLOAD_IMG, 4);

        } else {

            if (mode == MODE_NEW) {

                sendItem();

            } else {

                saveItem();
            }

        }
    }

    public void sendItem() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("itemId")) {

                                    itemId = response.getLong("itemId");
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendPostSuccess();

                            Log.d("sendItem Success", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendPostSuccess();

                Log.e("sendItem Error", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("clientId", CLIENT_ID);
                params.put("appType", Integer.toString(APP_TYPE_ANDROID));

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("categoryId", Integer.toString(categoryId));
                params.put("subcategoryId", Integer.toString(subcategoryId));
                params.put("price", Integer.toString(price));
                params.put("currency", Integer.toString(currency));
                params.put("allowComments", Integer.toString(allowComments));
                params.put("title", title);
                params.put("description", description);

                for (int i = 0; i < images.size(); i++) {

                    params.put("images[" + i + "]", images.get(i).getImageUrl());
                }

                params.put("postArea", postArea);
                params.put("postCountry", postCountry);
                params.put("postCity", postCity);
                params.put("postLat", String.format(Locale.getDefault(), "%f", lat));
                params.put("postLng", String.format(Locale.getDefault(), "%f", lng));

                params.put("phoneNumber", phoneNumber);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendPostSuccess() {

        if (App.getInstance().getLat() == 0.0 || App.getInstance().getLng() == 0.0) {

            if (lat != 37.773972 && lng != 122.431297) {

                App.getInstance().setLat(lat);
                App.getInstance().setLng(lng);
            }
        }

        // Save data for another new item

        App.getInstance().setNewItemLat(lat);
        App.getInstance().setNewItemLng(lng);

        App.getInstance().setNewItemCity(postCity);
        App.getInstance().setNewItemCountry(postCountry);
        App.getInstance().setNewItemPhone(phoneNumber);

        App.getInstance().saveData();

        // End save data

        loading = false;

        hidepDialog();

        Intent i = new Intent();
        i.putExtra("itemId", itemId);
        getActivity().setResult(RESULT_OK, i);

        if (itemId != 0) {

            Intent intent = new Intent(getActivity(), ViewItemActivity.class);
            intent.putExtra("itemId", itemId);
            intent.putExtra("inviteShare", true);
            startActivity(intent);

        } else {

            Toast.makeText(getActivity(), getText(R.string.msg_item_posted), Toast.LENGTH_SHORT).show();
        }

        getActivity().finish();
    }

    public void saveItem() {

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_EDIT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            saveItemSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                saveItemSuccess();

                Log.e(TAG, error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("categoryId", Integer.toString(categoryId));
                params.put("subcategoryId", Integer.toString(subcategoryId));
                params.put("price", Integer.toString(price));
                params.put("currency", Integer.toString(currency));
                params.put("allowComments", Integer.toString(allowComments));
                params.put("title", title);
                params.put("description", description);

                for (int i = 0; i < images.size(); i++) {

                    params.put("images[" + i + "]", images.get(i).getImageUrl());
                }

                params.put("postArea", postArea);
                params.put("postCountry", postCountry);
                params.put("postCity", postCity);

                params.put("postLat", String.format(Locale.getDefault(), "%f", lat));
                params.put("postLng", String.format(Locale.getDefault(), "%f", lng));

                params.put("phoneNumber", phoneNumber);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void saveItemSuccess() {

        loading = false;

        hidepDialog();

        Intent i = new Intent();

        i.putParcelableArrayListExtra("images", images);

        i.putExtra("categoryId", categoryId);
        i.putExtra("subcategoryId", subcategoryId);
        i.putExtra("categoryTitle", App.getInstance().getCategoriesList().get(categoryId - 1));
        i.putExtra("itemPrice", price);
        i.putExtra("itemCurrency", currency);
        i.putExtra("itemTitle", title);
        i.putExtra("itemDescription", description);

        i.putExtra("itemCity", postCity);
        i.putExtra("itemCountry", postCountry);
        i.putExtra("itemArea", postArea);

        i.putExtra("lat", lat);
        i.putExtra("lng", lng);

        i.putExtra("phoneNumber", phoneNumber);

        i.putExtra("itemAllowComments", allowComments);

        requireActivity().setResult(RESULT_OK, i);

        Toast.makeText(getActivity(), getText(R.string.msg_item_saved), Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    public void uploadFile(String serverURL, final int imgId) {

        if (images.get(imgId).getSelectedImageFileName() != null) {

            File file = new File(images.get(imgId).getSelectedImageFileName());

            final OkHttpClient client = new OkHttpClient();

            try {

                RequestBody requestBody = new MultipartBuilder()
                        .type(MultipartBuilder.FORM)
                        .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                        .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                        .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                        .build();

                com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                        .url(serverURL)
                        .addHeader("Accept", "application/json;")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                        loading = false;

                        hidepDialog();

                        Log.e(TAG, request.toString());
                    }

                    @Override
                    public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                        String jsonData = response.body().string();

                        Log.e(TAG, jsonData);

                        try {

                            JSONObject result = new JSONObject(jsonData);

                            if (!result.getBoolean("error")) {

                                images.get(imgId).setImageUrl(result.getString("imgUrl"));
                            }

                            Log.d(TAG, response.toString());

                        } catch (Throwable t) {

                            Log.e(TAG, "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                        } finally {

                            Log.e(TAG, jsonData);

                            prepare();
                        }
                    }
                });

            } catch (Exception ex) {
                // Handle the error

                loading = false;

                hidepDialog();
            }

        } else {

            prepare();
        }
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {

        super.onDetach();
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