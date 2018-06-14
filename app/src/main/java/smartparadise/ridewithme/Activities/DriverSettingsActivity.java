package smartparadise.ridewithme.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import smartparadise.ridewithme.R;

public class DriverSettingsActivity extends BaseActivity {


    @BindView(R.id.profileImage)
    CircleImageView profileImage;


    @BindView(R.id.confirmButton)
    Button confirmButton;
    @BindView(R.id.takePic)
    FloatingActionButton takePic;
    @BindView(R.id.driverFirstName)
    EditText driverFirstName;
    @BindView(R.id.driverLastName)
    EditText driverLastName;
    @BindView(R.id.driverPhone)
    EditText driverPhone;
    @BindView(R.id.driverEmail)
    EditText driverEmail;
    @BindView(R.id.driver_licence)
    EditText driverLicence;
    @BindView(R.id.vehical_Model)
    EditText vehicalModel;
    @BindView(R.id.vehical_reg_no)
    EditText vehicalRegNo;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userId;
    private String first_name,last_name, phone,driver_licence,email,vehicle_model,vehicle_reg_no, profileImageUrl;
    ProgressDialog dialog;
    Bitmap imageBitmap = null;

    @Override
    protected void init() {
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
        getDriverInfo();

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog=new ProgressDialog(DriverSettingsActivity.this);
                dialog.setTitle("RideWithMe");
                dialog.setMessage("Updating Profile..");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                saveDriverInfo();
            }

        });

    }

    private void saveDriverInfo() {
        first_name = driverFirstName.getText().toString();
        last_name = driverLastName.getText().toString();
        phone = driverPhone.getText().toString();
        email=driverEmail.getText().toString();
        driver_licence = driverLicence.getText().toString();
        vehicle_model=vehicalModel.getText().toString();
        vehicle_reg_no=vehicalRegNo.getText().toString();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            dialog.dismiss();
            Toast.makeText(this, "Invalid Email!", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.PHONE.matcher(phone).matches()){
            dialog.dismiss();
            Toast.makeText(this, "Invalid Phone no.", Toast.LENGTH_SHORT).show();
        }else{
            Map driverInfo = new HashMap();
            driverInfo.put("first_name",first_name);
            driverInfo.put("last_name",last_name);
            driverInfo.put("phone",phone);
            driverInfo.put("email",email);
            driverInfo.put("driver_licence",driver_licence);
            driverInfo.put("vehical_model",vehicle_model);
            driverInfo.put("vehical_reg_num",vehicle_reg_no);
            mRef.updateChildren(driverInfo);

            if (resultUri != null) {
                StorageReference storagePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
                Log.e("StoragePath", storagePath.toString());

                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] imageBytes = baos.toByteArray();
                UploadTask uploadTask = storagePath.putBytes(imageBytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Map imageURL = new HashMap();
                        imageURL.put("ProfileImage", downloadUrl.toString());
                        mRef.updateChildren(imageURL);
                        dialog.dismiss();
                        Toast.makeText(DriverSettingsActivity.this, "Information Saved Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DriverSettingsActivity.this, "Image couldn't be saved! Try Again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(DriverSettingsActivity.this, "Information Saved Successfully", Toast.LENGTH_SHORT).show();

                finish();

            }
        }

    }

    private void getDriverInfo() {

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("first_name") != null) {
                        first_name = map.get("first_name").toString();
                        driverFirstName.setText(first_name);
                    }
                    if (map.get("last_name") != null) {
                        last_name = map.get("last_name").toString();
                        driverLastName.setText(last_name);
                    }
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        driverPhone.setText(phone);
                    }
                    if (map.get("email") != null) {
                        email = map.get("email").toString();
                        driverEmail.setText(email);
                    }
                    if (map.get("driver_licence") != null) {
                        driver_licence = map.get("driver_licence").toString();
                        driverLicence.setText(driver_licence);
                    }
                    if (map.get("vehical_model") != null) {
                        vehicle_model = map.get("vehical_model").toString();
                        vehicalModel.setText(vehicle_model);
                    }
                    if (map.get("vehical_reg_num") != null) {
                        vehicle_reg_no = map.get("vehical_reg_num").toString();
                        vehicalRegNo.setText(vehicle_reg_no);
                    }

                    if (map.get("ProfileImage") != null) {
                        profileImageUrl = map.get("ProfileImage").toString();
                        Glide.with(getApplication()).load(profileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    Uri resultUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_driver_settings;
    }


}
