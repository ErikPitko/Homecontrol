package pitko.erik.homecontrol.sensors;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.SensorStatusFragment;

import static android.widget.RelativeLayout.BELOW;

public class Sensor {
    private final String topic;
    private final String sensorText;
    private String sensorStatus;
    protected String postfix;
    private SensorStatusFragment sensorFragment;
    private static int sensorCount = 1;
    private String layout;

    public Sensor(String topic, String sensorText, String layout) {
        this.topic = topic;
        this.sensorText = sensorText;
        this.layout = layout;
    }

    public Sensor(String topic, String sensorText, String layout, String postfix) {
        this(topic, sensorText, layout);
        this.postfix = postfix;
    }

    public String getLayout() {
        return layout;
    }

    public String getTopic() {
        return topic;
    }

    public void setSensorStatus(String msg) {
        this.sensorStatus = msg;
        if (sensorFragment != null) {
            Activity act;
            if ((act = sensorFragment.getActivity()) != null)
                act.runOnUiThread(() -> sensorFragment.setStatus(msg));
        }
    }

    public void setPostfixbyResource(String postfix) {
        this.postfix = MainActivity.getResourcebyId(postfix);
        sensorFragment.setPostfix(this.postfix);
    }

    /***
     * Create new relative layout under the set placeholder at the last position
     * @param instance parent fragment instance
     * @param placeHolder RelativeLayout where sensor should be placed in.
     * @param act UI thread activity
     */
    public void drawSensor(Fragment instance, RelativeLayout placeHolder, Activity act) {
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
        fl.setId(sensorCount++);

//        Adding the RelativeLayout to the placeholder as a last child
        placeHolder.addView(fl);

        sensorFragment = new SensorStatusFragment();
//        Must be defined in strings
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), sensorFragment);
        transaction.commit();
        sensorFragment.setText(MainActivity.getResourcebyId(this.sensorText));
        sensorFragment.setPostfix(postfix);
        act.runOnUiThread(() -> sensorFragment.setStatus(sensorStatus));
    }

}
