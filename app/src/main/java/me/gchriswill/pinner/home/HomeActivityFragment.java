package me.gchriswill.pinner.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import me.gchriswill.pinner.User;


/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends ListFragment {

    public static final String TAG = "HomeActivityFragment";
    HomeActivityFragmentInterface homeActivityFragmentInterface;

    public HomeActivityFragment(){}

    public interface HomeActivityFragmentInterface {
        void onItemClicked(User user);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof HomeActivityFragmentInterface)
            homeActivityFragmentInterface = (HomeActivityFragmentInterface) context;
        else throw new IllegalStateException("HomeActivityFragmentInterface not implemented");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No data yet...");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        User user = (User) v.getTag();
        homeActivityFragmentInterface.onItemClicked(user);
    }
}
