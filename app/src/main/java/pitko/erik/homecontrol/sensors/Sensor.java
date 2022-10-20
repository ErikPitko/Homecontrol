package pitko.erik.homecontrol.sensors;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;

import pitko.erik.homecontrol.R;
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
    private final String layout;
    private static final HashMap<String, RelativeLayout> sensorPlaceHolderMap = new HashMap<>();

    public Sensor(String topic, String sensorText, String layout) {
        this.topic = topic;
        this.sensorText = sensorText;
        this.layout = layout;
    }

    public Sensor(String topic, String sensorText, String layout, String postfix) {
        this(topic, sensorText, layout);
        this.postfix = postfix;
    }

    public String getSensorText() {
        return sensorText;
    }

    public static void destroyPlaceHolderMap() {
        sensorPlaceHolderMap.clear();
    }

    public String getLayout() {
        return layout;
    }

    public String getTopic() {
        return topic;
    }

    public void setSensorStatus(String msg, Integer color) {
        this.sensorStatus = msg;
        if (sensorFragment != null) {
            Activity act;
            if ((act = sensorFragment.getActivity()) != null)
                act.runOnUiThread(() -> sensorFragment.setStatus(msg, color));
        }
    }

    public void setPostfixbyResource(String postfix) {
        this.postfix = MainActivity.getResourcebyId(postfix);
        sensorFragment.setPostfix(this.postfix);
    }

    private RelativeLayout createSensorPlaceholder(LinearLayout linearLayout, Fragment instance) {
        Resources res = instance.getActivity().getResources();
        RelativeLayout placeHolderLayout;
        String layoutLabel = MainActivity.getResourcebyId(this.layout);
        if (layoutLabel == null) {
            layoutLabel = this.layout;
        }
        TextView textView = new TextView(instance.getContext());
        textView.setText(layoutLabel);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                30,
                res.getDisplayMetrics()
        );
        if (!sensorPlaceHolderMap.isEmpty())
            params.setMargins(0, px, 0, 0);
        textView.setLayoutParams(params);

        textView.setBackgroundColor(res.getColor(R.color.colorPrimaryDark, null));
        textView.setTextColor(res.getColor(R.color.holo_blue_bright, null));
        textView.setGravity(Gravity.BOTTOM);
        float scale = res.getDisplayMetrics().density;
        int padd = (int) (8 * scale + 0.5f);
        textView.setPadding(padd, padd, padd, padd);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        placeHolderLayout = new RelativeLayout(instance.getContext());
        placeHolderLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        linearLayout.addView(textView);
        linearLayout.addView(placeHolderLayout);

        return placeHolderLayout;
    }

    /***
     * Create new relative layout under the set placeholder at the last position
     * @param instance parent fragment instance
     * @param placeHolder RelativeLayout where sensor should be placed in.
     * @param act UI thread activity
     */
    public void drawSensor(Fragment instance, LinearLayout placeHolder, Activity act) {
        Context context = instance.getContext();

        RelativeLayout placeHolderLayout = sensorPlaceHolderMap.get(this.layout);
        if (placeHolderLayout == null) {
            placeHolderLayout = createSensorPlaceholder(placeHolder, instance);
            sensorPlaceHolderMap.put(this.layout, placeHolderLayout);
        }

        RelativeLayout fl = new RelativeLayout(context);
        RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        View lastView = placeHolderLayout.getChildAt(placeHolderLayout.getChildCount() - 1);
        if (lastView != null)
            flp.addRule(BELOW, lastView.getId());
        fl.setLayoutParams(flp);
        fl.setId(sensorCount++);

//        Adding the RelativeLayout to the placeholder as a last child
        placeHolderLayout.addView(fl);

        sensorFragment = new SensorStatusFragment();
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), sensorFragment);
        transaction.commit();
//        Must be defined in strings
        String sensorLabel = MainActivity.getResourcebyId(this.sensorText);
        if (sensorLabel == null) {
            sensorLabel = this.sensorText;
        }
        sensorFragment.setText(sensorLabel);
        sensorFragment.setPostfix(postfix);
        act.runOnUiThread(() -> sensorFragment.setStatus(sensorStatus, null));
    }

}
