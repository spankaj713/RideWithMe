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

/**
 * Created by HP on 15-05-2018.
 */

public class CustomerProfileActivity extends BaseActivity {


    @BindView(R.id.profileImage)
    CircleImageView profileImage;
    @BindView(R.id.takePic)
    FloatingActionButton takePic;

    @BindView(R.id.customerPhone)
    EditText customerPhone;
    @BindView(R.id.confirmButton)
    Button confirmButton;
    @BindView(R.id.customerFirstName)
    EditText customerFirstName;
    @BindView(R.id.customerLastName)
    EditText customerLastName;
    @BindView(R.id.customerEmail)
    EditText customerEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userId;
    private String first_name,last_name, phone,email, profileImageUrl;
    ProgressDialog dialog;
    Bitmap imageBitmap = null;

    @Override
    protected void init() {
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(userId);
        getRidersInfo();

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
                dialog=new ProgressDialog(CustomerProfileActivity.this);
                dialog.setTitle("RideWithMe");
                dialog.setMessage("Updating Profile..");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                saveRidersInfo();
            }


        });

    }

    private void getRidersInfo() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("first_name") != null) {
                        first_name = map.get("first_name").toString();
                        customerFirstName.setText(first_name);
                    }
                    if (map.get("last_name") != null) {
                        last_name = map.get("last_name").toString();
                        customerLastName.setText(last_name);
                    }
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        customerPhone.setText(phone);
                    }

                    if (map.get("email") != null) {
                        email = map.get("email").toString();
                        customerEmail.setText(email);
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

    private void saveRidersInfo() {

        first_name=customerFirstName.getText().toString();
        last_name=customerLastName.getText().toString();
        phone=customerPhone.getText().toString();
        email=customerEmail.getText().toString();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            dialog.dismiss();
            Toast.makeText(this, "Invalid Email!", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.PHONE.matcher(phone).matches()){
            dialog.dismiss();
            Toast.makeText(this, "Invalid Phone no.", Toast.LENGTH_SHORT).show();
        }
        else{
            Map userInfo = new HashMap();
            userInfo.put("first_name",first_name);
            userInfo.put("last_name",last_name);
            userInfo.put("phone",phone);
            userInfo.put("email",email);
            mRef.updateChildren(userInfo);

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
                        Toast.makeText(CustomerProfileActivity.this, "Information Saved Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CustomerProfileActivity.this, "Image couldn't be saved! Try Again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(CustomerProfileActivity.this, "Information Saved Successfully", Toast.LENGTH_SHORT).show();

                finish();

            }
        }

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
        return R.layout.activity_customer_setting;
    }

}

