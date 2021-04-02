package com.sabo.sabostore.Common;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sabo.sabostore.API.APICurrency;
import com.sabo.sabostore.API.RetrofitAPI;
import com.sabo.sabostore.Model.ItemsModel;
import com.sabo.sabostore.Model.OrderModel;
import com.sabo.sabostore.Model.StoreModel;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {

    /**
     * BASE URL ExchangeRatesApi (Convert Currency)  -> latest?base=USD
     */
    private static final String URL = "http://api.exchangeratesapi.io/v1/";

    /**
     * REQUEST PERMISSION CODE
     */
    public static final int REQUEST_PERMISSION_CHANGE_PHOTO = 101;

    /**
     * Database Reference
     */
    public static final String USER_REF = "Users";
    public static final String STORE_REF = "Item-Store";
    public static final String SLIDER_REF = "Slider";
    public static final String ORDER_REF = "Order";
    public static final String DELIVERY_REF = "DeliveryCost";

    /**
     * Child Reference of USER_REF
     */
    public static final String KEY_STATUS = "status";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IMG = "image";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";

    /**
     * Child Reference of ORDER_REF
     */
    public static final String KEY_ORDER_STATUS = "orderStatus";

    /**
     * KEY SharePreference
     */
    public static final String SF_EMAIL = "sf_email";
    public static final String SF_PASSWORD = "sf_password";
    /**
     * Checkout Shipping
     */
    public static final String SF_PHONE = "sf_name";
    public static final String SF_ADDRESS = "sf_add";
    public static final String SF_ZIP = "sf_zip";
    public static final String SF_CITY = "sf_city";
    public static final String SF_SAVE_SHIPPING = "save_shipping";

    /**
     * Animation Intent
     */
    public static final String Anim_Fadein_to_Fadeout = "fadein-to-fadeout";
    public static final String Anim_Left_to_Right = "left-to-right";
    public static final String Anim_Right_to_Left = "right-to-left";
    public static final String Anim_Bottom_to_Up = "bottom-to-up";
    public static final String Anim_Up_to_Bottom = "up-to-bottom";

    /**
     * Temp Model | List | String | Int & Other...
     */
    public static FavoriteItem selectedFavorite;
    public static UserModel currentUser;
    public static StoreModel selectedStoreItems;
    public static String selectedItemImage;
    public static ItemsModel selectedItem;
    public static String favoriteItemId;
    public static String itemType;
    public static List<ItemsModel> searchItems;
    public static double deliveryCost;
    public static double minimumPriceFreeDelivery;
    public static OrderModel shipping;
    public static double totalPayment;


    /**
     * Retrofit API ExchangeRates
     */
    public static APICurrency getAPIExchangeRates() {
        return RetrofitAPI.getAPI(URL).create(APICurrency.class);
    }

    /**
     * Temp RatesIDR
     */
    public static double ratesIDR;

    /**
     * Format Price
     */
    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            return new StringBuilder().append(df.format(price)).toString();

        } else
            return "0.00";
    }

    /**
     * Format Phone Number
     */
    public static String formatPhoneNumber(Context context, String phone) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.getApplicationContext().TELEPHONY_SERVICE);
        String countryISO = tm.getNetworkCountryIso();
        String defaultCountryIso = countryISO.toUpperCase();
        Log.d("ISO", countryISO.toUpperCase());

        return PhoneNumberUtils.formatNumber(phone, defaultCountryIso);
    }

    /**
     * Create Order Number Next Int
     */
    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())         /** Get current time in millisecond */
                .append(Math.abs(new Random().nextInt()))   /** Add random number to block same order at same time */
                .toString();
    }

    /**
     * Get Date Of Week
     */
    public static String getDateOfWeek(int i) {
        switch (i) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Unknown";
        }
    }

    /** Convert Order Status to String */
    public static String covertStatus(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Canceled";
            case 1 :
                return "Ordered";
            case 2 :
                return "On Process";
            case 3:
                return "Shipped";
            case 4:
                return "Received";
            default:
                return "Unknown";
        }
    }
}
