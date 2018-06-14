package smartparadise.ridewithme.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import smartparadise.ridewithme.Fragments.SelectCharacterFragment;
import smartparadise.ridewithme.R;
import smartparadise.ridewithme.Services.OnAppKilled;

public class MainActivity extends BaseActivity {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment fragment;


    @Override
    protected void init() {


        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        sharedPreferences.getString("user","Not Found!");
        if(!sharedPreferences.getString("user","").equals("")){
            if(sharedPreferences.getBoolean("CHARACTER",true)){
                startActivity(new Intent(this, DriverMapActivity.class));
                finish();
            }else{
                startActivity(new Intent(this,CustomerMapActivity.class));
                finish();
            }

        }

        fragmentManager=getSupportFragmentManager();
        fragmentTransaction=fragmentManager.beginTransaction();
        fragment= new SelectCharacterFragment();
        fragmentTransaction.replace(R.id.select_char_frag_container,fragment,"MyFragments");
        fragmentTransaction.addToBackStack("SELE");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        int fragments=fragmentManager.getBackStackEntryCount();
        if(fragments==1){
            finish();
        }else{
            if(fragmentManager.getBackStackEntryCount()>1){
                fragmentManager.popBackStack();
            }else{
                super.onBackPressed();

            }
        }
         }

    @Override
    protected int getLayout()
    {
        return R.layout.activity_main;
    }

}
