package pitko.erik.homecontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import pitko.erik.homecontrol.R;

public class FragmentSingleRelay extends Fragment {
    private String text;
    private SwitchCompat aSwitch;
    private boolean switchState;
    private TextView txtView;
    private CompoundButton.OnCheckedChangeListener listener;
    private TextView.OnClickListener textListener = null;

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public SwitchCompat getaSwitch() {
        return aSwitch;
    }

    public CompoundButton.OnCheckedChangeListener getListener() {
        return listener;
    }

    public void setSwitchListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public void setTextListener(TextView.OnClickListener listener) {
        this.textListener = listener;
    }

    public void setSwitchChecked(boolean state) {
        this.switchState = state;
        final FragmentActivity act = getActivity();
        if (aSwitch != null && act != null) {
            this.aSwitch.setOnCheckedChangeListener(null);
            act.runOnUiThread(() -> aSwitch.setChecked(state));
            this.aSwitch.setOnCheckedChangeListener(listener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlerelay,
                container, false);
        txtView = (TextView) view.findViewById(R.id.textView);
        aSwitch = (SwitchCompat) view.findViewById(R.id.switchR);
        aSwitch.setChecked(switchState);
        aSwitch.setOnCheckedChangeListener(listener);
        if (textListener != null)
            txtView.setOnClickListener(textListener);
        setText(text);
        return view;
    }
}
