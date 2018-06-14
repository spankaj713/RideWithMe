package smartparadise.ridewithme.DataModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 05-05-2018.
 */

public class IsPhoneExist {

    public boolean isResult() {
        return result;
    }

    public boolean isError() {
        return error;
    }

    @Expose
    @SerializedName("result")
    public boolean result;
    @Expose
    @SerializedName("error")
    public boolean error;
}

