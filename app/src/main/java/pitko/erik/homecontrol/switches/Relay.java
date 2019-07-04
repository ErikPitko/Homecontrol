package pitko.erik.homecontrol.switches;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.PublishMessage;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleRelay;

import static android.widget.RelativeLayout.BELOW;

public class Relay implements OnCheckedChangeListener {
    @SerializedName("Relay")
    private String relayName;
    @SerializedName("State")
    private boolean state;

    private transient String topic;
    private transient boolean notify_subs = false;
    private transient FragmentSingleRelay singleRelay;
    private transient static int relayCount = 1;
    private transient boolean retained = false;

    Relay(String relayName, String topic) {
        this.topic = topic;
        this.relayName = relayName;
    }

    /***
     * Creates new RelativeLayout and sets new FragmentSingleRelay into the newly created layout
     * @param instance parent fragment instance
     * @param placeHolder RelativeLayout where relay should be placed in.
     */
    public void draw(Fragment instance, RelativeLayout placeHolder) {
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
//        Must be defined in strings
        singleRelay.setText(MainActivity.getResourcebyId(this.relayName));
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), singleRelay);
        transaction.commit();
        singleRelay.setSwitchChecked(state);
    }

    public FragmentSingleRelay getSingleRelay() {
        return singleRelay;
    }

    public String getRelayName() {
        return relayName;
    }

    public boolean isNotify_subs() {
        return notify_subs;
    }

    public void unsetNotify_subs() {
        this.notify_subs = false;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        if (this.singleRelay != null)
            this.singleRelay.setSwitchChecked(state);
    }

    public void setRetained(boolean retained){
        this.retained = retained;
    }

    /***
     * Creates short message for the user
     * @param msg message to be shown
     */
    public void pushToast(String msg) {
        Activity act = getSingleRelay().getActivity();
        if (act == null)
            return;
        act.runOnUiThread(() -> Toast.makeText(act, msg,
                Toast.LENGTH_SHORT).show());
    }

    /***
     * Publish relay state to mqtt topic
     */
    private void publish() {
        String msg;
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            if (!mqttClient.isConnected()) {
                pushToast("Client not connected");
                return;
            }
            List<Relay> list = new ArrayList<>();
            list.add(this);
            msg = new Gson().toJson(list);
            PublishMessage message = PublishMessage.create(msg.getBytes(), 1, retained);
            mqttClient.publish(this.topic, message).subscribe();
            notify_subs = true;
            Log.d("Trigger", msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /***
     * Switch listener, sets new state to the switch and publishes it
     * @param compoundButton button event
     * @param b new state
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.isPressed()) {
            if (b) {
                this.setState(true);
            } else {
                this.setState(false);
            }
            this.publish();
        }
    }
}
