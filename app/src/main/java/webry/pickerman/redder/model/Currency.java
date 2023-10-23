package webry.pickerman.redder.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import webry.pickerman.redder.constants.Constants;

public class Currency extends Application implements Constants, Parcelable {

    private String code, name, symbol;

    public Currency() {

    }

    public Currency(JSONObject jsonData) {

        try {

            this.setCode(jsonData.getString("code"));
            this.setName(jsonData.getString("name"));
            this.setSymbol(jsonData.getString("symbol"));

        } catch (Throwable t) {

            Log.e("Currency", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Currency", jsonData.toString());
        }
    }

    public void setCode(String code) {

        this.code = code;
    }

    public String getCode() {

        return this.code;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return this.name;
    }

    public void setSymbol(String symbol) {

        this.symbol = symbol;
    }

    public String getSymbol() {

        return this.symbol;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(this.code);
        dest.writeString(this.name);
        dest.writeString(this.symbol);
    }

    protected Currency(Parcel in) {

        this.code = in.readString();
        this.name = in.readString();
        this.symbol = in.readString();
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
}
