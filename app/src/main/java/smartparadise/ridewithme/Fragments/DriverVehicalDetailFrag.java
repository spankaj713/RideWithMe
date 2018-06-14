package smartparadise.ridewithme.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartparadise.ridewithme.Activities.CustomerProfileActivity;
import smartparadise.ridewithme.Activities.DriverMapActivity;
import smartparadise.ridewithme.Adapters.RetrofitClient;
import smartparadise.ridewithme.DataModels.RegisterPojo;
import smartparadise.ridewithme.NetworkUtils.WebApi;
import smartparadise.ridewithme.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverVehicalDetailFrag extends BaseFragment {


    @BindView(R.id.vehicleModel)
    EditText vehicleModel;
    @BindView(R.id.vehicleRegNum)
    EditText vehicleRegNum;

    @BindView(R.id.driverLicence)
    EditText driverLicence;
    @BindView(R.id.next)
    FloatingActionButton next;
    String model,reg_num,d_licence;
    String uid,firstName,lastName,phoneNum,email;
    DatabaseReference mRef;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_driver_vehical_detail;
    }

    @Override
    protected void init() {

        /*model=vehicleModel.getText().toString();
        reg_num=vehicleRegNum.getText().toString();
        d_licence=driverLicence.getText().toString();*/
        uid=getArguments().getString("UID");
        firstName=getArguments().getString("FIRST_NAME");
        lastName=getArguments().getString("LAST_NAME");
        phoneNum=getArguments().getString("PHONE_NO");
        email=getArguments().getString("EMAIL");
        next.setImageResource(R.drawable.check);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });


    }

    private void registerUser() {

        if(TextUtils.isEmpty(vehicleModel.getText().toString().trim())||TextUtils.isEmpty(vehicleRegNum.getText().toString().trim())||TextUtils.isEmpty(driverLicence.getText().toString().trim())){
            Toast.makeText(getActivity(), "Fill all details first!", Toast.LENGTH_SHORT).show();
        }else{
            ProgressDialog dialog=new ProgressDialog(getActivity());
            dialog.setTitle("RideWithMe");
            dialog.setMessage("Registering Device");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            RetrofitClient.getClient().create(WebApi.class)
                    .register(uid,phoneNum,"driver")
                    .enqueue(new Callback<RegisterPojo>() {
                        @Override
                        public void onResponse(Call<RegisterPojo> call, Response<RegisterPojo> response) {
                            if(!response.body().isError()){
                                saveDriverInfo();
                                Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getActivity(),response.body().getMessage() , Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterPojo> call, Throwable t) {

                        }
                    });
            dialog.dismiss();
            startActivity(new Intent(getActivity(), DriverMapActivity.class));
            getActivity().finish();
        }
    }
    private void saveDriverInfo() {
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(uid);
        Map userInfo = new HashMap();
        userInfo.put("first_name",firstName);
        userInfo.put("last_name",lastName);
        userInfo.put("phone",phoneNum);
        userInfo.put("email",email);
        /*userInfo.put("driver_licence",driverLicence.getText().toString().trim());
        userInfo.put("vehical_model",vehicleModel.getText().toString().trim());
        userInfo.put("vehical_reg_num",vehicleRegNum.getText().toString().trim());*/
        mRef.updateChildren(userInfo);


        }
    }

