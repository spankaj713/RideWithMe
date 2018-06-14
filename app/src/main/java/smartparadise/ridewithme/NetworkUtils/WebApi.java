package smartparadise.ridewithme.NetworkUtils;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import smartparadise.ridewithme.DataModels.DriverLogin;
import smartparadise.ridewithme.DataModels.IsPhoneExist;
import smartparadise.ridewithme.DataModels.RegisterPojo;

/**
 * Created by HP on 25-04-2018.
 */

public interface WebApi {

    @GET("/ridewithme/test.php")
    Call<DriverLogin> login();

    @FormUrlEncoded
    @POST("/ridewithme/RegisterDevice.php")
    Call<RegisterPojo>register(@Field("uid")String uid,
                               @Field("phone_no")String phone_no,
                               @Field("role")String role);


    @FormUrlEncoded
    @POST("/ridewithme/isPhoneExist.php")
    Call<IsPhoneExist>alreadyRegistered(@Field("phone_no")String phone_no);

}
