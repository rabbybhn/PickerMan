package webry.pickerman.redder;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;


import webry.pickerman.redder.app.App;
import webry.pickerman.redder.constants.Constants;

public class NotificationsSettingsFragment extends PreferenceFragment implements Constants {

    private CheckBoxPreference mAllowReviewsGCM, mAllowCommentsGCM, mAllowMessagesGCM, mAllowCommentReplyGCM;

    private ProgressDialog pDialog;

    int mAllowReviews, mAllowComments, mAllowMessages, mAllowCommentReply;

    private Boolean loading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notifications_settings);

        mAllowReviewsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowReviewsGCM");

        mAllowReviewsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowReviews = 1;

                    } else {

                        mAllowReviews = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowCommentsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentsGCM");

        mAllowCommentsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowComments = 1;

                    } else {

                        mAllowComments = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowMessagesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessagesGCM");

        mAllowMessagesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowCommentReplyGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentReplyGCM");

        mAllowCommentReplyGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowCommentReply = 1;

                    } else {

                        mAllowCommentReply = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        checkAllowReviews(App.getInstance().getAllowReviewsGCM());
        checkAllowComments(App.getInstance().getAllowCommentsGCM());
        checkAllowMessages(App.getInstance().getAllowMessagesGCM());
        checkAllowCommentReply(App.getInstance().getAllowCommentReplyGCM());
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    public void checkAllowReviews(int value) {

        if (value == 1) {

            mAllowReviewsGCM.setChecked(true);
            mAllowReviews = 1;

        } else {

            mAllowReviewsGCM.setChecked(false);
            mAllowReviews = 0;
        }
    }

    public void checkAllowComments(int value) {

        if (value == 1) {

            mAllowCommentsGCM.setChecked(true);
            mAllowComments = 1;

        } else {

            mAllowCommentsGCM.setChecked(false);
            mAllowComments = 0;
        }
    }

    public void checkAllowMessages(int value) {

        if (value == 1) {

            mAllowMessagesGCM.setChecked(true);
            mAllowMessages = 1;

        } else {

            mAllowMessagesGCM.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void checkAllowCommentReply(int value) {

        if (value == 1) {

            mAllowCommentReplyGCM.setChecked(true);
            mAllowCommentReply = 1;

        } else {

            mAllowCommentReplyGCM.setChecked(false);
            mAllowCommentReply = 0;
        }
    }

    public void saveSettings() {

        App.getInstance().setAllowReviewsGCM(mAllowReviews);
        App.getInstance().setAllowMessagesGCM(mAllowMessages);
        App.getInstance().setAllowCommentsGCM(mAllowComments);
        App.getInstance().setAllowCommentReplyGCM(mAllowCommentReply);

        App.getInstance().saveData();
    }
}