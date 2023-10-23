package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import webry.pickerman.redder.constants.Constants;

public class SearchFilters extends Application implements Constants, Parcelable {

    private int sortType = 0, moderationType = 0, distance = 25;
    private int categoryId = 0;
    private Double lat = 0.000000, lng = 0.000000;
    private String location = "";

    public SearchFilters() {

    }

    public void setSortType(int sortType) {

        this.sortType = sortType;
    }

    public int getSortType() {

        return this.sortType;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    public String getLocation() {

        if (this.location == null) {

            this.location = "";
        }

        return this.location;
    }

    public void setModerationType(int moderationType) {

        this.moderationType = moderationType;
    }

    public int getModerationType() {

        return this.moderationType;
    }

    public void setCategoryId(int categoryId) {

        this.categoryId = categoryId;
    }

    public int getCategoryId() {

        return this.categoryId;
    }

    public void setDistance(int distance) {

        this.distance = distance;
    }

    public int getDistance() {

        return this.distance;
    }

    public void setLat(Double lat) {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        this.lat = lat;
    }

    public Double getLat() {

        if (this.lat == null) {

            this.lat = 0.000000;
        }

        return this.lat;
    }

    public void setLng(Double lng) {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        this.lng = lng;
    }

    public Double getLng() {

        if (this.lng == null) {

            this.lng = 0.000000;
        }

        return this.lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.sortType);
        dest.writeInt(this.moderationType);
        dest.writeInt(this.distance);
        dest.writeInt(this.categoryId);
        dest.writeValue(this.lat);
        dest.writeValue(this.lng);
        dest.writeString(this.location);
    }

    protected SearchFilters(Parcel in) {
        this.sortType = in.readInt();
        this.moderationType = in.readInt();
        this.distance = in.readInt();
        this.categoryId = in.readInt();
        this.lat = (Double) in.readValue(Double.class.getClassLoader());
        this.lng = (Double) in.readValue(Double.class.getClassLoader());
        this.location = in.readString();
    }

    public static final Creator<SearchFilters> CREATOR = new Creator<SearchFilters>() {
        @Override
        public SearchFilters createFromParcel(Parcel source) {
            return new SearchFilters(source);
        }

        @Override
        public SearchFilters[] newArray(int size) {
            return new SearchFilters[size];
        }
    };
}
