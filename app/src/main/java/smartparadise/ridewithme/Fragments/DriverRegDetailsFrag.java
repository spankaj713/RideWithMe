package smartparadise.ridewithme.Fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartparadise.ridewithme.Activities.BaseActivity;
import smartparadise.ridewithme.Activities.CustomerMapActivity;
import smartparadise.ridewithme.Adapters.RetrofitClient;
import smartparadise.ridewithme.DataModels.RegisterPojo;
import smartparadise.ridewithme.NetworkUtils.WebApi;
import smartparadise.ridewithme.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DriverRegDetailsFrag extends BaseFragment {


    String userId, phoneNum;

    @BindView(R.id.driverFirstName)
    EditText driverFirstName;
    @BindView(R.id.driverLastName)
    EditText driverLastName;
    @BindView(R.id.reg_email)
    EditText regEmail;
    @BindView(R.id.nextDetails)
    FloatingActionButton nextDetails;
    boolean isDriver;
    String firstName, lastName, email;
    DatabaseReference mRef;

    @Override
    protected int getLayoutId() {
        return R.layout.driver_reg_details;
    }

    @Override
    protected void init() {
        userId = getArguments().getString("UID");
        phoneNum = getArguments().getString("PHONE_NUM");
        isDriver=getArguments().getBoolean("CHARACTER");

        if(!isDriver){
            nextDetails.setImageResource(R.drawable.check);
        }
        firstName = driverFirstName.getText().toString();
        lastName = driverLastName.getText().toString();
        email=regEmail.getText().toString();
        Log.e("f_n",firstName);
        Log.e("l_n",lastName);
        Log.e("email_n",email);

        nextDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(driverFirstName.getText().toString().trim())||TextUtils.isEmpty(driverLastName.getText().toString().trim())||TextUtils.isEmpty(regEmail.getText().toString().trim())) {
                    Toast.makeText(getActivity(), "Enter all Details", Toast.LENGTH_SHORT).show();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(regEmail.getText().toString().trim()).matches()){
                    Toast.makeText(getActivity(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
                }
                else {
                    ProgressDialog dialog=new ProgressDialog(getActivity());
                    dialog.setTitle("RideWithMe");
                    dialog.setMessage("Registering Device");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    dialog.show();
                    if (!isDriver) {
                        RetrofitClient.getClient().create(WebApi.class)
                                .register(userId,phoneNum,"Rider")
                                .enqueue(new Callback<RegisterPojo>() {
                                    @Override
                                    public void onResponse(Call<RegisterPojo> call, Response<RegisterPojo> response) {
                                        if(!response.body().isError()){
                                            saveRidersInfo();
                                            Toast.makeText(getActivity(),response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<RegisterPojo> call, Throwable t) {

                                    }
                                });
                        dialog.dismiss();
                        startActivity(new Intent(getActivity(), CustomerMapActivity.class));
                        getActivity().finish();


                    } else {

                        Fragment fragment = new DriverVehicalDetailFrag();
                        Bundle bundle = new Bundle();
                        bundle.putString("FIRST_NAME",driverFirstName.getText().toString().trim());
                        bundle.putString("LAST_NAME",driverLastName.getText().toString().trim());
                        bundle.putString("EMAIL",regEmail.getText().toString().trim());
                        bundle.putString("PHONE_NO",phoneNum);
                        bundle.putString("UID", userId);
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        ft.replace(R.id.select_char_frag_container, fragment, "VEHICLE_INFO");
                        ft.addToBackStack("DETAIL_FRAG");
                        ft.commit();
                    }
                }

            }
        });
    }
    private void saveRidersInfo() {
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(userId);
        Map userInfo = new HashMap();
        userInfo.put("first_name",driverFirstName.getText().toString().trim());
        userInfo.put("last_name",driverLastName.getText().toString().trim());
        userInfo.put("phone",phoneNum.substring(2));
        userInfo.put("email",regEmail.getText().toString().trim());
        mRef.updateChildren(userInfo);
    }
}
