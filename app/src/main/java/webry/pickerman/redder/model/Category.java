package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import webry.pickerman.redder.constants.Constants;

public class Category extends Application implements Constants, Parcelable {

    private int id, mainCategoryId;
    private String title;

    public Category() {

    }

    public Category(JSONObject jsonData) {

        try {

            this.setId(jsonData.getInt("id"));
            this.setMainCategoryId(jsonData.getInt("mainCategoryId"));
            this.setTitle(jsonData.getString("title"));

        } catch (Throwable t) {

            Log.e("Category", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Category", jsonData.toString());
        }
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getId() {

        return this.id;
    }

    public void setMainCategoryId(int mainCategoryId) {

        this.mainCategoryId = mainCategoryId;
    }

    public int getMainCategoryId() {

        return this.mainCategoryId;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getTitle() {

        return this.title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.mainCategoryId);
        dest.writeString(this.title);
    }

    protected Category(Parcel in) {
        this.id = in.readInt();
        this.mainCategoryId = in.readInt();
        this.title = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
