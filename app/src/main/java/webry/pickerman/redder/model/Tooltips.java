package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import webry.pickerman.redder.constants.Constants;

public class Tooltips extends Application implements Constants, Parcelable {

    private Boolean show_flow_tooltip = true, show_select_location_tooltip = true, show_select_location_promo_tooltip = true;

    public Tooltips() {

    }

    public void setShowFlowTooltip(Boolean show_flow_tooltip) {

        this.show_flow_tooltip = show_flow_tooltip;
    }

    public Boolean isAllowShowFlowTooltip() {

        return this.show_flow_tooltip;
    }

    public void setShowSelectLocationTooltip(Boolean show_select_location_tooltip) {

        this.show_select_location_tooltip = show_select_location_tooltip;
    }

    public Boolean isAllowShowSelectLocationTooltip() {

        return this.show_select_location_tooltip;
    }

    public void setShowSelectLocationPromoTooltip(Boolean show_select_location_promo_tooltip) {

        this.show_select_location_promo_tooltip = show_select_location_promo_tooltip;
    }

    public Boolean isAllowShowSelectLocationPromoTooltip() {

        return this.show_select_location_promo_tooltip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.show_flow_tooltip);
        dest.writeValue(this.show_select_location_tooltip);
        dest.writeValue(this.show_select_location_promo_tooltip);
    }

    protected Tooltips(Parcel in) {
        this.show_flow_tooltip = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.show_select_location_tooltip = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.show_select_location_promo_tooltip = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Tooltips> CREATOR = new Creator<Tooltips>() {
        @Override
        public Tooltips createFromParcel(Parcel source) {
            return new Tooltips(source);
        }

        @Override
        public Tooltips[] newArray(int size) {
            return new Tooltips[size];
        }
    };
}
