package webry.pickerman.redder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;
import webry.pickerman.redder.util.CustomRequest;

public class SettingsFragment extends PreferenceFragmentCompat implements Constants {

    private Preference languagePreference, logoutPreference, itemContactUs, aboutPreference, changePassword, itemServices, itemGdpr, itemPrivacy, itemSite, itemTerms, itemThanks, itemBlackList, itemNotifications, itemDeactivateAccount;
    private CheckBoxPreference allowMessages;
    private SwitchPreference mNightModeSwitch;

    private ProgressDialog pDialog;

    int mAllowMessages;

    LinearLayout aboutDialogContent;
    TextView aboutDialogAppName, aboutDialogAppVersion, aboutDialogAppCopyright;

    private Boolean loading = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setRetainInstance(true);

        initpDialog();

        // Load the preferences from an XML resource
        //addPreferencesFromResource(R.xml.settings);

        setPreferencesFromResource(R.xml.settings, rootKey);

        languagePreference = findPreference("settings_language");
        logoutPreference = findPreference("settings_logout");
        aboutPreference = findPreference("settings_version");
        changePassword = findPreference("settings_change_password");
        itemDeactivateAccount = findPreference("settings_deactivate_account");
        itemServices = findPreference("settings_services");
        itemSite = findPreference("settings_site");
        itemPrivacy = findPreference("settings_privacy");
        itemGdpr = findPreference("settings_gdpr");
        itemTerms = findPreference("settings_terms");
        itemThanks = findPreference("settings_thanks");
        itemBlackList = findPreference("settings_blocked_list");
        itemNotifications = findPreference("settings_push_notifications");
        itemContactUs = findPreference("settings_contact_us");

        allowMessages = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessages");

        Preference pref = findPreference("settings_version");

        pref.setTitle(getString(R.string.app_name) + " v" + getString(R.string.app_version));

        // Set language info

        languagePreference.setSummary(App.getInstance().getLanguageNameByCode(App.getInstance().getLanguage()));

        languagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                List<String> language_names = new ArrayList<String>();

                Resources r = getResources();
                Configuration c = r.getConfiguration();

                for (int i = 0; i < App.getInstance().getLanguages().size(); i++) {

                    language_names.add(App.getInstance().getLanguages().get(i).get("lang_name"));
                }

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(getText(R.string.title_select_language));

                b.setItems(language_names.toArray(new CharSequence[language_names.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        App.getInstance().setLanguage(App.getInstance().getLanguages().get(which).get("lang_id"));

                        App.getInstance().saveData();

                        // Clear Categories list

                        App.getInstance().clearCategoriesList();

                        // Set App Language

                        App.getInstance().setLocale(App.getInstance().getLanguage());

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

                b.setNegativeButton(getText(R.string.action_cancel), null);

                AlertDialog d = b.create();
                d.show();

                return true;
            }
        });

        itemContactUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), SupportActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemThanks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_THANKS);
                i.putExtra("title", getText(R.string.settings_thanks));
                startActivity(i);

                return true;
            }
        });

        itemTerms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_TERMS);
                i.putExtra("title", getText(R.string.settings_terms));
                startActivity(i);

                return true;
            }
        });

        itemGdpr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_GDPR);
                i.putExtra("title", getText(R.string.settings_gdpr));
                startActivity(i);

                return true;
            }
        });

        itemPrivacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_PRIVACY);
                i.putExtra("title", getText(R.string.settings_privacy));
                startActivity(i);

                return true;
            }
        });

        itemSite.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(WEB_SITE));
                startActivity(i);

                return true;
            }
        });

        if (!WEB_SITE_AVAILABLE) {

            PreferenceCategory headerAbout = (PreferenceCategory) findPreference("header_about");

            headerAbout.removePreference(itemSite);
        }

        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(getText(R.string.action_about));

                aboutDialogContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);

                alertDialog.setView(aboutDialogContent);

                aboutDialogAppName = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppName);
                aboutDialogAppVersion = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppVersion);
                aboutDialogAppCopyright = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppCopyright);

                aboutDialogAppName.setText(getString(R.string.app_name));
                aboutDialogAppVersion.setText("Version " + getString(R.string.app_version));
                aboutDialogAppCopyright.setText("Copyright © " + getString(R.string.app_year) + " " + getString(R.string.app_copyright));

//                    alertDialog.setMessage("Version " + APP_VERSION + "/r/n" + APP_COPYRIGHT);
                alertDialog.setCancelable(true);
                alertDialog.setNeutralButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.show();

                return false;
            }
        });

        if (App.getInstance().getId() != 0) {

            // if authorized

            pref = findPreference("settings_logout");

            pref.setSummary(App.getInstance().getUsername());

            if (!FACEBOOK_AUTHORIZATION) {

                PreferenceCategory headerGeneral = (PreferenceCategory) findPreference("header_general");
                headerGeneral.removePreference(itemServices);
            }

            itemNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {

                    Intent i = new Intent(getActivity(), NotificationsSettingsActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            itemBlackList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {

                    Intent i = new Intent(getActivity(), BlackListActivity.class);
                    startActivity(i);

                    return true;
                }
            });



            logoutPreference.setSummary(App.getInstance().getUsername());

            logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(getText(R.string.action_logout));

                    alertDialog.setMessage(getText(R.string.msg_action_logout));
                    alertDialog.setCancelable(true);

                    alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });

                    alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            loading = true;

                            showpDialog();

                            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {

                                                if (!response.getBoolean("error")) {

                                                    Log.d("Logout", "Logout success");
                                                }

                                            } catch (JSONException e) {

                                                e.printStackTrace();

                                            } finally {

                                                loading = false;

                                                hidepDialog();

                                                App.getInstance().removeData();
                                                App.getInstance().readData();

                                                App.getInstance().setNotificationsCount(0);
                                                App.getInstance().setMessagesCount(0);
                                                App.getInstance().setId(0);
                                                App.getInstance().setUsername("");
                                                App.getInstance().setFullname("");

                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    loading = false;

                                    hidepDialog();
                                }
                            }) {

                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("clientId", CLIENT_ID);
                                    params.put("accountId", Long.toString(App.getInstance().getId()));
                                    params.put("accessToken", App.getInstance().getAccessToken());

                                    return params;
                                }
                            };

                            RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                            jsonReq.setRetryPolicy(policy);

                            App.getInstance().addToRequestQueue(jsonReq);
                        }
                    });

                    alertDialog.show();

                    return true;
                }
            });

            changePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent i = new Intent(getActivity(), ChangePasswordActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            itemDeactivateAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent i = new Intent(getActivity(), DeactivateActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            itemServices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent i = new Intent(getActivity(), ServicesActivity.class);
                    startActivity(i);

                    return true;
                }
            });

            allowMessages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (newValue instanceof Boolean) {

                        Boolean value = (Boolean) newValue;

                        if (value) {

                            mAllowMessages = 1;

                        } else {

                            mAllowMessages = 0;
                        }

                        if (App.getInstance().isConnected()) {

                            setAllowMessages();

                        } else {

                            Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
            });

            checkAllowMessages(App.getInstance().getAllowMessages());

        } else {

            // if not authorized - delete some settings menus

            PreferenceCategory headerGeneral = (PreferenceCategory) findPreference("header_general");

            headerGeneral.removePreference(allowMessages);
            headerGeneral.removePreference(itemNotifications);
            headerGeneral.removePreference(changePassword);
            headerGeneral.removePreference(itemDeactivateAccount);
            headerGeneral.removePreference(itemBlackList);
            headerGeneral.removePreference(itemServices);

            PreferenceCategory headerOthers = (PreferenceCategory) findPreference("header_others");

            headerOthers.removePreference(logoutPreference);
        }

        //

        mNightModeSwitch = (SwitchPreference) getPreferenceManager().findPreference("nightThemeSwitch");

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

            mNightModeSwitch.setChecked(true);

        } else {

            mNightModeSwitch.setChecked(false);
        }

        mNightModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                        App.getInstance().setNightMode(1);

                    } else {

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                        App.getInstance().setNightMode(0);
                    }

                    App.getInstance().saveData();
                }

                return true;
            }
        });

        //


    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        if (loading) {

            showpDialog();
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    public void checkAllowMessages(int value) {

        if (value == 1) {

            allowMessages.setChecked(true);
            mAllowMessages = 1;

        } else {

            allowMessages.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void setAllowMessages() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_ALLOW_MESSAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowMessages(response.getInt("allowMessages"));

                                checkAllowMessages(App.getInstance().getAllowMessages());
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

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowMessages", Integer.toString(mAllowMessages));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}