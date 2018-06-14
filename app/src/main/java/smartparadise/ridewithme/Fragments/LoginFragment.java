package smartparadise.ridewithme.Fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import smartparadise.ridewithme.Adapters.RetrofitClient;
import smartparadise.ridewithme.DataModels.DriverLogin;
import smartparadise.ridewithme.NetworkUtils.WebApi;
import smartparadise.ridewithme.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment {


    @BindView(R.id.mobileNo)
    EditText mobileNo;
    @BindView(R.id.password)
    EditText password_et;
    @BindView(R.id.forgotPassword)
    TextView forgotPasswordTextView;
    @BindView(R.id.registerUser)
    TextView registerUser;
    @BindView(R.id.next)
    FloatingActionButton next;

    String phoneNum,password;
    Boolean isDriver;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    protected void init() {
        isDriver=getArguments().getBoolean("CHARACTER");
        phoneNum=mobileNo.getText().toString();
        password=password_et.getText().toString();




/*
        if(Patterns.PHONE.matcher(phoneNum).matches()){

        }*/
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* RetrofitClient.getClient().create(WebApi.class)
                        .login()
                        .enqueue(new Callback<DriverLogin>() {
                            @Override
                            public void onResponse(Call<DriverLogin> call, Response<DriverLogin> response) {
                                if(!response.body().error){
                                    Log.e("data", response.body().getUser().driver_licence);
                                }
                                else {
                                    Log.e("data", response.body().getMsg());
                                }
                            }

                            @Override
                            public void onFailure(Call<DriverLogin> call, Throwable t) {

                            }
                        });*/

                Bundle bundle=new Bundle();
                Fragment fragment=new DriverRegPhoneFrag();
                FragmentManager fm=getFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                bundle.putBoolean("CHARACTER",isDriver);
                fragment.setArguments(bundle);
                ft.replace(R.id.select_char_frag_container,fragment,"REG_FRAG");
                ft.addToBackStack("LOGIN_FRAG");
                ft.commit();
            }
        });
    }


}
