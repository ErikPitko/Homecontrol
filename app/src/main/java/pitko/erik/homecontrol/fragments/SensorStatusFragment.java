package pitko.erik.homecontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import pitko.erik.homecontrol.R;

public class SensorStatusFragment extends Fragment {
    private String text;
    private TextView txtView;
    private String status;
    private String postfix;
    private TextView statusTxtView;

    public void setPostfix(String msg) {
        this.postfix = msg;
    }

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public void setStatus(String text) {
        this.status = text;
        if (statusTxtView != null) {
            if (postfix != null)
                statusTxtView.setText(text + " " + postfix);
            else
                statusTxtView.setText(text);

        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_status,
                container, false);
        txtView = (TextView) view.findViewById(R.id.textView);
        statusTxtView = (TextView) view.findViewById(R.id.statusS);
        setText(text);
        setStatus(status);
        return view;
    }
}
