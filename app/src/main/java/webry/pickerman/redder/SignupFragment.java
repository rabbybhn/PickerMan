package webry.pickerman.redder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;

public class SignupFragment extends Fragment implements Constants {

    CallbackManager callbackManager;

    LoginButton loginButton;

    private ProgressDialog pDialog;

    LinearLayout mActionContainer;

    EditText signupUsername, signupFullname, signupPassword, signupEmail;
    Button signupJoinHowBtn, mSigninButton;
    TextView mLabelTerms, mRegularSignup, mLabelAuthorizationViaFacebook;

    private String username, password, email, language, fullname;
    private String oauth_id = "";
    private int oauth_type = 0;

    private Boolean restore = false;
    private Boolean loading = false;
    private Boolean username_loading = false;

    private String username_loading_txt = "";

    private FirebaseAnalytics mFirebaseAnalytics;

    // Google

    SignInButton mGoogleSignInButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle params = new Bundle();
        params.putString("action", "open");
        params.putString("fragment", "SignupFragment");
        mFirebaseAnalytics.logEvent("app_open_fragment", params);

        setRetainInstance(true);

        if (AccessToken.getCurrentAccessToken()!= null) LoginManager.getInstance().logOut();

        callbackManager = CallbackManager.Factory.create();

        //

        Intent i = getActivity().getIntent();
        oauth_id = i.getStringExtra("oauth_id");

        if (oauth_id == null) {

            oauth_id = "";
        }

        oauth_type = i.getIntExtra("oauth_type", 0);

        //

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            // User is signed in

            FirebaseAuth.getInstance().signOut();
        }

        googleSigninActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // There are no request codes
                    Intent data = result.getData();

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {

                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        // Signed in successfully, show authenticated UI.

                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                        mAuth.signInWithCredential(credential)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {

                                            // Sign in success, update UI with the signed-in user's information

                                            FirebaseUser user = mAuth.getCurrentUser();

                                            oauth_id = user.getUid();
                                            fullname = user.getDisplayName();
                                            email = user.getEmail();
                                            oauth_type = OAUTH_TYPE_GOOGLE;

                                            showpDialog();

                                            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GOOGLE_AUTH, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            if (App.getInstance().authorize(response)) {

                                                                if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);

                                                                } else {

                                                                    if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                                                        Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                                                    } else {

                                                                        Toast.makeText(getActivity(), getString(R.string.msg_account_inactive), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                            } else {

                                                                if (oauth_id.length() != 0) {

                                                                    oauth_type = OAUTH_TYPE_GOOGLE;

                                                                    updateView();

                                                                } else {

                                                                    Toast.makeText(getActivity(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            hidepDialog();
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                    Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                                    hidepDialog();
                                                }
                                            }) {

                                                @Override
                                                protected Map<String, String> getParams() {
                                                    Map<String, String> params = new HashMap<String, String>();
                                                    params.put("client_id", CLIENT_ID);
                                                    params.put("uid", oauth_id);
                                                    params.put("app_type", Integer.toString(APP_TYPE_ANDROID));
                                                    params.put("fcm_regId", App.getInstance().get_fcm_token());

                                                    return params;
                                                }
                                            };

                                            App.getInstance().addToRequestQueue(jsonReq);

                                        } else {

                                            // If sign in fails, display a message to the user.
                                            Log.e("Google", "signInWithCredential:failure", task.getException());
                                            Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                    } catch (ApiException e) {

                        // The ApiException status code indicates the detailed failure reason.
                        // Please refer to the GoogleSignInStatusCodes class reference for more information.
                        Log.e("Google", "Google sign in failed", e);
                    }
                }
            }
        });

        //

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        if (loading) {

            showpDialog();
        }

        // Google Button

        mGoogleSignInButton = rootView.findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);

        setGooglePlusButtonText(mGoogleSignInButton, getString(R.string.action_signup_with_google));

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSigninActivityResultLauncher.launch(signInIntent);
            }
        });

        //

        loginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        loginButton.setPermissions("public_profile"); // "email",

        if (!FACEBOOK_AUTHORIZATION) {

            loginButton.setVisibility(View.GONE);
        }

        mActionContainer = (LinearLayout) rootView.findViewById(R.id.actionContainer);

        signupUsername = (EditText) rootView.findViewById(R.id.signupUsername);
        signupFullname = (EditText) rootView.findViewById(R.id.signupFullname);
        signupPassword = (EditText) rootView.findViewById(R.id.signupPassword);
        signupEmail = (EditText) rootView.findViewById(R.id.signupEmail);

        mLabelTerms = (TextView) rootView.findViewById(R.id.SignupLabelTerms);

        mLabelTerms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_TERMS);
                i.putExtra("title", getText(R.string.signup_label_terms_and_policies));
                startActivity(i);
            }
        });

        mLabelAuthorizationViaFacebook = (TextView) rootView.findViewById(R.id.labelAuthorizationViaFacebook);

        mRegularSignup = (TextView) rootView.findViewById(R.id.regularSignup);

        mRegularSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                oauth_id = "";
                oauth_type = 0;

                updateView();
            }
        });

        mSigninButton = (Button) rootView.findViewById(R.id.signinBtn);

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);

                getActivity().finish();
            }
        });

        if (oauth_id == null) {

            oauth_id = "";
        }

        signupUsername.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (App.getInstance().isConnected() && checkUsername() && !username_loading) {

                    username_loading = true;
                    username_loading_txt = signupUsername.getText().toString().trim();

                    CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECKUSERNAME, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {

                                        if (response.getBoolean("error")) {

                                            if (username_loading_txt.equals(signupUsername.getText().toString().trim())) {

                                                signupUsername.setError(getString(R.string.error_login_taken));
                                            }
                                        }

                                    } catch (JSONException e) {

                                        e.printStackTrace();

                                    } finally {

                                        username_loading = false;
                                        username_loading_txt = "";
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            username_loading = false;
                            username_loading_txt = "";

                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", username);

                            return params;
                        }
                    };

                    App.getInstance().addToRequestQueue(jsonReq);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        signupFullname.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                checkFullname();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signupPassword.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                checkPassword();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signupEmail.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                checkEmail();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signupJoinHowBtn = (Button) rootView.findViewById(R.id.signupJoinHowBtn);

        signupJoinHowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = signupUsername.getText().toString();
                fullname = signupFullname.getText().toString();
                password = signupPassword.getText().toString();
                email = signupEmail.getText().toString();
                language = Locale.getDefault().getLanguage();

                if (verifyRegForm()) {

                    if (App.getInstance().isConnected()) {

                        loading = true;

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        if (App.getInstance().authorize(response)) {

                                            if (App.getInstance().getState() != ACCOUNT_STATE_DEACTIVATED) {

                                                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }

                                        } else {

                                            switch (App.getInstance().getErrorCode()) {

                                                case ERROR_CLIENT_ID : {

                                                    Toast.makeText(getActivity(), getString(R.string.error_client_id), Toast.LENGTH_SHORT).show();

                                                    break;
                                                }

                                                case ERROR_CLIENT_SECRET : {

                                                    Toast.makeText(getActivity(), getString(R.string.error_client_secret), Toast.LENGTH_SHORT).show();

                                                    break;
                                                }

                                                case ERROR_LOGIN_TAKEN : {

                                                    signupUsername.setError(getString(R.string.error_login_taken));
                                                    break;
                                                }

                                                case ERROR_EMAIL_TAKEN : {

                                                    signupEmail.setError(getString(R.string.error_email_taken));
                                                    break;
                                                }

                                                default: {

                                                    Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                                    break;
                                                }
                                            }
                                        }

                                        loading = false;

                                        hidepDialog();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Log.e("Error Signup()", "Malformed JSON: \"" + error.toString() + "\"");
                                Log.e("Error Signup()", "Malformed JSON: \"" + error.getMessage() + "\"");

                                Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                loading = false;

                                hidepDialog();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {

                                Map<String, String> params = new HashMap<String, String>();

                                params.put("clientId", CLIENT_ID);
                                params.put("appType", Integer.toString(APP_TYPE_ANDROID));

                                params.put("username", username);
                                params.put("fullname", fullname);
                                params.put("password", password);
                                params.put("email", email);
                                params.put("language", language);

                                params.put("oauth_id", oauth_id);
                                params.put("oauth_type", Integer.toString(oauth_type));

                                params.put("hash", Helper.md5(Helper.md5(username) + CLIENT_SECRET));

                                params.put("fcm_regId", App.getInstance().get_fcm_token());
                                params.put("lang", App.getInstance().getLanguage());

                                return params;
                            }
                        };

                        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                        jsonReq.setRetryPolicy(policy);

                        App.getInstance().addToRequestQueue(jsonReq);

                    } else {

                        Toast.makeText(getActivity(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Registering CallbackManager with the LoginButton
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                // Retrieving access token using the LoginResult
                AccessToken accessToken = loginResult.getAccessToken();

                useLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {

            }
            @Override
            public void onError(FacebookException error) {

            }
        });

        if (!restore) {

//            getNotifications();
        }

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateView() {

        if (oauth_id.length() == 0) {

            loginButton.setVisibility(View.VISIBLE);
            mGoogleSignInButton.setVisibility(View.VISIBLE);

            mActionContainer.setVisibility(View.GONE);

        } else {

            loginButton.setVisibility(View.GONE);
            mGoogleSignInButton.setVisibility(View.GONE);

            if (oauth_type == OAUTH_TYPE_FACEBOOK) {

                mLabelAuthorizationViaFacebook.setText(getString(R.string.label_authorization_via_facebook));

            } else {

                mLabelAuthorizationViaFacebook.setText(getString(R.string.label_authorization_via_google));
            }

            mActionContainer.setVisibility(View.VISIBLE);
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {

        for (int i = 0; i < signInButton.getChildCount(); i++) {

            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {

                TextView tv = (TextView) v;
                tv.setTextSize(15);
                tv.setTypeface(null, Typeface.NORMAL);
                tv.setText(buttonText);

                return;
            }
        }
    }

    private void useLoginInformation(AccessToken accessToken) {

        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/

        loading = true;

        showpDialog();

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {

                    if (object.has("id")) {

                        oauth_id = object.getString("id");
                    }

                    if (object.has("name")) {

                        fullname = object.getString("name");
                    }

                    if (object.has("email")) {

                        email = object.getString("email");
                    }

                } catch (JSONException e) {

                    Log.e("Facebook Login", "Could not parse malformed JSON: \"" + object.toString() + "\"");

                } finally {

                    if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();

                    if (!oauth_id.equals("")) {

                        signinByFacebookId();

                    } else {

                        loading = false;

                        hidepDialog();
                    }
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        // parameters.putString("fields", "id,name,email,picture.width(200)");
        parameters.putString("fields", "id, name");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signinByFacebookId() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINBYFACEBOOK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_DISABLED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_disabled), Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.msg_account_inactive), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            if (!oauth_id.equals("")) {

                                oauth_type = OAUTH_TYPE_FACEBOOK;

                                updateView();
                            }
                        }

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Facebook Login", "Error");

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("appType", Integer.toString(APP_TYPE_ANDROID));

                params.put("facebookId", oauth_id);
                params.put("clientId", CLIENT_ID);

                params.put("fcm_regId", App.getInstance().get_fcm_token());
                params.put("lang", App.getInstance().getLanguage());

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public Boolean checkUsername() {

        username = signupUsername.getText().toString().trim();

        Helper helper = new Helper();

        if (username.length() == 0) {

            signupUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            signupUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            signupUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupUsername.setError(null);

        return  true;
    }

    public Boolean checkFullname() {

        fullname = signupFullname.getText().toString();

        if (fullname.length() == 0) {

            signupFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            signupFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        signupFullname.setError(null);

        return  true;
    }

    public Boolean checkPassword() {

        password = signupPassword.getText().toString();

        Helper helper = new Helper();

        if (password.length() == 0) {

            signupPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            signupPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            signupPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupPassword.setError(null);

        return true;
    }

    public Boolean checkEmail() {

        email = signupEmail.getText().toString();

        Helper helper = new Helper();

        if (email.length() == 0) {

            signupEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            signupEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        signupEmail.setError(null);

        return true;
    }

    public Boolean verifyRegForm() {

        signupUsername.setError(null);
        signupFullname.setError(null);
        signupPassword.setError(null);
        signupEmail.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            signupUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            signupUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            signupUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (fullname.length() == 0) {

            signupFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            signupFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        if (password.length() == 0) {

            signupPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            signupPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            signupPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (email.length() == 0) {

            signupEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            signupEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return true;
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