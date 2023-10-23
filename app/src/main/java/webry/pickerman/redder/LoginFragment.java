package webry.pickerman.redder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.util.CustomRequest;
import webry.pickerman.redder.util.Helper;


public class LoginFragment extends Fragment implements Constants {

    CallbackManager callbackManager;

    LoginButton loginButton;
    SignInButton mGoogleSignInButton;

    private ProgressDialog pDialog;

    TextView mForgotPassword;
    Button signinBtn, mSignupBtn;
    EditText signinUsername, signinPassword;
    String username, password;

    private String oauth_id = "", oauth_name = "", oauth_email = "";
    private int oauth_type = 0;

    private int pageId = PAGE_UNKNOWN;

    private Boolean loading = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        Bundle params = new Bundle();
        params.putString("action", "open");
        params.putString("fragment", "LoginFragment");
        mFirebaseAnalytics.logEvent("app_open_fragment", params);

        setRetainInstance(true);

        if (AccessToken.getCurrentAccessToken()!= null) LoginManager.getInstance().logOut();

        callbackManager = CallbackManager.Factory.create();

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
                                            oauth_name = user.getDisplayName();
                                            oauth_email = user.getEmail();
                                            oauth_type = OAUTH_TYPE_GOOGLE;

                                            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GOOGLE_AUTH, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            if (App.getInstance().authorize(response)) {

                                                                if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                                                    success();

                                                                } else {

                                                                    if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                                                        Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                                                    } else {

                                                                        Toast.makeText(getActivity(), getString(R.string.msg_account_inactive), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                            } else {

                                                                if (oauth_id.length() != 0) {

                                                                    Intent i = new Intent(getActivity(), SignupActivity.class);
                                                                    i.putExtra("oauth_id", oauth_id);
                                                                    i.putExtra("oauth_name", oauth_name);
                                                                    i.putExtra("oauth_email", oauth_email);
                                                                    i.putExtra("oauth_type", oauth_type);
                                                                    startActivity(i);

                                                                    getActivity().finish();

                                                                } else {

                                                                    Toast.makeText(getActivity(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            loading = false;

                                                            hidepDialog();
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                    Toast.makeText(getActivity(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                                    loading = false;

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

        // Get intent data

        Intent i = getActivity().getIntent();

        pageId = i.getIntExtra("pageId", PAGE_UNKNOWN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        if (loading) {

            showpDialog();
        }

        // Google Button

        mGoogleSignInButton = rootView.findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);

        setGooglePlusButtonText(mGoogleSignInButton, getString(R.string.action_login_with_google));

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSigninActivityResultLauncher.launch(signInIntent);
            }
        });

        // Facebook button

        loginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        loginButton.setPermissions("public_profile"); // "email",

        if (!FACEBOOK_AUTHORIZATION) {

            loginButton.setVisibility(View.GONE);
        }

        signinUsername = (EditText) rootView.findViewById(R.id.signinUsername);
        signinPassword = (EditText) rootView.findViewById(R.id.signinPassword);

        mForgotPassword = (TextView) rootView.findViewById(R.id.forgotPassword);

        mForgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), RecoveryActivity.class);
                startActivity(i);

                getActivity().finish();
            }
        });

        signinBtn = (Button) rootView.findViewById(R.id.signinBtn);
        mSignupBtn = (Button) rootView.findViewById(R.id.signupBtn);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = signinUsername.getText().toString();
                password = signinPassword.getText().toString();

                if (!App.getInstance().isConnected()) {

                    Toast.makeText(getActivity(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();

                } else if (!checkUsername() || !checkPassword()) {


                } else {

                    signin();
                }
            }
        });

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), SignupActivity.class);
                startActivity(intent);

                getActivity().finish();
            }
        });

        signinPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_DONE) {

                    if (checkUsername() && checkPassword()) {

                        signin();
                    }

                    return true;
                }

                return false;
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


        // Inflate the layout for this fragment
        return rootView;
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

                    oauth_type = OAUTH_TYPE_FACEBOOK;

                    if (object.has("id")) {

                        oauth_id = object.getString("id");
                    }

                    if (object.has("name")) {

                        oauth_name = object.getString("name");
                    }

                    if (object.has("email")) {

                        oauth_email = object.getString("email");
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

        Log.e("dimon", oauth_id);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINBYFACEBOOK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                success();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_DISABLED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_disabled), Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.msg_account_inactive), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            if (oauth_id.length() == 0) {

                                Toast.makeText(getActivity(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();

                            } else {

                                Intent i = new Intent(getActivity(), SignupActivity.class);
                                i.putExtra("oauth_id", oauth_id);
                                i.putExtra("oauth_name", oauth_name);
                                i.putExtra("oauth_email", oauth_email);
                                i.putExtra("oauth_type", oauth_type);
                                startActivity(i);

                                getActivity().finish();
                            }
                        }

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Login byFacebook()", "Error");

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

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void signin() {

        hideKeyboard();

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGIN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                success();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_DISABLED) {

                                Toast.makeText(getActivity(), getText(R.string.msg_account_disabled), Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(getActivity(), getString(R.string.msg_account_inactive), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            Toast.makeText(getActivity(), getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                        }

                        loading = false;

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Login signin()", error.toString());

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
                params.put("password", password);

                params.put("fcm_regId", App.getInstance().get_fcm_token());
                params.put("lang", App.getInstance().getLanguage());

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void success() {

        hideKeyboard();

        Intent i = new Intent();
        i.putExtra("pageId", pageId);

        getActivity().setResult(getActivity().RESULT_OK, i);
        getActivity().finish();
    }

    public Boolean checkUsername() {

        username = signinUsername.getText().toString();

        signinUsername.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            signinUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            signinUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username) && !helper.isValidEmail(username)) {

            signinUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return  true;
    }

    public Boolean checkPassword() {

        password = signinPassword.getText().toString();

        signinPassword.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            signinPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            signinPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            signinPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return  true;
    }

    private void hideKeyboard() {

        View view = getActivity().getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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