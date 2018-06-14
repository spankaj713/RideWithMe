package smartparadise.ridewithme.Adapters;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by HP on 25-04-2018.
 */

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient(){
        if(retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl("https://e87953b8.ngrok.io")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
