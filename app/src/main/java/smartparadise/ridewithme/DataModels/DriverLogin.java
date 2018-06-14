package smartparadise.ridewithme.DataModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 27-04-2018.
 */

public class DriverLogin {

    @Expose
    @SerializedName("user")
    public User user;
    @Expose
    @SerializedName("msg")
    public String msg;
    @Expose
    @SerializedName("error")
    public boolean error;

    public User getUser() {
        return user;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isError() {
        return error;
    }

    public static class User {
        @Expose
        @SerializedName("vehical_reg_date")
        public String vehical_reg_date;
        @Expose
        @SerializedName("vehical_reg_num")
        public String vehical_reg_num;
        @Expose
        @SerializedName("vehical_model")
        public String vehical_model;
        @Expose
        @SerializedName("driver_licence")
        public String driver_licence;
        @Expose
        @SerializedName("password")
        public String password;
        @Expose
        @SerializedName("last_name")
        public String last_name;
        @Expose
        @SerializedName("first_name")
        public String first_name;
        @Expose
        @SerializedName("phone_no")
        public String phone_no;
        @Expose
        @SerializedName("uid")
        public String uid;

        public String getVehical_reg_date() {
            return vehical_reg_date;
        }

        public String getVehical_reg_num() {
            return vehical_reg_num;
        }

        public String getVehical_model() {
            return vehical_model;
        }

        public String getDriver_licence() {
            return driver_licence;
        }

        public String getPassword() {
            return password;
        }

        public String getLast_name() {
            return last_name;
        }

        public String getFirst_name() {
            return first_name;
        }

        public String getPhone_no() {
            return phone_no;
        }

        public String getUid() {
            return uid;
        }
    }
}
