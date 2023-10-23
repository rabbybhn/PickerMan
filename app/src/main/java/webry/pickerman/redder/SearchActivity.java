package webry.pickerman.redder;


import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import webry.pickerman.redder.common.ActivityBase;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;
import webry.pickerman.redder.view.SpacingItemDecoration;

public class SearchActivity extends ActivityBase implements Constants {

    private static final String STATE_LIST = "State Adapter Data";
    private static final String STATE_LIST_2 = "State Adapter Data 2";

    Toolbar mToolbar;

    private ProgressBar mProgressBar;

    private ActionBar mActionBar;
    private EditText mSearchBox;
    private RecyclerView mRecyclerView;
    private ImageButton mClearButton;
    private View mParentView;
    private SwipeRefreshLayout mSwipeRefresh;

    private Button mGetFiltersButton;

    private TextView mStatusMessage, mItemsCountLabel;
    private ImageView mStatusImage;

    private ArrayList<Item> itemsList;
    private ProductListAdapter itemsAdapter;

    private Parcelable mListState;

    private int pageId = 0, itemsCount = 0;
    private int arrayLength = 0;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;
    private Boolean restore = false;

    private String query = "";

    int pastVisiblesItems = 0, visibleItemCount = 0, totalItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        Intent i = getIntent();

        query = i.getStringExtra("query");

        mStatusImage = (ImageView) findViewById(R.id.search_status_img);
        mStatusMessage = (TextView) findViewById(R.id.search_status_message);

        mItemsCountLabel = (TextView) findViewById(R.id.items_count_label);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST_2);
            itemsAdapter = new ProductListAdapter(this, itemsList);

            restore = savedInstanceState.getBoolean("restore");
            pageId = savedInstanceState.getInt("pageId");
            itemsCount = savedInstanceState.getInt("itemsCount");
            query = savedInstanceState.getString("query");

            viewMore = savedInstanceState.getBoolean("viewMore");

        } else {

            itemsList = new ArrayList<>();
            itemsAdapter = new ProductListAdapter(this, itemsList);

            restore = false;
            pageId = 0;
            itemsCount = 0;
        }

        initComponent();
        setupToolbar();

        if (mRecyclerView.getAdapter().getItemCount() == 0) {

            if (query.length() == 0) {

                showStatusSearchMessage();

            } else {

                showStatusNoResultsMessage();
            }

        } else {

            hideStatusSearchMessage();
        }

        updateView();

        if (!restore) {

            search();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("viewMore", viewMore);

        outState.putBoolean("restore", true);
        outState.putInt("pageId", pageId);
        outState.putInt("itemsCount", itemsCount);
        outState.putString("query", query);

        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STATE_LIST, mListState);

        outState.putParcelableArrayList(STATE_LIST_2, itemsList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateView() {

        if (App.getInstance().getSearchFilters().getSortType() != 0 || App.getInstance().getSearchFilters().getCategoryId() != 0 || App.getInstance().getSearchFilters().getModerationType() != 0 || App.getInstance().getSearchFilters().getLat() != 0.000000 || App.getInstance().getSearchFilters().getLng() != 0.000000) {

            mGetFiltersButton.setText(getString(R.string.label_filters) + ": " + getString(R.string.label_filters_set));

        } else {

            mGetFiltersButton.setText(getString(R.string.label_filters) + ": " + getString(R.string.label_filters_default));
        }

        if (itemsCount != 0) {

            mItemsCountLabel.setText(getString(R.string.label_search_results) + " " + String.format(Locale.getDefault(), "%d", itemsCount));

        } else {

            mItemsCountLabel.setText(" ");
        }
    }

    private void initComponent() {

        mParentView = findViewById(android.R.id.content);

        mGetFiltersButton = (Button) findViewById(R.id.getFiltersButton);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);

        // Progress Bar

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Search Box

        mSearchBox = (EditText) findViewById(R.id.search_box);

        mSearchBox.setText(query);
        mSearchBox.addTextChangedListener(textWatcher);
        mSearchBox.setCursorVisible(false);
        mSearchBox.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        mSearchBox.clearFocus();

        mSearchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {

                    //got focus

                    mSearchBox.setCursorVisible(true);

                } else {

                    mSearchBox.setCursorVisible(false);
                    mSearchBox.clearFocus();

                    hideKeyboard();
                }
            }
        });

        // Clear Button

        mClearButton = (ImageButton) findViewById(R.id.button_clear);
        mClearButton.setVisibility(View.GONE);

        updateClearButton(query);

        // Recycler View

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final GridLayoutManager mLayoutManager = new GridLayoutManager(SearchActivity.this, Helper.getGridSpanCount(SearchActivity.this));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new SpacingItemDecoration(2, Helper.dpToPx(SearchActivity.this, 4), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Item item, int position) {

                Intent intent = new Intent(SearchActivity.this, ViewItemActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {

                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loadingMore) {

                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && (viewMore) && !(mSwipeRefresh.isRefreshing())) {

                            loadingMore = true;
                            Log.e("...", "Last Item Wow !");

                            search();
                        }
                    }
                }
            }
        });

        mGetFiltersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SearchActivity.this, SearchFiltersActivity.class);
                startActivityForResult(intent, 100);
            }
        });


        mClearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mSearchBox.setText("");

                //mSearchBox.setFocusableInTouchMode(true);
                //mSearchBox.requestFocus();

                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);

                updateClearButton("");
            }
        });

        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    mSearchBox.clearFocus();
                    mSearchBox.setCursorVisible(false);

                    hideKeyboard();

                    searchAction();

                    return true;
                }
                return false;
            }
        });

        // on swipe list
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                if (App.getInstance().isConnected()) {

                    if (!query.equals("")) {

                        pageId = 0;
                        itemsCount = 0;

                        search();

                    } else {

                        mSwipeRefresh.setRefreshing(false);
                    }

                } else {

                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });

//        showNoItemView(true);
    }

    private void setupToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void searchAction() {

        query = mSearchBox.getText().toString().trim();

        if (!query.equals("")) {

            itemsCount = 0;
            pageId = 0;

            search();
        }
    }

    public void search() {

        if (mRecyclerView.getAdapter().getItemCount() != 0) {

            mSwipeRefresh.setRefreshing(true);

        } else {

            showStatusLoadingMessage();
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_FINDER_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("itemsCount")) {

                                    if (pageId == 0) {

                                        itemsCount = response.getInt("itemsCount");
                                    }
                                }

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

                            Log.e("Response", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loadingComplete();

                Log.e("ERROR", "SearchActivity Error");
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("clientId", Constants.CLIENT_ID);

                params.put("page", "search");

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("query", query);
                params.put("pageId", Integer.toString(pageId));

                params.put("sortType", Integer.toString(App.getInstance().getSearchFilters().getSortType()));
                params.put("categoryId", Integer.toString(App.getInstance().getSearchFilters().getCategoryId()));
                params.put("moderationType", Integer.toString(App.getInstance().getSearchFilters().getModerationType()));
                params.put("distance", Integer.toString(App.getInstance().getSearchFilters().getDistance() + 5));

                params.put("lat", Double.toString(App.getInstance().getSearchFilters().getLat()));
                params.put("lng", Double.toString(App.getInstance().getSearchFilters().getLng()));

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

        loadingMore = false;

        mSwipeRefresh.setRefreshing(false);

        if (mRecyclerView.getAdapter().getItemCount() == 0) {

            showStatusNoResultsMessage();
            itemsCount = 0;

        } else {

            hideKeyboard();
            hideStatusSearchMessage();

//            hideMessage();
//            mHeaderContainer.setVisibility(View.VISIBLE);

//            mHeaderText.setText(getText(R.string.label_search_results) + " " + Integer.toString(itemCount));
        }

        updateView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && null != data) {

            searchAction();
            updateView();
        }
    }

    private void updateClearButton(String s) {

        if (s.trim().length() == 0) {

            mClearButton.setVisibility(View.GONE);

        } else {

            mClearButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {

        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hideStatusSearchMessage() {

        mStatusImage.setVisibility(View.GONE);
        mStatusMessage.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showStatusSearchMessage() {

        mStatusImage.setImageResource(R.drawable.ic_search_screen_search);
        mStatusMessage.setText(R.string.search_page_search_message);

        mStatusImage.setVisibility(View.VISIBLE);
        mStatusMessage.setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.GONE);
    }

    private void showStatusNoResultsMessage() {

        mStatusImage.setImageResource(R.drawable.ic_search_screen_noresults);
        mStatusMessage.setText(R.string.search_page_noresults_message);

        mStatusImage.setVisibility(View.VISIBLE);
        mStatusMessage.setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.GONE);
    }

    private void showStatusLoadingMessage() {

        mStatusImage.setVisibility(View.GONE);
        mStatusMessage.setVisibility(View.GONE);

        mProgressBar.setVisibility(View.VISIBLE);
    }

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {

            updateClearButton(c.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            View v = getCurrentFocus();

            if (v instanceof EditText) {

                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {

                    v.clearFocus();

                    mSearchBox.clearFocus();
                    mSearchBox.setCursorVisible(false);

                    hideKeyboard();

//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }
}