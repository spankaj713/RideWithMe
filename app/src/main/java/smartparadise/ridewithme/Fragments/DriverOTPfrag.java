package smartparadise.ridewithme.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartparadise.ridewithme.Activities.CustomerMapActivity;
import smartparadise.ridewithme.Activities.DriverMapActivity;
import smartparadise.ridewithme.Adapters.RetrofitClient;
import smartparadise.ridewithme.DataModels.IsPhoneExist;
import smartparadise.ridewithme.DataModels.RegisterPojo;
import smartparadise.ridewithme.NetworkUtils.WebApi;
import smartparadise.ridewithme.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class DriverOTPfrag extends BaseFragment {


    @BindView(R.id.resendOTP)
    TextView resendOTP;

    @BindView(R.id.otp_et)
    EditText codeText;

    @BindView(R.id.floatNext1)
    FloatingActionButton floatNext1;

    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String phoneVerificationId;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    String phoneNum;

    ProgressDialog progressDialog;
    PhoneAuthCredential credential;
    boolean isDriver;

    Bundle bundle;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_driver_otpfrag;
    }

    @Override
    protected void init() {
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setTitle("Verifying Phone No.");
        progressDialog.setMessage("Please Wait..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        isDriver=getArguments().getBoolean("CHARACTER");
        phoneNum = getArguments().getString("PHONE_NO");
        mAuth = FirebaseAuth.getInstance();

        setUpVerificationCallbacks();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNum,
                    60,
                    TimeUnit.SECONDS,
                    getActivity(),
                    mCallbacks
            );


            floatNext1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verifyCode();

                }
            });
            resendOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resendCode();
                }
            });
        }

    public void setUpVerificationCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getContext(), phoneAuthCredential.toString(), Toast.LENGTH_SHORT).show();
              //  mAuth.getCurrentUser().unlink(PhoneAuthProvider.PROVIDER_ID);

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                sharedPreferences.edit()
                .putString("user",userId).putBoolean("CHARACTER",isDriver)
                .apply();
                Log.e("shared Pref",sharedPreferences.getString("user","").toString());
                progressDialog.dismiss();
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getContext(), "Phone number couldn't verified!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressDialog.dismiss();
                Toast.makeText(getContext(), "OTP has been sent", Toast.LENGTH_SHORT).show();
                resendingToken = forceResendingToken;
                phoneVerificationId = s;

            }
        };

    }

    String userId;
    private void signIn(final PhoneAuthCredential phoneAuthCredential) {

        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getContext(),"Invalid Credetials",Toast.LENGTH_SHORT).show();
                }else{
                    bundle=new Bundle();


                    if(isDriver){
                        userId=mAuth.getCurrentUser().getUid();
                        Log.e("USer",userId.toString());
                        DatabaseReference database=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child(phoneNum);
                        database.setValue(phoneNum);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                        sharedPreferences.edit()
                                .putString("user",userId).putBoolean("CHARACTER",isDriver).apply();
                        Log.e("shared Pref",sharedPreferences.getString("user","").toString());
                        bundle.putString("PHONE_NUM",phoneNum);
                        bundle.putBoolean("CHARACTER",isDriver);
                        bundle.putString("UID",userId);
                        RetrofitClient.getClient().create(WebApi.class)
                                .alreadyRegistered(phoneNum).enqueue(new Callback<IsPhoneExist>() {
                            @Override
                            public void onResponse(Call<IsPhoneExist> call, Response<IsPhoneExist> response) {
                                if(!response.body().error){
                                    if(!response.body().isResult()){
                                        Fragment fragment= new DriverRegDetailsFrag();
                                        fragment.setArguments(bundle);
                                        FragmentManager fragmentManager=getFragmentManager();
                                        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.select_char_frag_container,fragment,"REG_DETAIL");
                                        fragmentTransaction.addToBackStack("OTP_FRAG");
                                        fragmentTransaction.commit();
                                    }else{
                                        startActivity((new Intent(getActivity(), DriverMapActivity.class)).putExtra("PHONE_BUNDLE",bundle));
                                        getActivity().finish();
                                        return;
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<IsPhoneExist> call, Throwable t) {

                            }
                        });


                    }else{
                        userId=mAuth.getCurrentUser().getUid();
                        DatabaseReference database=FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(userId).child(phoneNum);
                        database.setValue(phoneNum);

                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                        sharedPreferences.edit()
                                .putString("user",userId).putBoolean("CHARACTER",isDriver).apply();
                        bundle.putString("PHONE_NUM",phoneNum);
                        bundle.putBoolean("CHARACTER",isDriver);
                        bundle.putString("UID",userId);
                        RetrofitClient.getClient().create(WebApi.class)
                                .alreadyRegistered(phoneNum).enqueue(new Callback<IsPhoneExist>() {
                            @Override
                            public void onResponse(Call<IsPhoneExist> call, Response<IsPhoneExist> response) {
                                if(!response.body().error){
                                    if(!response.body().isResult()){
                                        Fragment fragment= new DriverRegDetailsFrag();
                                        fragment.setArguments(bundle);
                                        FragmentManager fragmentManager=getFragmentManager();
                                        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.select_char_frag_container,fragment,"REG_DETAIL");
                                        fragmentTransaction.addToBackStack("OTP_FRAG");
                                        fragmentTransaction.commit();
                                    }else{
                                        startActivity((new Intent(getActivity(), CustomerMapActivity.class)).putExtra("PHONE_BUNDLE",bundle));
                                        getActivity().finish();

                                        return;

                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<IsPhoneExist> call, Throwable t) {

                            }
                        });
                                            }

                }
            }
        });
    }


    public void verifyCode(){
        String code=codeText.getText().toString();
        credential= PhoneAuthProvider.getCredential(phoneVerificationId,code);
        signIn(credential);

    }

    public void resendCode(){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNum,
                60,
                TimeUnit.SECONDS,
                getActivity(),
                mCallbacks,
                resendingToken
        );

    }
}
