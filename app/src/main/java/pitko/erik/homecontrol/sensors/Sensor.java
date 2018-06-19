package pitko.erik.homecontrol.sensors;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;

import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.SensorStatusFragment;

import static android.widget.RelativeLayout.BELOW;

public class Sensor {
    private final String topic;
    private final String sensorText;
    private String sensorStatus;
    private String postfix;
    private SensorStatusFragment sensorFragment;
    private static int sensorCount = 1;

    public Sensor(String topic, String sensorText) {
        this.topic = topic;
        this.sensorText = sensorText;
    }

    public Sensor(String topic, String sensorText, String postfix) {
        this(topic, sensorText);
        this.postfix = postfix;
    }

    public String getTopic() {
        return topic;
    }

    public void setSensorStatus(String msg) {
        this.sensorStatus = msg;
        if (sensorFragment != null){
            Activity act;
            if ((act = sensorFragment.getActivity()) != null)
                act.runOnUiThread(() -> sensorFragment.setStatus(msg));
        }
    }

    private String getResourcebyId(Context context, String name) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
    }

    public void subscribe(){
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            MainActivity.COMPOSITE_DISPOSABLE.add(
                    mqttClient.subscribe(topic, 0).subscribe(msg -> this.setSensorStatus(new String(msg.getPayload())))
            );
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

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


//        Adding the RelativeLayout to the placeholder as a child
        placeHolder.addView(fl);

        sensorFragment = new SensorStatusFragment();
//        Must be defined in strings
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), sensorFragment);
        transaction.commit();
        sensorFragment.setText(getResourcebyId(context, this.sensorText));
        sensorFragment.setPostfix(postfix);
        act.runOnUiThread(() -> sensorFragment.setStatus(sensorStatus));
    }

}
