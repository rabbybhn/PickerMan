package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import webry.pickerman.redder.constants.Constants;

public class Review extends Application implements Constants, Parcelable {

    private long id, fromUserId, toUserId;
    private int fromUserState, createAt, rank;
    private String review, answer, fromUserUsername, fromUserFullname, fromUserPhotoUrl, timeAgo;

    public Review() {

    }

    public Review(JSONObject jsonData) {

        try {

            this.setId(jsonData.getLong("id"));
            this.setFromUserId(jsonData.getLong("fromUserId"));
            this.setFromUserState(jsonData.getInt("fromUserState"));
            this.setFromUserUsername(jsonData.getString("fromUserUsername"));
            this.setFromUserFullname(jsonData.getString("fromUserFullname"));
            this.setFromUserPhotoUrl(jsonData.getString("fromUserPhotoUrl"));
            this.setToUserId(jsonData.getLong("tooUserId"));
            this.setReview(jsonData.getString("review"));
            this.setAnswer(jsonData.getString("answer"));
            this.setTimeAgo(jsonData.getString("timeAgo"));
            this.setCreateAt(jsonData.getInt("createAt"));

            this.setRank(jsonData.getInt("rank"));

        } catch (Throwable t) {

            Log.e("Review", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Review", jsonData.toString());
        }
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getId() {

        return this.id;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public long getFromUserId() {

        return this.fromUserId;
    }

    public void setToUserId(long toUserId) {

        this.toUserId = toUserId;
    }

    public long getToUserId() {

        return this.toUserId;
    }

    public void setFromUserState(int fromUserState) {

        this.fromUserState = fromUserState;
    }

    public int getFromUserState() {

        return this.fromUserState;
    }

    public void setReview(String review) {

        this.review = review;
    }

    public String getReview() {

        return this.review;
    }

    public void setAnswer(String answer) {

        this.answer = answer;
    }

    public String getAnswer() {

        return this.answer;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public String getTimeAgo() {

        return this.timeAgo;
    }

    public void setFromUserUsername(String fromUserUsername) {

        this.fromUserUsername = fromUserUsername;
    }

    public String getFromUserUsername() {

        return this.fromUserUsername;
    }

    public void setFromUserFullname(String fromUserFullname) {

        this.fromUserFullname = fromUserFullname;
    }

    public String getFromUserFullname() {

        return this.fromUserFullname;
    }

    public void setFromUserPhotoUrl(String fromUserPhotoUrl) {

        this.fromUserPhotoUrl = fromUserPhotoUrl;
    }

    public String getFromUserPhotoUrl() {

        return this.fromUserPhotoUrl;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getCreateAt() {

        return this.createAt;
    }

    public void setRank(int rank) {

        this.rank = rank;
    }

    public int getRank() {

        return this.rank;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
    public static final Creator CREATOR = new Creator() {

        public Review createFromParcel(Parcel in) {

            return new Review();
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
