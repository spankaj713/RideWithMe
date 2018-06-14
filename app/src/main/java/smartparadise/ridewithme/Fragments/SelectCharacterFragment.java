package smartparadise.ridewithme.Fragments;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import smartparadise.ridewithme.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectCharacterFragment extends BaseFragment {


    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment fragment;
    boolean isDriver = false;
    @BindView(R.id.myCar)
    ImageView myCar;
    @BindView(R.id.riderCard)
    CardView riderCard;
    @BindView(R.id.driverCard)
    CardView driverCard;



    @Override
    protected int getLayoutId() {
        return R.layout.fragment_select_character;
    }

    @Override
    protected void init() {
        ObjectAnimator animatorX=ObjectAnimator.ofFloat(myCar,"x",100f);
        animatorX.setDuration(1000);
        animatorX.start();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        final Bundle bundle=new Bundle();
        fragment = new DriverRegPhoneFrag();
        riderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDriver = false;
                bundle.putBoolean("CHARACTER", isDriver);
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.select_char_frag_container, fragment, "MyFragments");
                fragmentTransaction.addToBackStack("MyFragments");
                fragmentTransaction.commit();

            }
        });

        driverCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDriver = true;
                bundle.putBoolean("CHARACTER", isDriver);

                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.select_char_frag_container, fragment, "MyFragments");
                fragmentTransaction.addToBackStack("MyFragments");
                fragmentTransaction.commit();
            }
        });

    }

}
