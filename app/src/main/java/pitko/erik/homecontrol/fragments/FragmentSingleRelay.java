package pitko.erik.homecontrol.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import pitko.erik.homecontrol.R;

public class FragmentSingleRelay extends Fragment {
    private String text;
    private Switch aSwitch;
    private boolean switchState;
    private TextView txtView;
    private CompoundButton.OnCheckedChangeListener listener;

    public void setText(String text) {
        this.text = text;
        if (txtView != null)
            txtView.setText(text);
    }

    public Switch getaSwitch() {
        return aSwitch;
    }

    public CompoundButton.OnCheckedChangeListener getListener() {
        return listener;
    }

    public void setSwitchListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public void setSwitchChecked(boolean state) {
        this.switchState = state;
        if (aSwitch != null) {
            this.aSwitch.setOnCheckedChangeListener(null);
            getActivity().runOnUiThread(() -> aSwitch.setChecked(state));
            this.aSwitch.setOnCheckedChangeListener(listener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singlerelay,
                container, false);
        txtView = (TextView) view.findViewById(R.id.textView);
        aSwitch = (Switch) view.findViewById(R.id.switchR);
        aSwitch.setChecked(switchState);
        aSwitch.setOnCheckedChangeListener(listener);
        setText(text);
        return view;
    }
}
