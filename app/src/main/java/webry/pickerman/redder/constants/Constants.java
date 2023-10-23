package webry.pickerman.redder.constants;

public interface Constants {

    public static final Boolean EMOJI_KEYBOARD = true; // false = Do not display your own Emoji keyboard | true = allow display your own Emoji keyboard

    public static final Boolean FACEBOOK_AUTHORIZATION = false; // false = Do not show buttons Login/Signup with Facebook | true = allow display buttons Login/Signup with Facebook

    public static final Boolean SHOW_ONLY_MODERATED_ADS_BY_DEFAULT = false; // true = Do not show unmoderated ads | false = allow display all ads by default | users can change this value in filters

    public static final Boolean WEB_SITE_AVAILABLE = true;  //Show menus "Open in Browser" and "Copy link" if site available

    public static final String APP_TEMP_FOLDER = "marketplace"; //directory for temporary storage of images from the camera

    public static final String WEB_SITE = "https://pickerman.live";  //web site url address | without "/" at the end!

    //Client ID | For identify the application | Example: 12567 | Must be the same in server config db.inc.php

    public static final String CLIENT_ID = "1";

    // Client Secret | Text constant | Must be the same with CLIENT_SECRET from server config: db.inc.php

    String CLIENT_SECRET = "Af_0W1+8v91h_YMhYT*&7=";    // Example: "Af_0W1+8v91h_YMhYT*&7="

    public static final String API_DOMAIN = "https://pickerman.live/";  //url address to which the application sends requests | with back slash "/" at the and, for example "https://mysite.com/"

    public static final String API_FILE_EXTENSION = ""; // Do not change the value of this constant!
    public static final String API_VERSION = "v1"; // Do not change the value of this constant!

    public static final String METHOD_ACCOUNT_GET_CATEGORIES = API_DOMAIN + "api/" + API_VERSION + "/method/category.getList" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_GET_CURRENCIES = API_DOMAIN + "api/" + API_VERSION + "/method/currency.getList" + API_FILE_EXTENSION;

    public static final String METHOD_ACCOUNT_GET_SETTINGS = API_DOMAIN + "api/" + API_VERSION + "/method/account.getSettings" + API_FILE_EXTENSION;

    String METHOD_ACCOUNT_LOGIN = API_DOMAIN + "api/" + API_VERSION + "/method/account.signIn" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SIGNUP = API_DOMAIN + "api/" + API_VERSION + "/method/account.signUp" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_AUTHORIZE = API_DOMAIN + "api/" + API_VERSION + "/method/account.authorize" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SET_FCM_TOKEN = API_DOMAIN + "api/" + API_VERSION + "/method/account.setFcmToken" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_LOGINBYFACEBOOK = API_DOMAIN + "api/" + API_VERSION + "/method/account.signInByFacebook" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_RECOVERY = API_DOMAIN + "api/" + API_VERSION + "/method/account.recovery" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SETPASSWORD = API_DOMAIN + "api/" + API_VERSION + "/method/account.setPassword" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_DEACTIVATE = API_DOMAIN + "api/" + API_VERSION + "/method/account.deactivate" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SAVE_SETTINGS = API_DOMAIN + "api/" + API_VERSION + "/method/account.saveSettings" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_CONNECT_TO_FACEBOOK = API_DOMAIN + "api/" + API_VERSION + "/method/account.connectToFacebook" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_DISCONNECT_FROM_FACEBOOK = API_DOMAIN + "api/" + API_VERSION + "/method/account.disconnectFromFacebook" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_LOGOUT = API_DOMAIN + "api/" + API_VERSION + "/method/account.logOut" + API_FILE_EXTENSION;
    public static final String METHOD_ACCOUNT_SET_ALLOW_MESSAGES = API_DOMAIN + "api/" + API_VERSION + "/method/account.setAllowMessages" + API_FILE_EXTENSION;

    public static final String METHOD_ACCOUNT_SET_GEO_LOCATION = API_DOMAIN + "api/" + API_VERSION + "/method/account.setGeoLocation" + API_FILE_EXTENSION;

    public static final String METHOD_SUPPORT_SEND_TICKET = API_DOMAIN + "api/" + API_VERSION + "/method/support.sendTicket" + API_FILE_EXTENSION;

    public static final String METHOD_PROFILE_GET = API_DOMAIN + "api/" + API_VERSION + "/method/profile.get" + API_FILE_EXTENSION;
    public static final String METHOD_PROFILE_REPORT = API_DOMAIN + "api/" + API_VERSION + "/method/profile.report" + API_FILE_EXTENSION;
    public static final String METHOD_PROFILE_UPLOADPHOTO = API_DOMAIN + "api/" + API_VERSION + "/method/profile.uploadPhoto" + API_FILE_EXTENSION;
    public static final String METHOD_PROFILE_UPLOADCOVER = API_DOMAIN + "api/" + API_VERSION + "/method/profile.uploadCover" + API_FILE_EXTENSION;
    public static final String METHOD_PROFILE_WALL = API_DOMAIN + "api/" + API_VERSION + "/method/profile.wall" + API_FILE_EXTENSION;


    public static final String METHOD_BLACKLIST_GET = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.get" + API_FILE_EXTENSION;
    public static final String METHOD_BLACKLIST_ADD = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.add" + API_FILE_EXTENSION;
    public static final String METHOD_BLACKLIST_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/blacklist.remove" + API_FILE_EXTENSION;

    public static final String METHOD_NOTIFICATIONS_GET = API_DOMAIN + "api/" + API_VERSION + "/method/notifications.get" + API_FILE_EXTENSION;
    public static final String METHOD_ITEM_GET = API_DOMAIN + "api/" + API_VERSION + "/method/item.get" + API_FILE_EXTENSION;

    public static final String METHOD_APP_CHECKUSERNAME = API_DOMAIN + "api/" + API_VERSION + "/method/app.checkUsername" + API_FILE_EXTENSION;
    public static final String METHOD_APP_TERMS = API_DOMAIN + "api/" + API_VERSION + "/method/app.terms" + API_FILE_EXTENSION;
    public static final String METHOD_APP_PRIVACY = API_DOMAIN + "api/" + API_VERSION + "/method/app.privacy" + API_FILE_EXTENSION;
    public static final String METHOD_APP_GDPR = API_DOMAIN + "api/" + API_VERSION + "/method/app.gdpr" + API_FILE_EXTENSION;
    public static final String METHOD_APP_THANKS = API_DOMAIN + "api/" + API_VERSION + "/method/app.thanks" + API_FILE_EXTENSION;
    public static final String METHOD_APP_SEARCH = API_DOMAIN + "api/" + API_VERSION + "/method/app.search" + API_FILE_EXTENSION;

    public static final String METHOD_ITEMS_ACTIVATE = API_DOMAIN + "api/" + API_VERSION + "/method/items.activate" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_INACTIVATE = API_DOMAIN + "api/" + API_VERSION + "/method/items.inactivate" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/items.remove" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_UPLOAD_IMG = API_DOMAIN + "api/" + API_VERSION + "/method/items.uploadImg" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_NEW = API_DOMAIN + "api/" + API_VERSION + "/method/items.new" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_EDIT = API_DOMAIN + "api/" + API_VERSION + "/method/items.edit" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_REPORT = API_DOMAIN + "api/" + API_VERSION + "/method/items.report" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_LIKE = API_DOMAIN + "api/" + API_VERSION + "/method/items.like" + API_FILE_EXTENSION;

    public static final String METHOD_ITEMS_PHONE = API_DOMAIN + "api/" + API_VERSION + "/method/items.phone" + API_FILE_EXTENSION;
    public static final String METHOD_ITEMS_SHARE = API_DOMAIN + "api/" + API_VERSION + "/method/items.share" + API_FILE_EXTENSION;

    public static final String METHOD_FAVORITES_GET = API_DOMAIN + "api/" + API_VERSION + "/method/favorites.get" + API_FILE_EXTENSION;

    public static final String METHOD_CHAT_GET = API_DOMAIN + "api/" + API_VERSION + "/method/chat.get" + API_FILE_EXTENSION;
    public static final String METHOD_CHAT_REMOVE = API_DOMAIN + "api/" + API_VERSION + "/method/chat.remove" + API_FILE_EXTENSION;
    public static final String METHOD_CHAT_UPDATE = API_DOMAIN + "api/" + API_VERSION + "/method/chat.update" + API_FILE_EXTENSION;

    public static final String METHOD_DIALOGS_GET = API_DOMAIN + "api/" + API_VERSION + "/method/dialogs.get" + API_FILE_EXTENSION;

    public static final String METHOD_MSG_NEW = API_DOMAIN + "api/" + API_VERSION + "/method/msg.new" + API_FILE_EXTENSION;
    public static final String METHOD_MSG_UPLOAD_IMG = API_DOMAIN + "api/" + API_VERSION + "/method/msg.uploadImg" + API_FILE_EXTENSION;

    public static final String METHOD_FINDER_GET = API_DOMAIN + "api/" + API_VERSION + "/method/finder.get" + API_FILE_EXTENSION;

    public static final String METHOD_ACCOUNT_GOOGLE_AUTH = API_DOMAIN + "api/" + API_VERSION + "/method/account.google" + API_FILE_EXTENSION;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_PHOTO = 1;                  //WRITE_EXTERNAL_STORAGE
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_COVER = 2;                  //WRITE_EXTERNAL_STORAGE
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 3;                               //ACCESS_COARSE_LOCATION
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;                        //WRITE_EXTERNAL_STORAGE
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 5;                                    //CALL_PHONE

    public static final int VOLLEY_REQUEST_SECONDS = 20;                                              //SECONDS TO REQUEST

    public static final int OAUTH_TYPE_FACEBOOK = 0;
    public static final int OAUTH_TYPE_GOOGLE = 1;

    public static final int APP_TYPE_ALL = -1;
    public static final int APP_TYPE_UNKNOWN = 0;
    public static final int APP_TYPE_WEB = 1;
    public static final int APP_TYPE_ANDROID = 2;
    public static final int APP_TYPE_IOS = 3;

    public static final int PAGE_UNKNOWN = 0;
    public static final int PAGE_FLOW = 1;
    public static final int PAGE_SEARCH = 3;
    public static final int PAGE_FAVORITES = 5;
    public static final int PAGE_NOTIFICATIONS = 6;
    public static final int PAGE_MESSAGES = 7;
    public static final int PAGE_PROFILE = 8;

    public static final int APP_BAR_WITH_ADMOB_HEIGHT = 150;
    public static final int APP_BAR_WITHOUT_ADMOB_HEIGHT = 100;

    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;

    public static final int LIST_ITEMS = 20;

    public static final int POST_CHARACTERS_LIMIT = 1000;
    public static final int NEW_ITEM_DESCRIPTION_CHARACTERS_LIMIT = 500;

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;

    public static final int GCM_ENABLED = 1;
    public static final int GCM_DISABLED = 0;

    public static final int ADMOB_ENABLED = 0;
    public static final int ADMOB_DISABLED = 0;

    public static final int COMMENTS_ENABLED = 1;
    public static final int COMMENTS_DISABLED = 0;

    public static final int MESSAGES_ENABLED = 1;
    public static final int MESSAGES_DISABLED = 0;

    public static final int ERROR_SUCCESS = 0;

    public static final int SEX_UNKNOWN = 0;
    public static final int SEX_MALE = 1;
    public static final int SEX_FEMALE = 2;

    public static final int NOTIFY_TYPE_LIKE = 0;
    public static final int NOTIFY_TYPE_FOLLOWER = 1;
    public static final int NOTIFY_TYPE_MESSAGE = 2;
    public static final int NOTIFY_TYPE_COMMENT = 3;
    public static final int NOTIFY_TYPE_COMMENT_REPLY = 4;
    public static final int NOTIFY_TYPE_GIFT = 6;
//    public static final int NOTIFY_TYPE_REVIEW = 7;

    public static final int NOTIFY_TYPE_REVIEW = 14;
    public static final int NOTIFY_TYPE_FRIEND_REQUEST = 15;
    public static final int NOTIFY_TYPE_REPOST = 16;
    public static final int NOTIFY_TYPE_POST = 17;
    public static final int NOTIFY_TYPE_MENTION = 18;

    public static final int NOTIFY_TYPE_ITEM_APPROVED = 19;
    public static final int NOTIFY_TYPE_ITEM_REJECTED = 20;

    public static final int NOTIFY_TYPE_PROFILE_PHOTO_APPROVE = 2003;
    public static final int NOTIFY_TYPE_PROFILE_PHOTO_REJECT = 2004;
    public static final int NOTIFY_TYPE_PROFILE_COVER_APPROVE = 2007;
    public static final int NOTIFY_TYPE_PROFILE_COVER_REJECT = 2008;


    public static final int GCM_NOTIFY_CONFIG = 0;
    public static final int GCM_NOTIFY_SYSTEM = 1;
    public static final int GCM_NOTIFY_CUSTOM = 2;
    public static final int GCM_NOTIFY_LIKE = 3;
    public static final int GCM_NOTIFY_ANSWER = 4;
    public static final int GCM_NOTIFY_QUESTION = 5;
    public static final int GCM_NOTIFY_COMMENT = 6;
    public static final int GCM_NOTIFY_FOLLOWER = 7;
    public static final int GCM_NOTIFY_PERSONAL = 8;
    public static final int GCM_NOTIFY_MESSAGE = 9;
    public static final int GCM_NOTIFY_COMMENT_REPLY = 10;
    public static final int GCM_NOTIFY_GIFT = 14;
    public static final int GCM_NOTIFY_REVIEW = 15;

    public static final int GCM_NOTIFY_ITEM_APPROVED = 100;
    public static final int GCM_NOTIFY_ITEM_REJECTED = 101;

    public static final int GCM_NOTIFY_PROFILE_PHOTO_APPROVE = 1003;
    public static final int GCM_NOTIFY_PROFILE_PHOTO_REJECT = 1004;
    public static final int GCM_NOTIFY_PROFILE_COVER_APPROVE = 1007;
    public static final int GCM_NOTIFY_PROFILE_COVER_REJECT = 1008;

    public static final int ITEM_TYPE_ALL = -1;
    public static final int ITEM_TYPE_DEFAULT = 0;
    public static final int ITEM_TYPE_MEDIA = 1;
    public static final int ITEM_TYPE_CLASSIFIED = 2;


    public static final int ERROR_LOGIN_TAKEN = 300;
    public static final int ERROR_EMAIL_TAKEN = 301;
    public static final int ERROR_FACEBOOK_ID_TAKEN = 302;

    public static final int ACCOUNT_STATE_ENABLED = 0;
    public static final int ACCOUNT_STATE_DISABLED = 1;
    public static final int ACCOUNT_STATE_BLOCKED = 2;
    public static final int ACCOUNT_STATE_DEACTIVATED = 3;

    public static final int ACCOUNT_TYPE_USER = 0;
    public static final int ACCOUNT_TYPE_GROUP = 1;

    public static final int ERROR_UNKNOWN = 100;
    public static final int ERROR_ACCESS_TOKEN = 101;

    public static final int ERROR_ACCOUNT_ID = 400;

    int ERROR_CLIENT_ID = 19100;
    int ERROR_CLIENT_SECRET = 19101;

    public static final int UPLOAD_TYPE_PHOTO = 0;
    public static final int UPLOAD_TYPE_COVER = 1;

    public static final int ACTION_NEW = 1;
    public static final int ACTION_EDIT = 2;
    public static final int SELECT_POST_IMG = 3;
    public static final int VIEW_CHAT = 4;
    public static final int CREATE_POST_IMG = 5;
    public static final int SELECT_CHAT_IMG = 6;
    public static final int CREATE_CHAT_IMG = 7;
    public static final int FEED_NEW_POST = 8;
    public static final int FRIENDS_SEARCH = 9;
    public static final int ITEM_EDIT = 10;
    public static final int STREAM_NEW_POST = 11;
    public static final int ACTION_LOGIN = 100;
    public static final int ACTION_SIGNUP = 101;

    public static final int ACCOUNT_ACCESS_LEVEL_AVAILABLE_TO_ALL = 0;
    public static final int ACCOUNT_ACCESS_LEVEL_AVAILABLE_TO_FRIENDS = 1;

    public static final String TAG = "TAG";

    public static final String HASHTAGS_COLOR = "#5BCFF2";
}