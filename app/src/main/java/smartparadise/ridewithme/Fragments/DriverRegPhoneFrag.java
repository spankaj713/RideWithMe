package smartparadise.ridewithme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import butterknife.BindView;
import smartparadise.ridewithme.Activities.DriverMapActivity;
import smartparadise.ridewithme.Activities.CustomerMapActivity;
import smartparadise.ridewithme.R;


public class DriverRegPhoneFrag extends BaseFragment {



    @BindView(R.id.floatNext)
    FloatingActionButton floatNext;

    @BindView(R.id.countryPicker)
    CountryCodePicker countryPicker;

    @BindView(R.id.carrierNum)
    EditText carrierNum;

    String phoneNum="";



    boolean isDriver;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_driver_reg_phone;
    }

    @Override
    protected void init() {
        isDriver=getArguments().getBoolean("CHARACTER");
        floatNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               verifyPhone();
            }
        });
    }

    public void verifyPhone(){

        countryPicker.registerCarrierNumberEditText(carrierNum);

        phoneNum=countryPicker.getFullNumber();

        if(Patterns.PHONE.matcher(phoneNum).matches()){
            Bundle bundle=new Bundle();
            bundle.putString("PHONE_NO",phoneNum);
            bundle.putBoolean("CHARACTER",isDriver);
            FragmentManager fragmentManager=getFragmentManager();
            FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
            Fragment fragment=new DriverOTPfrag();
            fragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.select_char_frag_container,fragment,"OTP_FRAG");
            fragmentTransaction.addToBackStack("REG_FRAG");
            fragmentTransaction.commit();
        }
        else if(phoneNum==""){
            Toast.makeText(getActivity(), "Enter Phone Number first!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getActivity(), "Invalid Phone Number!", Toast.LENGTH_SHORT).show();
        }

    }
}
