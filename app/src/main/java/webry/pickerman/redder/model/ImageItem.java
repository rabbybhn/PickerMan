package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import webry.pickerman.redder.constants.Constants;

public class ImageItem extends Application implements Constants, Parcelable {

    private String selectedImageFileName, imageUrl;

    public ImageItem() {

    }

    public ImageItem(String selectedImageFileName) {

        this.setSelectedImageFileName(selectedImageFileName);
    }

    public ImageItem(String selectedImageFileName, String imageUrl) {

        this.setSelectedImageFileName(selectedImageFileName);
        this.setImageUrl(imageUrl);
    }

    public void setSelectedImageFileName(String selectedImageFileName) {

        this.selectedImageFileName = selectedImageFileName;
    }

    public String getSelectedImageFileName() {

        return this.selectedImageFileName;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {

        if (this.imageUrl == null) {

            this.imageUrl = "";
        }

        return this.imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.selectedImageFileName);
        dest.writeString(this.imageUrl);
    }

    protected ImageItem(Parcel in) {
        this.selectedImageFileName = in.readString();
        this.imageUrl = in.readString();
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
