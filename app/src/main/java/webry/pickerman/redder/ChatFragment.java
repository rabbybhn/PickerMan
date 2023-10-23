package webry.pickerman.redder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
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

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import webry.pickerman.redder.app.App;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.adapter.ChatListAdapter;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.model.ChatItem;
import webry.pickerman.redder.util.Helper;

public class ChatFragment extends Fragment implements Constants {

    public final static int STATUS_START = 100;

    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "ru.ifsoft.marketplace";

    final String LOG_TAG = "myLogs";

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    Menu MainMenu;

    View mListViewHeader;

    RelativeLayout mLoadingScreen, mErrorScreen;
    LinearLayout mContentScreen, mContainerImg, mChatListViewHeaderContainer;

    private LinearLayout mAdItemContainer;
    private TextView mAdItemLabel;
    private MaterialRippleLayout mAdItemButton;

    ImageView mSendMessage, mAddImg, mEmojiBtn, mDeleteImg, mPreviewImg;
    EmojiconEditText mMessageText;

    ListView listView;

    BroadcastReceiver br;

    private ArrayList<ChatItem> chatList;

    private ChatListAdapter chatAdapter;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;

    String withProfile = "", messageText = "", messageImg = "";
    int chatId = 0, msgId = 0, itemsCount = 0, position = 0;
    long profileId = 0;

    int pageId = 0; // For cha pagination

    private String adItemTitle = "";
    private long adItemId = 0;

    String lMessage = "", lMessageImage = "";

    // for chat update
    int u_id = 0;
    String u_message = "", u_image = "";
    int last_loaded_msg_ig = 0;

    Boolean blocked = false;

    long fromUserId = 0, toUserId = 0;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private String with_user_username = "", with_user_fullname = "", with_user_photo_url = "";
    private int with_user_state = 0, with_user_verified = 0;

    int arrayLength = 0;
    Boolean loadingMore = false;
    Boolean viewMore = false;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean visible = true;

    EmojiconsPopup popup;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();
        position = i.getIntExtra("position", 0);
        chatId = i.getIntExtra("chatId", 0);
        profileId = i.getLongExtra("profileId", 0);
        withProfile = i.getStringExtra("withProfile");

        with_user_username = i.getStringExtra("with_user_username");
        with_user_fullname = i.getStringExtra("with_user_fullname");
        with_user_photo_url = i.getStringExtra("with_user_photo_url");

        with_user_state = i.getIntExtra("with_user_state", 0);
        with_user_verified = i.getIntExtra("with_user_verified", 0);

        blocked = i.getBooleanExtra("blocked", false);

        fromUserId = i.getLongExtra("fromUserId", 0);
        toUserId = i.getLongExtra("toUserId", 0);

        adItemTitle = i.getStringExtra("itemTitle");
        adItemId = i.getLongExtra("itemId", 0);

        chatList = new ArrayList<ChatItem>();
        chatAdapter = new ChatListAdapter(getActivity(), chatList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        //Set on emojicon click listener
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //Set on backspace click listener
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });

        //If the emoji popup is dismissed, change mEmojiBtn to emoji icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if(popup.isShowing())

                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mMessageText.append(emojicon.getEmoji());
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageText.dispatchKeyEvent(event);
            }
        });




        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");

        } else {

            App.getInstance().setCurrentChatId(chatId);

            restore = false;
            loading = false;
            preload = false;
        }

        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                int msgId = intent.getIntExtra("msgId", 0);
                long msgFromUserId = intent.getLongExtra("msgFromUserId", 0);
                int msgFromUserState = intent.getIntExtra("msgFromUserState", 0);
                int msgFromUserVerify = intent.getIntExtra("msgFromUserVerify", 0);
                String msgFromUserUsername = intent.getStringExtra("msgFromUserUsername");
                String msgFromUserFullname = intent.getStringExtra("msgFromUserFullname");
                String msgFromUserPhotoUrl = intent.getStringExtra("msgFromUserPhotoUrl");
                String msgMessage = intent.getStringExtra("msgMessage");
                String msgImgUrl = intent.getStringExtra("msgImgUrl");
                int msgCreateAt = intent.getIntExtra("msgCreateAt", 0);
                String msgDate = intent.getStringExtra("msgDate");
                String msgTimeAgo = intent.getStringExtra("msgTimeAgo");

                ChatItem c = new ChatItem();
                c.setId(msgId);
                c.setFromUserId(msgFromUserId);

                if (msgFromUserId == App.getInstance().getId()) {

                    c.setFromUserState(App.getInstance().getState());
                    c.setFromUserVerify(App.getInstance().getVerify());
                    c.setFromUserUsername(App.getInstance().getUsername());
                    c.setFromUserFullname(App.getInstance().getFullname());
                    c.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                } else {

                    c.setFromUserState(with_user_state);
                    c.setFromUserVerify(with_user_verified);
                    c.setFromUserUsername(with_user_username);
                    c.setFromUserFullname(with_user_fullname);
                    c.setFromUserPhotoUrl(with_user_photo_url);
                }

                c.setMessage(msgMessage);
                c.setImgUrl(msgImgUrl);
                c.setCreateAt(msgCreateAt);
                c.setDate(msgDate);
                c.setTimeAgo(getActivity().getString(R.string.label_just_now));

                Log.e(LOG_TAG, "onReceive: task = " + task + ", status = " + status + " " + c.getMessage() + " " + Integer.toString(c.getId()));



                final ChatItem lastItem = (ChatItem) listView.getAdapter().getItem(listView.getAdapter().getCount() - 1);

                itemsCount++;

                chatList.add(c);

                if (!visible) {

                    try {

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
                        r.play();

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                chatAdapter.notifyDataSetChanged();

                scrollListViewToBottom();



//                getNextMessages();
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(br, intFilt);

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

                        mPreviewImg.setImageURI(null);
                        mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                        mContainerImg.setVisibility(View.VISIBLE);
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

                        mPreviewImg.setImageURI(null);
                        mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                        mContainerImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                choiceImage();

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

                choiceImage();

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

        mAdItemContainer = (LinearLayout) rootView.findViewById(R.id.adItemContainer);
        mAdItemButton = (MaterialRippleLayout) rootView.findViewById(R.id.adItemButton);
        mAdItemLabel = (TextView) rootView.findViewById(R.id.adItemLabel);

        mAdItemLabel.setText(adItemTitle);

        mAdItemButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra("itemId", adItemId);
                startActivity(intent);
            }
        });

        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loadingScreen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.errorScreen);

        mContentScreen = (LinearLayout) rootView.findViewById(R.id.contentScreen);

        mSendMessage = (ImageView) rootView.findViewById(R.id.sendMessage);
        mMessageText = (EmojiconEditText) rootView.findViewById(R.id.messageText);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newMessage();
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        mListViewHeader = getActivity().getLayoutInflater().inflate(R.layout.chat_listview_header, null);
        mChatListViewHeaderContainer = (LinearLayout) mListViewHeader.findViewById(R.id.chatListViewHeaderContainer);

        listView.addHeaderView(mListViewHeader);

        mListViewHeader.setVisibility(View.GONE);

        listView.setAdapter(chatAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0 && mListViewHeader.getVisibility() == View.VISIBLE) {

                    getChat();
                }
            }
        });

        mAddImg = (ImageView) rootView.findViewById(R.id.addImg);
        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mDeleteImg = (ImageView) rootView.findViewById(R.id.deleteImg);
        mPreviewImg = (ImageView) rootView.findViewById(R.id.previewImg);

        mContainerImg = (LinearLayout) rootView.findViewById(R.id.container_img);

        mContainerImg.setVisibility(View.GONE);

        mDeleteImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                selectedImage = null;
                selectedImagePath = "";

                mContainerImg.setVisibility(View.GONE);
            }
        });

        mAddImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Helper helper = new Helper(getActivity());

                if (helper.checkStoragePermission()) {

                    requestStoragePermission();

                } else {

                    choiceImage();
                }
            }
        });

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mPreviewImg.setImageURI(FileProvider.getUriForFile(getActivity(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
            mContainerImg.setVisibility(View.VISIBLE);
        }

        if (!EMOJI_KEYBOARD) {

            mEmojiBtn.setVisibility(View.GONE);
        }

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        //else, open the text keyboard first and immediately after that show the emoji popup
                        mMessageText.setFocusableInTouchMode(true);
                        mMessageText.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mMessageText, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    //If popup is showing, simply dismiss it to show the undelying text keyboard
                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mMessageText.setOnEditTextImeBackListener(er);

        if (!restore) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getChat();

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (!preload) {

                    showContentScreen();

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

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroyView() {

        super.onDestroyView();

        getActivity().unregisterReceiver(br);

        hidepDialog();
    }

    @Override
    public void onResume() {

        super.onResume();

        visible = true;
    }

    @Override
    public void onPause() {

        super.onPause();

        if (chatId != 0 && chatAdapter.getCount() > 0) {

            if (isAdded() || getActivity() != null) {

                int current_loaded_msg_id = chatList.get(chatAdapter.getCount() -1).getId();

                if (last_loaded_msg_ig != 0 && last_loaded_msg_ig < current_loaded_msg_id) {

                    Log.e("onPause", "onPause");

                    updateChat();
                }
            }
        }

        visible = false;
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

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
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

    public void choiceImage() {

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

    private void scrollListViewToBottom() {

        listView.smoothScrollToPosition(chatAdapter.getCount());

        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    public void updateChat() {

        if (chatAdapter.getCount() > 0) {

            u_message = "";
            u_image = "";
            u_id = 0;

            int current_loaded_msg_id = chatList.get(chatAdapter.getCount() -1).getId();

            if (last_loaded_msg_ig != 0 && last_loaded_msg_ig < current_loaded_msg_id) {

                u_message = chatList.get(chatAdapter.getCount() -1).getMessage();
                u_image = chatList.get(chatAdapter.getCount() -1).getImgUrl();
                u_id = chatList.get(chatAdapter.getCount() -1).getId();
            }

            Log.e("updateChat()", "updateChat()");

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_UPDATE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (!isAdded() || getActivity() == null) {

                                Log.e("ERROR", "ChatFragment Not Added to Activity");

                                return;
                            }

                            try {

                                if (!response.getBoolean("error")) {

                                    last_loaded_msg_ig = chatList.get(chatAdapter.getCount() -1).getId();
                                }

                            } catch (JSONException e) {

                                e.printStackTrace();

                            } finally {

                                Log.e("TAG", response.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (!isAdded() || getActivity() == null) {

                        Log.e("ERROR", "ChatFragment Not Added to Activity");

                        return;
                    }

                    preload = false;
                }
            }) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();

                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());

                    params.put("chatId", Integer.toString(chatId));

                    params.put("chatFromUserId", Long.toString(fromUserId));
                    params.put("chatToUserId", Long.toString(toUserId));

                    params.put("message", u_message);
                    params.put("image", u_image);
                    params.put("message_id", Integer.toString(u_id));

                    return params;
                }
            };

            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            jsonReq.setRetryPolicy(policy);

            App.getInstance().addToRequestQueue(jsonReq);
        }
    }

    public void getChat() {

        mListViewHeader.setVisibility(View.GONE);

        preload = true;

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("Chat", response.toString());

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                pageId = response.getInt("pageId");
                                chatId = response.getInt("chatId");

                                if (response.getInt("itemsCount") != 0) {

                                    itemsCount = response.getInt("itemsCount");
                                }

                                App.getInstance().setCurrentChatId(chatId);

                                fromUserId = response.getLong("chatFromUserId");
                                toUserId = response.getLong("chatToUserId");

                                if (response.has("items")) {

                                    JSONArray messagesArray = response.getJSONArray("items");

                                    arrayLength = messagesArray.length();

                                    if (arrayLength >= 20) {

                                        mListViewHeader.setVisibility(View.VISIBLE);
                                    }

                                    if (arrayLength > 0) {

                                        if (pageId == 0) {

                                            for (int i = messagesArray.length() - 1; i > -1; i--) {

                                                JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                                ChatItem item = new ChatItem(msgObj);

                                                if (item.getFromUserId() == App.getInstance().getId()) {

                                                    item.setFromUserState(App.getInstance().getState());
                                                    item.setFromUserVerify(App.getInstance().getVerify());
                                                    item.setFromUserUsername(App.getInstance().getUsername());
                                                    item.setFromUserFullname(App.getInstance().getFullname());
                                                    item.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                                                } else {

                                                    item.setFromUserState(with_user_state);
                                                    item.setFromUserVerify(with_user_verified);
                                                    item.setFromUserUsername(with_user_username);
                                                    item.setFromUserFullname(with_user_fullname);
                                                    item.setFromUserPhotoUrl(with_user_photo_url);
                                                }

                                                chatList.add(item);

                                                last_loaded_msg_ig = item.getId();
                                            }

                                            if (response.has("lastMessageCreateAt")) {

                                                int last_chat_view = 0;

                                                if (App.getInstance().getId() == response.getLong("chatFromUserId")) {

                                                    last_chat_view = response.getInt("fromUserId_lastView");

                                                } else {

                                                    last_chat_view = response.getInt("toUserId_lastView");
                                                }

                                                if (last_chat_view != 0 && last_chat_view < response.getInt("lastMessageCreateAt")) {

                                                    updateChat();
                                                }
                                            }

                                        } else {

                                            for (int i = 0; i < messagesArray.length(); i++) {

                                                JSONObject msgObj = (JSONObject) messagesArray.get(i);

                                                ChatItem item = new ChatItem(msgObj);

                                                if (item.getFromUserId() == App.getInstance().getId()) {

                                                    item.setFromUserState(App.getInstance().getState());
                                                    item.setFromUserVerify(App.getInstance().getVerify());
                                                    item.setFromUserUsername(App.getInstance().getUsername());
                                                    item.setFromUserFullname(App.getInstance().getFullname());
                                                    item.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());

                                                } else {

                                                    item.setFromUserState(with_user_state);
                                                    item.setFromUserVerify(with_user_verified);
                                                    item.setFromUserUsername(with_user_username);
                                                    item.setFromUserFullname(with_user_fullname);
                                                    item.setFromUserPhotoUrl(with_user_photo_url);
                                                }

                                                chatList.add(0, item);
                                            }
                                        }


                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            showContentScreen();

                            if (chatId != 0) {

                                if (pageId == 0) {

                                    scrollListViewToBottom();
                                }

                                pageId++;

                                Log.e("Response", response.toString());

                                Log.e("last_loaded_msg_ig", Integer.toString(last_loaded_msg_ig));

                                chatAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                preload = false;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();

                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("pageId", Integer.toString(pageId));

                params.put("adItemId", Long.toString(adItemId));

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void newMessage() {

        if (App.getInstance().isConnected()) {

            messageText = mMessageText.getText().toString();
            messageText = messageText.trim();

            if (selectedImagePath.length() != 0) {

                loading = true;

                showpDialog();

                File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName);

                uploadFile(METHOD_MSG_UPLOAD_IMG, f);

            } else {

                if (messageText.length() > 0) {

                    loading = true;

//                    showpDialog();

                    send();

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_msg), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

        } else {

            Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void send() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_MSG_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("CHAT", response.toString());

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                chatId = response.getInt("chatId");

                                App.getInstance().setCurrentChatId(chatId);

                                if (response.has("message")) {

                                    JSONObject msgObj = (JSONObject) response.getJSONObject("message");

                                    ChatItem item = new ChatItem(msgObj);

                                    item.setListId(response.getInt("listId"));

                                    chatList.set(response.getInt("listId") - 1, item);
                                }

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.msg_send_msg_error), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            if (chatId != 0 && fromUserId == 0 && toUserId == 0) {

                                fromUserId = App.getInstance().getId();
                                toUserId = profileId;
                            }

                            Log.e("CHAT NEW MSG", response.toString());

                            loading = false;

                            hidepDialog();

                            messageText = "";
                            messageImg = "";

                            getActivity().invalidateOptionsMenu();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("CHAT", error.toString());

                messageText = "";
                messageImg = "";

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));

                params.put("chatId", Integer.toString(chatId));
                params.put("messageText", lMessage);
                params.put("messageImg", lMessageImage);

                params.put("listId", Integer.toString(chatAdapter.getCount()));

                params.put("adItemId", Long.toString(adItemId));
                params.put("adItemTitle", adItemTitle);

                params.put("chatFromUserId", Long.toString(fromUserId));
                params.put("chatToUserId", Long.toString(toUserId));

                return params;
            }
        };

        lMessage = messageText;
        lMessageImage = messageImg;

        ChatItem cItem = new ChatItem();

        cItem.setListId(chatAdapter.getCount());
        cItem.setId(0);
        cItem.setFromUserId(App.getInstance().getId());
        cItem.setFromUserState(ACCOUNT_STATE_ENABLED);
        cItem.setFromUserUsername(App.getInstance().getUsername());
        cItem.setFromUserFullname(App.getInstance().getFullname());
        cItem.setFromUserPhotoUrl(App.getInstance().getPhotoUrl());
        cItem.setMessage(messageText);
        cItem.setImgUrl(messageImg);
        cItem.setTimeAgo(getActivity().getString(R.string.label_just_now));

        chatList.add(cItem);

        chatAdapter.notifyDataSetChanged();

        scrollListViewToBottom();

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);

        mContainerImg.setVisibility(View.GONE);
        selectedImagePath = "";
        selectedImage = null;
        messageImg = "";
        mMessageText.setText("");
        itemsCount++;
    }

    public void deleteChat() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_CHAT_REMOVE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "ChatFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                Intent i = new Intent();
                                i.putExtra("action", "Delete");
                                i.putExtra("position", position);
                                i.putExtra("chatId", chatId);
                                getActivity().setResult(RESULT_OK, i);

                                chatId = 0;

                                getActivity().finish();
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("ERROR", response.toString());

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "ChatFragment Not Added to Activity");

                    return;
                }

                Log.e("ERROR", error.toString());

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());

                params.put("profileId", Long.toString(profileId));
                params.put("chatId", Integer.toString(chatId));

                params.put("adItemId", Long.toString(adItemId));
                params.put("adItemTitle", adItemTitle);

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showLoadingScreen() {

        mContentScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);
    }

    public void showErrorScreen() {

        mContentScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        if (adItemId != 0) {

            mAdItemContainer.setVisibility(View.VISIBLE);

        } else {

            mAdItemContainer.setVisibility(View.GONE);
        }

        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mContentScreen.setVisibility(View.VISIBLE);

        preload = false;

        getActivity().invalidateOptionsMenu();
    }

    private void showMenuItems(Menu menu, boolean visible) {

        for (int i = 0; i < menu.size(); i++){

            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (App.getInstance().isConnected()) {

            if (!preload) {

                getActivity().setTitle(withProfile);

                if (chatId == 0) {

                    showMenuItems(menu, false);

                } else {

                    showMenuItems(menu, true);
                }

            } else {

                showMenuItems(menu, false);
            }

        } else {

            showMenuItems(menu, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_chat, menu);

        MainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_chat_delete: {

                deleteChat();

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


    public Boolean uploadFile(String serverURL, File file) {

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
                    .addHeader("Accept", "application/json;")
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

                            messageImg = result.getString("imgUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                send();
                            }
                        });
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