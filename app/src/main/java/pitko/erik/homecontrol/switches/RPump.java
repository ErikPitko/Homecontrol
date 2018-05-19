package pitko.erik.homecontrol.switches;

import android.util.Log;
import android.widget.CompoundButton;

import com.google.gson.Gson;

import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.PublishMessage;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.IMqtt;

public class RPump extends Relay {

    public RPump(String relayName) {
        super(relayName);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            String msg;
            if (b) {
                super.setState(true);
                Log.d("Pump", "Enabled");
            } else {
                super.setState(false);
                Log.d("Pump", "Disabled");
            }
            List<Relay> list = new ArrayList<>();
            list.add(this);
            msg = new Gson().toJson(list);
            PublishMessage message = PublishMessage.create(msg.getBytes(), 1, false);
            mqttClient.publish("relay", message).subscribe();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
