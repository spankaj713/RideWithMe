package smartparadise.ridewithme.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by HP on 08-04-2018.
 */

public abstract class BaseFragment extends Fragment {

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=null;
        if(getLayoutId()!=0){
            view=inflater.inflate(getLayoutId(),container,false);
            unbinder=ButterKnife.bind(this,view);
            init();
        }
        return view;
    }


    protected abstract int getLayoutId();



    protected abstract void init();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder!=null){
            unbinder.unbind();
        }
    }

}
