package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pitko.erik.homecontrol.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private String statText;

    public HomeFragment() {
        // Required empty public constructor
    }

    public void setStatusMsg(String msg) {
        statText = msg;
        TextView txtView = (TextView) getActivity().findViewById(R.id.connStatus);
        txtView.setText(statText);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView txtView = (TextView) getActivity().findViewById(R.id.connStatus);
        txtView.setText(statText);
    }
}
