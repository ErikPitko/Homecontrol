package pitko.erik.homecontrol.switches;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.google.gson.annotations.SerializedName;

import pitko.erik.homecontrol.fragments.FragmentSingleRelay;

import static android.widget.RelativeLayout.BELOW;

public class Relay implements OnCheckedChangeListener {
    @SerializedName("Relay")
    private String relayName;
    @SerializedName("State")
    private boolean state;

    private transient FragmentSingleRelay singleRelay;
    private transient static int relayCount = 1;

    public Relay(String relayName) {
        this.relayName = relayName;
    }

    private String getResourcebyId(Context context, String name) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
    }

    public void drawRelay(Fragment instance, RelativeLayout placeHolder) {
        Context context = instance.getContext();

        RelativeLayout fl = new RelativeLayout(context);
        RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        View lastView = placeHolder.getChildAt(placeHolder.getChildCount() - 1);
        if (lastView != null)
            flp.addRule(BELOW, lastView.getId());
        fl.setLayoutParams(flp);
        fl.setId(relayCount++);


//        Adding the RelativeLayout to the placeholder as a child
        placeHolder.addView(fl);

        singleRelay = new FragmentSingleRelay();
        singleRelay.setText(getResourcebyId(context, this.relayName));
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), singleRelay);
        transaction.commit();
        singleRelay.setSwitchChecked(state);
    }

    public FragmentSingleRelay getSingleRelay() {
        return singleRelay;
    }

    public Switch getaSwitch() {
        return singleRelay.getaSwitch();
    }


    public String getRelayName() {
        return relayName;
    }

    public void setRelayName(String relayName) {
        this.relayName = relayName;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        this.singleRelay.setSwitchChecked(state);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }
}
