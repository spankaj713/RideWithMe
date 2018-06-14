package smartparadise.ridewithme.DataModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 27-04-2018.
 */

public class RegisterPojo {

    @Expose
    @SerializedName("message")
    public String message;
    @Expose
    @SerializedName("error")
    public boolean error;

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }
}
