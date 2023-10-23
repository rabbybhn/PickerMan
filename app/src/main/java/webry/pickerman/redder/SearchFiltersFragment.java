package webry.pickerman.redder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Category;

public class SearchFiltersFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private static final int SELECT_LOCATION = 1;

    Button mSelectLocationButton, mSetFiltersButton;

    Spinner mChoiceCategory, mChoiceSortType, mChoiceModerationType;
    AppCompatSeekBar mChoiceDistance;

    ImageView mCategoryStatusCheckbox, mSortTypeStatusCheckbox, mModerationTypeStatusCheckbox, mLocationStatusCheckbox, mDistanceStatusCheckbox;

    TextView mTextViewDistance, mResetFiltersButton;

    LinearLayout mModerationContainer, mDistanceContainer;

    double lat = 0.0, lng = 0.0;

    private int sortType = 0, spinnerCategoryIndex = 0, moderationType = 0, distance = 0;
    private int categoryId = 0;
    private String geolocation;

    String postArea, postCountry, postCity;

    private FirebaseAnalytics mFirebaseAnalytics;

    public SearchFiltersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

//            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
//            itemsAdapter = new NotificationsListAdapter(getActivity(), itemsList);
//
//            restore = savedInstanceState.getBoolean("restore");
//            itemId = savedInstanceState.getInt("itemId");

        } else {

            categoryId = App.getInstance().getSearchFilters().getCategoryId();
            sortType = App.getInstance().getSearchFilters().getSortType();
            moderationType = App.getInstance().getSearchFilters().getModerationType();
            distance = App.getInstance().getSearchFilters().getDistance();

            geolocation = App.getInstance().getSearchFilters().getLocation();

            lat = App.getInstance().getSearchFilters().getLat();
            lng = App.getInstance().getSearchFilters().getLng();
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        setRetainInstance(true);

        Intent i = getActivity().getIntent();

        Bundle params = new Bundle();
        params.putString("action", "select_search_filters");
        params.putString("fragment", "SearchFiltersFragment");
        mFirebaseAnalytics.logEvent("app_open_fragment", params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search_filters, container, false);

        mModerationContainer = (LinearLayout) rootView.findViewById(R.id.moderation_container);
        mDistanceContainer = (LinearLayout) rootView.findViewById(R.id.distance_container);

        mCategoryStatusCheckbox = (ImageView) rootView.findViewById(R.id.category_checkbox_status);
        mSortTypeStatusCheckbox = (ImageView) rootView.findViewById(R.id.sort_type_checkbox_status);
        mModerationTypeStatusCheckbox = (ImageView) rootView.findViewById(R.id.moderation_type_checkbox_status);
        mLocationStatusCheckbox = (ImageView) rootView.findViewById(R.id.location_checkbox_status);
        mDistanceStatusCheckbox = (ImageView) rootView.findViewById(R.id.distance_checkbox_status);

        mChoiceCategory = (Spinner) rootView.findViewById(R.id.choiceCategory);
        mChoiceSortType = (Spinner) rootView.findViewById(R.id.choiceSortType);
        mChoiceModerationType = (Spinner) rootView.findViewById(R.id.choiceModeration);
        mChoiceDistance = (AppCompatSeekBar) rootView.findViewById(R.id.choiceDistance);

        mTextViewDistance = (TextView) rootView.findViewById(R.id.textViewDistance);

        mSelectLocationButton = (Button) rootView.findViewById(R.id.selectLocationButton);
        mSetFiltersButton = (Button) rootView.findViewById(R.id.setFiltersButton);

        mResetFiltersButton = (TextView) rootView.findViewById(R.id.resetFiltersButton);

        mSetFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setFilters();
            }
        });

        mResetFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetFilters();
            }
        });

        mSelectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lat == 0.000000 || lng == 0.000000) {

                    double t_lat = 37.773972, t_lng = -122.431297; // San Francisco, CA, USA

                    if (App.getInstance().getLat() != 0.000000) {

                        t_lat = App.getInstance().getLat();
                    }

                    if (App.getInstance().getLng() != 0.000000) {

                        t_lng = App.getInstance().getLng();
                    }

                    Intent i = new Intent(getActivity(), SelectLocationActivity.class);
                    i.putExtra("lat", t_lat);
                    i.putExtra("lng", t_lng);
                    startActivityForResult(i, SELECT_LOCATION);

                } else {

                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
                    arrayAdapter.add(getString(R.string.action_search_filters_delete_geo));
                    arrayAdapter.add(getString(R.string.action_search_filters_select_geo));

                    builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {

                                case 0: {

                                    lat = 0.000000;
                                    lng = 0.000000;

                                    geolocation = "";

                                    postArea = "";
                                    postCountry = "";
                                    postCity = "";

                                    updateLocation();
                                    updateView();

                                    break;
                                }

                                default: {

                                    Intent i = new Intent(getActivity(), SelectLocationActivity.class);
                                    i.putExtra("lat", lat);
                                    i.putExtra("lng", lng);
                                    startActivityForResult(i, SELECT_LOCATION);

                                    break;
                                }
                            }

                        }
                    });

                    AlertDialog d = builderSingle.create();
                    d.show();
                }
            }
        });

        // Initialize Spinner | add categories list

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceCategory.setAdapter(spinnerAdapter);

        spinnerAdapter.add(getString(R.string.moderation_type_all));

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

                    categoryId = category.getId();

                } else {

                    categoryId = 0;
                }

                spinnerCategoryIndex = mChoiceCategory.getSelectedItemPosition();

                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Initialize Sort Type Spinner

        ArrayAdapter<String> sortTypeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        sortTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceSortType.setAdapter(sortTypeSpinnerAdapter);

        for (int i = 0; i < getResources().getStringArray(R.array.sortTypeItems).length; i++) {

            sortTypeSpinnerAdapter.add(getResources().getStringArray(R.array.sortTypeItems)[i]);
        }

        sortTypeSpinnerAdapter.notifyDataSetChanged();

        mChoiceSortType.setSelection(sortType);

        mChoiceSortType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                sortType = mChoiceSortType.getSelectedItemPosition();

                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Initialize Moderation Type Spinner

        ArrayAdapter<String> moderationTypeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        moderationTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceModerationType.setAdapter(moderationTypeSpinnerAdapter);

        for (int i = 0; i < getResources().getStringArray(R.array.moderationTypeItems).length; i++) {

            moderationTypeSpinnerAdapter.add(getResources().getStringArray(R.array.moderationTypeItems)[i]);
        }

        moderationTypeSpinnerAdapter.notifyDataSetChanged();

        mChoiceModerationType.setSelection(moderationType);

        mChoiceModerationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                moderationType = mChoiceModerationType.getSelectedItemPosition();

                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Distance Slider

        mChoiceDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                distance = progress;

                updateView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Update View

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateView() {

        mChoiceCategory.setSelection(spinnerCategoryIndex);
        mChoiceSortType.setSelection(sortType);
        mChoiceModerationType.setSelection(moderationType);

        if (mChoiceDistance != null) {

            mChoiceDistance.setProgress(distance);
        }

        mTextViewDistance.setText(getString(R.string.label_select_distance) + " (" + String.format(Locale.getDefault(), "%d", distance + 5) + ")");

        if (geolocation.length() > 0) {

            mSelectLocationButton.setText(geolocation);

        } else {

            mSelectLocationButton.setText(getString(R.string.label_item_location_placeholder));
        }

        if (lat == 0.000000 || lng == 0.000000) {

            mDistanceContainer.setVisibility(View.GONE);

        } else {

            mDistanceContainer.setVisibility(View.VISIBLE);
        }

        Boolean moderation_changed = false;

        if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT && moderationType != 1) {

            moderation_changed = true;
        }

        if (!SHOW_ONLY_MODERATED_ADS_BY_DEFAULT && moderationType != 0) {

            moderation_changed = true;
        }

        if (sortType != 0 || categoryId != 0 || lat != 0.000000 || lng != 0.000000 || moderation_changed) {

            mResetFiltersButton.setVisibility(View.VISIBLE);

        } else {

            mResetFiltersButton.setVisibility(View.GONE);
        }

        updateStatusCheckboxes();
    }

    public void updateStatusCheckboxes() {

        if (spinnerCategoryIndex != 0) {

            mCategoryStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mCategoryStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (sortType != 0) {

            mSortTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mSortTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (moderationType != 0) {

            mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
        }

        if (lat != 0.000000 || lng != 0.000000) {

            mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

        } else {

            mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
        }

        mDistanceStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);
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

            geolocation = location;

            mSelectLocationButton.setText(geolocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    private void resetFilters() {

        categoryId = 0;
        spinnerCategoryIndex = 0;
        sortType = 0;
        distance = 25;
        geolocation = "";
        postCity = "";
        postCountry = "";
        postArea = "";
        lat = 0.000000;
        lng = 0.000000;

        if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT) {

            moderationType = 1;

        } else {

            moderationType = 0;
        }

        updateLocation();
        updateView();

        Toast.makeText(getActivity(), getString(R.string.msg_filters_reset_success), Toast.LENGTH_SHORT).show();
    }

    private void setFilters() {

        App.getInstance().getSearchFilters().setCategoryId(categoryId);
        App.getInstance().getSearchFilters().setSortType(sortType);
        App.getInstance().getSearchFilters().setModerationType(moderationType);
        App.getInstance().getSearchFilters().setDistance(distance);
        App.getInstance().getSearchFilters().setLocation(geolocation);

        App.getInstance().getSearchFilters().setLat(lat);
        App.getInstance().getSearchFilters().setLng(lng);

        App.getInstance().saveSearchFilters();

        Intent i = new Intent();

        getActivity().setResult(RESULT_OK, i);

        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_LOCATION && resultCode == getActivity().RESULT_OK && null != data) {

            lat = data.getDoubleExtra("lat", 0.000000);
            lng = data.getDoubleExtra("lng", 0.000000);

            postCountry =  data.getStringExtra("countryName");
            postArea =  data.getStringExtra("stateName");
            postCity =  data.getStringExtra("cityName");

            updateLocation();
            updateView();
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
}