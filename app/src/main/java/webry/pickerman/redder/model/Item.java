package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import webry.pickerman.redder.constants.Constants;


public class Item extends Application implements Constants, Parcelable {

    private long id = 0, fromUserId = 0;
    private int moderateAt, inactiveAt, rejectedAt, rejectedId, createAt, likesCount, commentsCount, allowComments, categoryId, price, currency, subcategoryId;
    private String location, timeAgo, date, categoryTitle, itemTitle, itemDescription, itemContent, previewImgUrl, imgUrl, fromUserUsername, fromUserFullname, fromUserPhotoUrl, area, country, city, phone;
    private Double lat = 0.000000, lng = 0.000000;
    private Boolean myLike; // Favorites
    private String phoneNumber;
    private String itemUrl;
    private int appType, rating, viewsCount, phoneViewsCount;

    public Item() {

    }

    public Item(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setFromUserId(jsonData.getLong("fromUserId"));
                this.setFromUserUsername(jsonData.getString("fromUserUsername"));
                this.setFromUserFullname(jsonData.getString("fromUserFullname"));
                this.setFromUserPhotoUrl(jsonData.getString("fromUserPhoto"));
                this.setFromUserPhone(jsonData.getString("fromUserPhone"));
                this.setContent(jsonData.getString("itemContent"));
                this.setTitle(jsonData.getString("itemTitle"));
                this.setDescription(jsonData.getString("itemDesc"));
                this.setCategoryTitle(jsonData.getString("categoryTitle"));
                this.setCategoryId(jsonData.getInt("category"));
                this.setPrice(jsonData.getInt("price"));
                this.setImgUrl(jsonData.getString("imgUrl"));
                this.setPreviewImgUrl(jsonData.getString("previewImgUrl"));
                this.setArea(jsonData.getString("area"));
                this.setCountry(jsonData.getString("country"));
                this.setCity(jsonData.getString("city"));
                this.setAllowComments(jsonData.getInt("allowComments"));
                this.setCommentsCount(jsonData.getInt("commentsCount"));
                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setMyLike(jsonData.getBoolean("myLike"));
                this.setLat(jsonData.getDouble("lat"));
                this.setLng(jsonData.getDouble("lng"));
                this.setCreateAt(jsonData.getInt("createAt"));
                this.setDate(jsonData.getString("date"));
                this.setTimeAgo(jsonData.getString("timeAgo"));

                if (jsonData.has("currency")) {

                    this.setCurrency(jsonData.getInt("currency"));
                }

                if (jsonData.has("phoneNumber")) {

                    this.setPhoneNumber(jsonData.getString("phoneNumber"));
                }

                if (jsonData.has("moderatedAt")) {

                    this.setModerateAt(jsonData.getInt("moderatedAt"));
                }

                if (jsonData.has("inactiveAt")) {

                    this.setInactiveAt(jsonData.getInt("inactiveAt"));
                }

                if (jsonData.has("rejectedAt")) {

                    this.setRejectedAt(jsonData.getInt("rejectedAt"));
                }

                if (jsonData.has("rejectedId")) {

                    this.setRejectedId(jsonData.getInt("rejectedId"));
                }

                if (jsonData.has("location")) {

                    this.setLocation(jsonData.getString("location"));
                }

                if (jsonData.has("subcategory")) {

                    this.setSubcategoryId(jsonData.getInt("subcategory"));
                }

                if (jsonData.has("appType")) {

                    this.setAppType(jsonData.getInt("appType"));
                }

                if (jsonData.has("itemUrl")) {

                    this.setItemUrl(jsonData.getString("itemUrl"));
                }

                if (jsonData.has("viewsCount")) {

                    this.setViewsCount(jsonData.getInt("viewsCount"));
                }

                if (jsonData.has("phoneViewsCount")) {

                    this.setPhoneViewsCount(jsonData.getInt("phoneViewsCount"));
                }

                if (jsonData.has("rating")) {

                    this.setRating(jsonData.getInt("rating"));
                }
            }

        } catch (Throwable t) {

            Log.e("Item", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Item", jsonData.toString());
        }
    }


    public long getId() {

        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFromUserId() {

        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public int getAllowComments() {

        return allowComments;
    }

    public void setAllowComments(int allowComments) {
        this.allowComments = allowComments;
    }


    public int getCommentsCount() {

        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getLikesCount() {

        return this.likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getViewsCount() {

        return this.viewsCount;
    }

    public void setViewsCount(int viewsCount) {

        this.viewsCount = viewsCount;
    }

    public int getPhoneViewsCount() {

        return this.phoneViewsCount;
    }

    public void setPhoneViewsCount(int phoneViewsCount) {

        this.phoneViewsCount = phoneViewsCount;
    }

    public int getRating() {

        return this.rating;
    }

    public void setRating(int rating) {

        this.rating = rating;
    }

    public int getCategoryId() {

        return this.categoryId;
    }

    public void setCategoryId(int categoryId) {

        this.categoryId = categoryId;
    }

    public int getSubcategoryId() {

        return this.subcategoryId;
    }

    public void setSubcategoryId(int subcategoryId) {

        this.subcategoryId = subcategoryId;
    }

    public int getAppType() {

        return this.appType;
    }

    public void setAppType(int appType) {

        this.appType = appType;
    }

    public int getPrice() {

        return this.price;
    }

    public void setPrice(int price) {

        this.price = price;
    }

    public int getCurrency() {

        return this.currency;
    }

    public void setCurrency(int currency) {

        this.currency = currency;
    }

    public int getCreateAt() {

        return createAt;
    }

    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public int getModerateAt() {

        return this.moderateAt;
    }

    public void setModerateAt(int moderateAt) {

        this.moderateAt = moderateAt;
    }

    public int getInactiveAt() {

        return this.inactiveAt;
    }

    public void setInactiveAt(int inactiveAt) {

        this.inactiveAt = inactiveAt;
    }

    public int getRejectedAt() {

        return this.rejectedAt;
    }

    public void setRejectedAt(int rejectedAt) {

        this.rejectedAt = rejectedAt;
    }

    public int getRejectedId() {

        return this.rejectedId;
    }

    public void setRejectedId(int rejectedId) {

        this.rejectedId = rejectedId;
    }

    public String getTimeAgo() {

        if (this.timeAgo == null) {

            this.timeAgo = "";
        }

        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }


    public String getContent() {

        if (this.itemContent == null) {

            this.itemContent = "";
        }

        return itemContent;
    }

    public void setContent(String itemContent) {

        this.itemContent = itemContent;
    }

    public String getTitle() {

        if (this.itemTitle == null) {

            this.itemTitle = "";
        }

        return itemTitle;
    }

    public void setTitle(String itemTitle) {

        this.itemTitle = itemTitle;
    }

    public String getDescription() {

        if (this.itemDescription == null) {

            this.itemDescription = "";
        }

        return itemDescription;
    }

    public void setDescription(String itemDescription) {

        this.itemDescription = itemDescription;
    }

    public String getCategoryTitle() {

        if (this.categoryTitle == null) {

            this.categoryTitle = "";
        }

        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }


    public String getFromUserUsername() {
        return fromUserUsername;
    }

    public void setFromUserUsername(String fromUserUsername) {
        this.fromUserUsername = fromUserUsername;
    }


    public String getFromUserFullname() {

        if (this.fromUserFullname == null) {

            this.fromUserFullname = "";
        }

        return fromUserFullname;
    }

    public void setFromUserFullname(String fromUserFullname) {
        this.fromUserFullname = fromUserFullname;
    }


    public String getFromUserPhotoUrl() {

        if (fromUserPhotoUrl == null) {

            fromUserPhotoUrl = "";
        }

        return fromUserPhotoUrl;
    }

    public void setFromUserPhotoUrl(String fromUserPhotoUrl) {
        this.fromUserPhotoUrl = fromUserPhotoUrl;
    }

    public String getFromUserPhone() {

        if (phone == null) {

            phone = "";
        }

        return phone;
    }

    public void setFromUserPhone(String fromUserPhone) {

        this.phone = fromUserPhone;
    }


    public Boolean isMyLike() {
        return myLike;
    }

    public void setMyLike(Boolean myLike) {

        this.myLike = myLike;
    }

    public String getImgUrl() {

        if (this.imgUrl == null) {

            this.imgUrl = "";
        }

        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {

        this.imgUrl = imgUrl;
    }

    public String getPreviewImgUrl() {

        if (this.previewImgUrl == null) {

            this.previewImgUrl = "";
        }

        return previewImgUrl;
    }

    public void setPreviewImgUrl(String previewImgUrl) {

        this.previewImgUrl = previewImgUrl;
    }

    public String getDate() {

        return date;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getArea() {

        if (this.area == null) {

            this.area = "";
        }

        return this.area;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getCountry() {

        if (this.country == null) {

            this.country = "";
        }

        return this.country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCity() {

        if (this.city == null) {

            this.city = "";
        }

        return this.city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public Double getLat() {

        return this.lat;
    }

    public void setLat(Double lat) {

        this.lat = lat;
    }

    public Double getLng() {

        return this.lng;
    }

    public void setLng(Double lng) {

        this.lng = lng;
    }

    public String getItemUrl() {

        if (this.itemUrl == null) {

            this.itemUrl = "";
        }

        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {

        this.itemUrl = itemUrl;
    }

    public String getLink() {

        return WEB_SITE + "/classified/" + this.getItemUrl();
    }

    public String getPhoneNumber() {

        if (this.phoneNumber == null) {

            this.phoneNumber = "";
        }

        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {

        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {

        if (this.location == null) {

            this.location = "";
        }

        return this.location;
    }

    public void setLocation(String location) {

        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.fromUserId);
        dest.writeInt(this.moderateAt);
        dest.writeInt(this.inactiveAt);
        dest.writeInt(this.rejectedAt);
        dest.writeInt(this.rejectedId);
        dest.writeInt(this.createAt);
        dest.writeInt(this.likesCount);
        dest.writeInt(this.commentsCount);
        dest.writeInt(this.allowComments);
        dest.writeInt(this.categoryId);
        dest.writeInt(this.price);
        dest.writeInt(this.currency);
        dest.writeInt(this.subcategoryId);
        dest.writeString(this.location);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
        dest.writeString(this.categoryTitle);
        dest.writeString(this.itemTitle);
        dest.writeString(this.itemDescription);
        dest.writeString(this.itemContent);
        dest.writeString(this.previewImgUrl);
        dest.writeString(this.imgUrl);
        dest.writeString(this.fromUserUsername);
        dest.writeString(this.fromUserFullname);
        dest.writeString(this.fromUserPhotoUrl);
        dest.writeString(this.area);
        dest.writeString(this.country);
        dest.writeString(this.city);
        dest.writeString(this.phone);
        dest.writeValue(this.lat);
        dest.writeValue(this.lng);
        dest.writeValue(this.myLike);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.itemUrl);
        dest.writeInt(this.appType);
        dest.writeInt(this.rating);
        dest.writeInt(this.viewsCount);
        dest.writeInt(this.phoneViewsCount);
    }

    protected Item(Parcel in) {
        this.id = in.readLong();
        this.fromUserId = in.readLong();
        this.moderateAt = in.readInt();
        this.inactiveAt = in.readInt();
        this.rejectedAt = in.readInt();
        this.rejectedId = in.readInt();
        this.createAt = in.readInt();
        this.likesCount = in.readInt();
        this.commentsCount = in.readInt();
        this.allowComments = in.readInt();
        this.categoryId = in.readInt();
        this.price = in.readInt();
        this.currency = in.readInt();
        this.subcategoryId = in.readInt();
        this.location = in.readString();
        this.timeAgo = in.readString();
        this.date = in.readString();
        this.categoryTitle = in.readString();
        this.itemTitle = in.readString();
        this.itemDescription = in.readString();
        this.itemContent = in.readString();
        this.previewImgUrl = in.readString();
        this.imgUrl = in.readString();
        this.fromUserUsername = in.readString();
        this.fromUserFullname = in.readString();
        this.fromUserPhotoUrl = in.readString();
        this.area = in.readString();
        this.country = in.readString();
        this.city = in.readString();
        this.phone = in.readString();
        this.lat = (Double) in.readValue(Double.class.getClassLoader());
        this.lng = (Double) in.readValue(Double.class.getClassLoader());
        this.myLike = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.phoneNumber = in.readString();
        this.itemUrl = in.readString();
        this.appType = in.readInt();
        this.rating = in.readInt();
        this.viewsCount = in.readInt();
        this.phoneViewsCount = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
