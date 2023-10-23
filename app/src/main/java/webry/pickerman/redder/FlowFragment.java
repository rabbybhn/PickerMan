package webry.pickerman.redder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import webry.pickerman.redder.adapter.ProductListAdapter;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Category;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;
import webry.pickerman.redder.view.SpacingItemDecoration;

public class FlowFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final int SELECT_FILTERS = 2001;
    private static final int SELECT_LOCATION = 2002;

    private static final String STATE_LIST = "State Adapter Data";
    private static final String STATE_LIST_2 = "State Adapter Data";

    private CardView mFlowTooltip;
    private ImageButton mCloseTooltipButton;

    private TextView mMessage;
    private ImageView mSplash;

    RecyclerView mRecyclerView;

    SwipeRefreshLayout mItemsContainer;

    private ArrayList<Item> itemsList;
    private ProductListAdapter itemsAdapter;

    private Parcelable mListState;

    private int pageId = 0;
    private int arrayLength = 0;

    private String query = "";

    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;
    private Boolean isFabHide = false;

    private Boolean allowOpenItem = true;

    private AlertDialog d;
    private double lat = 0.00000;
    private double lng = 0.00000;
    private String geolocation = "";

    // Scrolling

    private int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;
    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    public FlowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST_2);
            itemsAdapter = new ProductListAdapter(getActivity(), itemsList);

            restore = savedInstanceState.getBoolean("restore");
            pageId = savedInstanceState.getInt("pageId");

            viewMore = savedInstanceState.getBoolean("viewMore");

            allowOpenItem = savedInstanceState.getBoolean("allowOpenItem");

            query = savedInstanceState.getString("query");

        } else {

            itemsList = new ArrayList<>();
            itemsAdapter = new ProductListAdapter(getActivity(), itemsList);

            restore = false;
            pageId = 0;

            query = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_flow, container, false);

        mItemsContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.container_items);
        mItemsContainer.setOnRefreshListener(this);

        mFlowTooltip = (CardView) rootView.findViewById(R.id.flow_tooltip);
        mCloseTooltipButton = (ImageButton) rootView.findViewById(R.id.close_tooltip_button);

        mMessage = (TextView) rootView.findViewById(R.id.message);
        mSplash = (ImageView) rootView.findViewById(R.id.splash);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        int columns = 2;

        if (isAdded()) columns = Helper.getGridSpanCount(getActivity());

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), columns);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SpacingItemDecoration(columns, Helper.dpToPx(getActivity(), 4), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mRecyclerView.setItemViewCacheSize(60);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Item item, int position) {

                // Need set "mrl_rippleDelayClick" to false

                if (allowOpenItem) {

                    allowOpenItem = false;

                    Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);

                    new Handler().postDelayed(new Runnable() {

                        public void run() {

                            allowOpenItem = true;
                        }

                    }, 200);

                }
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {

                    animateFab(true);
                    ((MainActivity)getActivity()).animateSearchBar(true);

                    controlsVisible = false;
                    scrolledDistance = 0;

                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {

                    animateFab(false);
                    ((MainActivity)getActivity()).animateSearchBar(false);

                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {

                    scrolledDistance += dy;
                }

                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loadingMore)
                    {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (viewMore) && !(mItemsContainer.isRefreshing()))
                        {
                            loadingMore = true;
                            Log.e("...", "Last Item Wow !");

                            getItems();
                        }
                    }
                }
            }
        });

//        mNestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//                if (scrollY < oldScrollY) { // up
//
//                    animateFab(false);
//                    ((MainActivity)getActivity()).animateSearchBar(false);
//                }
//
//                if (scrollY > oldScrollY) { // down
//
//                    animateFab(true);
//                    ((MainActivity)getActivity()).animateSearchBar(true);
//                }
//
//                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
//
//                    if (!loadingMore && (viewMore) && !(mItemsContainer.isRefreshing())) {
//
//                        mItemsContainer.setRefreshing(true);
//
//                        loadingMore = true;
//
//                        getItems();
//                    }
//                }
//            }
//        });

        ((MainActivity)getActivity()).mFabButton.setVisibility(View.VISIBLE);

        ((MainActivity)getActivity()).mFabButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() != 0) {

                    Intent intent = new Intent(getActivity(), NewItemActivity.class);
                    startActivityForResult(intent, STREAM_NEW_POST);

                } else {

                    Intent i = new Intent(getActivity(), LoginActivity.class);
                    i.putExtra("pageId", PAGE_FLOW);
                    startActivityForResult(i, ACTION_LOGIN);
                }


            }
        });

        mCloseTooltipButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                closeFlowTooltip();
            }
        });

        if (itemsAdapter.getItemCount() == 0) {

            showMessage(getText(R.string.label_empty_flow_list).toString());

        } else {

            hideMessage();
        }

        if (!restore) {

            showMessage(getText(R.string.msg_loading_2).toString());

            getItems();
        }

        initSearchBox();

        updateView();

        return rootView;
    }

    private void initSearchBox() {

        ((MainActivity)getActivity()).mSearchContainer.setVisibility(View.VISIBLE);

        ((MainActivity)getActivity()).mSearchBox.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        ((MainActivity)getActivity()).mSearchBox.setHint(getString(R.string.placeholder_flow_search_box));

        // ((MainActivity)getActivity()).mSearchBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_search, 0, 0, 0);

        updateClearButton(query);

        ((MainActivity)getActivity()).mSearchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ((MainActivity)getActivity()).mSearchBox.setText("");

                query = "";
            }
        });

        ((MainActivity)getActivity()).mSearchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                updateClearButton(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((MainActivity)getActivity()).mSearchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (isAdded()) {

                    if (hasFocus) {

                        //got focus

                        ((MainActivity)getActivity()).mSearchBox.setCursorVisible(true);

                        ((MainActivity)getActivity()).mFabButton.setVisibility(View.GONE);

                    } else {

                        ((MainActivity)getActivity()).mSearchBox.setCursorVisible(false);
                        ((MainActivity)getActivity()).mSearchBox.clearFocus();

                        // ((MainActivity)getActivity()).hideKeyboard();

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        new Handler().postDelayed(new Runnable() {

                            public void run() {

                                if (isAdded()) ((MainActivity)getActivity()).mFabButton.setVisibility(View.VISIBLE);
                            }

                        }, 200);

                    }
                }
            }
        });

        ((MainActivity)getActivity()).mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (isAdded()) {

                        ((MainActivity)getActivity()).mSearchBox.clearFocus();
                        ((MainActivity)getActivity()).hideKeyboard();

                        query = ((MainActivity)getActivity()).mSearchBox.getText().toString().trim();

                        if (!query.equals("")) {

                            Intent i = new Intent(getActivity(), SearchActivity.class);
                            i.putExtra("pageId", PAGE_SEARCH);
                            i.putExtra("query", query);
                            startActivityForResult(i, 1001);

                            query = "";

                            new Handler().postDelayed(new Runnable() {

                                public void run() {

                                    if (isAdded()) ((MainActivity)getActivity()).mSearchBox.setText(query);
                                }

                            }, 250);
                        }
                    }

                    return true;
                }

                return false;
            }
        });
    }

    private void updateClearButton(String s) {

        if (isAdded()) {

            if (s.trim().length() == 0) {

                ((MainActivity)getActivity()).mSearchClear.setVisibility(View.GONE);

            } else {

                ((MainActivity)getActivity()).mSearchClear.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateView() {

        if (App.getInstance().getTooltipsSettings().isAllowShowFlowTooltip()) {

            if (App.getInstance().getFlowFilters().getCurrency() != 0 || App.getInstance().getFlowFilters().getCategoryId() != 0 || App.getInstance().getFlowFilters().getLat() != 0.000000 || App.getInstance().getFlowFilters().getLng() != 0.000000) {

                closeFlowTooltip();

            } else {

                mFlowTooltip.setVisibility(View.VISIBLE);
            }

        } else {

            mFlowTooltip.setVisibility(View.GONE);
        }
    }

    private void closeFlowTooltip() {

        mFlowTooltip.setVisibility(View.GONE);

        App.getInstance().getTooltipsSettings().setShowFlowTooltip(false);
        App.getInstance().saveTooltipsSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);

        outState.putBoolean("allowOpenItem", allowOpenItem);

        outState.putBoolean("restore", true);
        outState.putInt("pageId", pageId);

        outState.putString("query", query);

        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STATE_LIST, mListState);

        outState.putParcelableArrayList(STATE_LIST_2, itemsList);
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            pageId = 0;
            getItems();

        } else {

            mItemsContainer.setRefreshing(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STREAM_NEW_POST && resultCode == getActivity().RESULT_OK && null != data) {

            long itemId = data.getLongExtra("itemId", 0);

            if (itemId != 0) {

                getItem(itemId);
            }

        } else if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            Item item = itemsList.get(position);

            item.setContent(data.getStringExtra("post"));
            item.setImgUrl(data.getStringExtra("imgUrl"));

            itemsAdapter.notifyDataSetChanged();

        } else if (requestCode == ACTION_LOGIN && resultCode == getActivity().RESULT_OK && null != data) {

            int page = data.getIntExtra("pageId", PAGE_UNKNOWN);

            // if (page == PAGE_FLOW) ((MainActivity)getActivity()).displayFragment(((MainActivity)getActivity()).mNavMenu.findItem(R.id.nav_profile).getItemId(), ((MainActivity)getActivity()).mNavMenu.findItem(R.id.nav_profile).getTitle().toString());

        } else if (requestCode == SELECT_FILTERS && resultCode == getActivity().RESULT_OK && null != data) {

            updateView();

            pageId = 0;
            getItems();

        } else if (requestCode == SELECT_LOCATION && resultCode == getActivity().RESULT_OK && null != data) {

            String postArea, postCountry, postCity;

            lat = data.getDoubleExtra("lat", 0.000000);
            lng = data.getDoubleExtra("lng", 0.000000);

            postCountry =  data.getStringExtra("countryName");
            postArea =  data.getStringExtra("stateName");
            postCity =  data.getStringExtra("cityName");

            if (postCountry != null || postArea != null || postCity != null) {

                String location = App.getInstance().getFlowFilters().getLocation();

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
            }

            if (d != null) {

                if (d.isShowing()) {

                    Button mSelectLocationButton;
                    ImageView mLocationStatusCheckbox, mDistanceStatusCheckbox;
                    AppCompatSeekBar mChoiceDistance;

                    mSelectLocationButton = (Button) d.findViewById(R.id.selectLocationButton);
                    mLocationStatusCheckbox = (ImageView) d.findViewById(R.id.location_checkbox_status);
                    mDistanceStatusCheckbox = (ImageView) d.findViewById(R.id.distance_checkbox_status);
                    mChoiceDistance = (AppCompatSeekBar) d.findViewById(R.id.choiceDistance);

                    if (mSelectLocationButton != null) {

                        if (geolocation.length() != 0) {

                            mSelectLocationButton.setText(geolocation);

                        } else {

                            mSelectLocationButton.setText(getString(R.string.label_item_location_placeholder));
                        }
                    }

                    if (mLocationStatusCheckbox != null) mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);
                    if (mDistanceStatusCheckbox != null) mDistanceStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);
                    if (mChoiceDistance != null) mChoiceDistance.setEnabled(true);
                }
            }
        }
    }

    public void getItem(final long itemId) {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "FlowFragment Not Added to Activity");

                            return;
                        }

                        try {

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Item item = new Item(itemObj);

                                            itemsList.add(0, item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("Error", response.toString());

                            itemsAdapter.notifyDataSetChanged();
                            mItemsContainer.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "FlowFragment Not Added to Activity");

                    return;
                }

                mItemsContainer.setRefreshing(false);
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

    private void getFlowFilters() {

        int  spinnerCategoryIndex = 0;

        String sz_geo = "";

        sz_geo = App.getInstance().getFlowFilters().getLocation();

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(getText(R.string.title_activity_flow_filters));

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_flow_filters, null);

        b.setView(view);

        final LinearLayout mDistanceContainer = view.findViewById(R.id.distance_container);

        final ImageView mCategoryStatusCheckbox = view.findViewById(R.id.category_checkbox_status);
        final ImageView mCurrencyTypeStatusCheckbox = view.findViewById(R.id.currency_type_checkbox_status);
        final ImageView mLocationStatusCheckbox = view.findViewById(R.id.location_checkbox_status);
        final ImageView mDistanceStatusCheckbox = view.findViewById(R.id.distance_checkbox_status);
        final ImageView mModerationTypeStatusCheckbox = view.findViewById(R.id.moderation_type_checkbox_status);

        final Spinner mChoiceCategory = view.findViewById(R.id.choiceCategory);
        final Spinner mChoiceCurrencyType = view.findViewById(R.id.choiceCurrency);
        final Spinner mChoiceModerationType = view.findViewById(R.id.choiceModeration);
        final AppCompatSeekBar mChoiceDistance = view.findViewById(R.id.choiceDistance);

        final TextView mTextViewDistance = view.findViewById(R.id.textViewDistance);
        final TextView mResetFiltersButton = view.findViewById(R.id.resetFiltersButton);

        final Button mSelectLocationButton = view.findViewById(R.id.selectLocationButton);

        mResetFiltersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                geolocation = "";

                lat = 0.00000;
                lng = 0.00000;

                mChoiceCategory.setSelection(0);
                mChoiceCurrencyType.setSelection(0);

                mSelectLocationButton.setText(getString(R.string.label_item_location_placeholder));

                mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                mDistanceStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                mChoiceDistance.setProgress(25);
                mChoiceDistance.setEnabled(false);

                mTextViewDistance.setText(getString(R.string.label_select_distance) + " (" + String.format(Locale.getDefault(), "%d", mChoiceDistance.getProgress() + 5) + ")");

                if (SHOW_ONLY_MODERATED_ADS_BY_DEFAULT) {

                    mChoiceModerationType.setSelection(1);
                    mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

                } else {

                    mChoiceModerationType.setSelection(0);
                    mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                }
            }
        });

        if (App.getInstance().getFlowFilters().getLat() != 0.000000 || App.getInstance().getFlowFilters().getLng() != 0.000000) {

            mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);
            mDistanceStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);
            mChoiceDistance.setEnabled(true);

            lat = App.getInstance().getFlowFilters().getLat();
            lng = App.getInstance().getFlowFilters().getLng();

            geolocation = App.getInstance().getFlowFilters().getLocation();

        } else {

            mLocationStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
            mDistanceStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
            mChoiceDistance.setEnabled(false);
        }

        if (sz_geo.length() != 0) {

            mSelectLocationButton.setText(sz_geo);
        }

        mSelectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double t_lat = 37.773972, t_lng = -122.431297; // San Francisco, CA, USA

                if (lat == 0.000000 || lng == 0.000000) {

                    if (App.getInstance().getLat() != 0.000000) {

                        t_lat = App.getInstance().getLat();
                    }

                    if (App.getInstance().getLng() != 0.000000) {

                        t_lng = App.getInstance().getLng();
                    }

                } else {

                    t_lat = lat;
                    t_lng = lng;
                }

                Intent i = new Intent(getActivity(), SelectLocationActivity.class);
                i.putExtra("lat", t_lat);
                i.putExtra("lng", t_lng);
                startActivityForResult(i, SELECT_LOCATION);
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

            if (App.getInstance().getFlowFilters().getCategoryId() == category.getId()) {

                spinnerCategoryIndex = i + 1;
            }
        }

        spinnerAdapter.notifyDataSetChanged();

        mChoiceCategory.setSelection(spinnerCategoryIndex);

        mChoiceCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (mChoiceCategory.getSelectedItemPosition() != 0) {

                    mCategoryStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

                } else {

                    mCategoryStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Initialize Currency Type Spinner

        ArrayAdapter<String> currencyTypeSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        currencyTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChoiceCurrencyType.setAdapter(currencyTypeSpinnerAdapter);

        currencyTypeSpinnerAdapter.add(getString(R.string.currency_select));
        currencyTypeSpinnerAdapter.add(getString(R.string.currency_free));
        currencyTypeSpinnerAdapter.add(getString(R.string.currency_contractual));

        for (int i = 0; i < App.getInstance().getCurrencyList().size() - 1; i++) {

            currencyTypeSpinnerAdapter.add(App.getInstance().getCurrencyList().get(i).getCode() + " (" +App.getInstance().getCurrencyList().get(i).getName() + ")");
        }

        currencyTypeSpinnerAdapter.notifyDataSetChanged();

        mChoiceCurrencyType.setSelection(App.getInstance().getFlowFilters().getCurrency());

        mChoiceCurrencyType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (mChoiceCurrencyType.getSelectedItemPosition() != 0) {

                    mCurrencyTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

                } else {

                    mCurrencyTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                }
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

        mChoiceModerationType.setSelection(App.getInstance().getFlowFilters().getModerationType());

        mChoiceModerationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (mChoiceModerationType.getSelectedItemPosition() != 0) {

                    mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_green);

                } else {

                    mModerationTypeStatusCheckbox.setImageResource(R.drawable.ic_checkbox_gray);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Distance Slider

        mChoiceDistance.setProgress(App.getInstance().getFlowFilters().getDistance());
        mTextViewDistance.setText(getString(R.string.label_select_distance) + " (" + String.format(Locale.getDefault(), "%d", App.getInstance().getFlowFilters().getDistance() + 5) + ")");

        mChoiceDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mTextViewDistance.setText(getString(R.string.label_select_distance) + " (" + String.format(Locale.getDefault(), "%d", progress + 5) + ")");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        b.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (mChoiceCategory.getSelectedItemPosition() != 0) {

                    Category category = App.getInstance().getCategoriesList().get(mChoiceCategory.getSelectedItemPosition() - 1);

                    App.getInstance().getFlowFilters().setCategoryId(category.getId());

                } else {

                    App.getInstance().getFlowFilters().setCategoryId(0);
                }

                App.getInstance().getFlowFilters().setCurrency(mChoiceCurrencyType.getSelectedItemPosition());

                // get moderation type

                App.getInstance().getFlowFilters().setModerationType(mChoiceModerationType.getSelectedItemPosition());

                // get distance

                App.getInstance().getFlowFilters().setDistance(mChoiceDistance.getProgress());

                //

                App.getInstance().getFlowFilters().setLocation(geolocation);

                App.getInstance().getFlowFilters().setLat(lat);
                App.getInstance().getFlowFilters().setLng(lng);

                // save filters

                App.getInstance().saveFlowFilters();

                pageId = 0;

                getItems();
            }
        });

        b.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        d = b.create();

        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        d.show();
    }

    public void getItems() {

        mItemsContainer.setRefreshing(true);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FINDER_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "FlowFragment Not Added to Activity");

                            return;
                        }

                        if (!loadingMore) {

                            itemsList.clear();

                            itemsAdapter.notifyDataSetChanged();
                        }

                        try {

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

                                            itemsAdapter.notifyItemInserted(itemsList.size());
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

                    Log.e("ERROR", "FlowFragment Not Added to Activity");

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

                params.put("page", "flow");

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("pageId", Integer.toString(pageId));

                params.put("categoryId", Integer.toString(App.getInstance().getFlowFilters().getCategoryId()));
                params.put("currency", Integer.toString(App.getInstance().getFlowFilters().getCurrency()));
                params.put("moderationType", Integer.toString(App.getInstance().getFlowFilters().getModerationType()));
                params.put("distance", Integer.toString(App.getInstance().getFlowFilters().getDistance() + 5));

                params.put("lat", Double.toString(App.getInstance().getFlowFilters().getLat()));
                params.put("lng", Double.toString(App.getInstance().getFlowFilters().getLng()));

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

        if (itemsAdapter.getItemCount() == 0) {

            if (FlowFragment.this.isVisible()) {

                showMessage(getText(R.string.label_empty_flow_list).toString());
            }

        } else {

            hideMessage();
        }

        loadingMore = false;
        mItemsContainer.setRefreshing(false);
    }

    public void showMessage(String message) {

        mSplash.setVisibility(View.VISIBLE);

        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);
    }

    public void hideMessage() {

        mSplash.setVisibility(View.GONE);

        mMessage.setVisibility(View.GONE);
    }

    private void animateFab(final boolean hide) {

        if (isFabHide && hide || !isFabHide && !hide) return;

        isFabHide = hide;

        int moveY = hide ? (2 * ((MainActivity)getActivity()).mFabButton.getHeight()) : 0;

        ((MainActivity)getActivity()).mFabButton.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_flow, menu);

//        SearchView searchView = (SearchView)menu.findItem(R.id.options_menu_main_search).getActionView();
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//
//        View v = searchView.findViewById(mymarketplace.support.v7.appcompat.R.id.search_plate);
//        v.setBackgroundColor(Color.parseColor("#ffffff"));
//
//        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_flow_settings: {

//                Intent intent = new Intent(getActivity(), FlowFiltersActivity.class);
//                startActivityForResult(intent, SELECT_FILTERS);

                getFlowFilters();

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
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