package pitko.erik.homecontrol.fragments.configs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.PublishMessage;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.models.AutomationField;

import static pitko.erik.homecontrol.activity.AutomationConfig.pushToast;

public class VentilationFragment extends Fragment implements View.OnClickListener {
    private ArrayList<AutomationField> ventilationFieldList = new ArrayList<>();

    public VentilationFragment() {
        // Required empty public constructor
    }

    public void subscribeFields() {
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            MainActivity.COMPOSITE_DISPOSABLE.add(
                mqttClient.subscribe("node/cellar/#", 1).subscribe(msg -> {
                    for (AutomationField af : ventilationFieldList) {
                        if (af.getTopic().equals(msg.getTopic())) {
                            getActivity().runOnUiThread(()->{
                                af.getEditText().setText(new String(msg.getPayload()));
                            });
                        }
                    }
                })
            );
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeFields() {
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            MainActivity.COMPOSITE_DISPOSABLE.add(
                    mqttClient.unsubscribe("node/cellar/#").subscribe()
            );
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeFields();
    }

    @Override
    public void onDestroy() {
        unsubscribeFields();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ventilation, container, false);
        ventilationFieldList.add(new AutomationField("node/cellar/fan_on_time", view.findViewById(R.id.fanontime)));
        ventilationFieldList.add(new AutomationField("node/cellar/fan_off_time", view.findViewById(R.id.fanofftime)));
        ventilationFieldList.add(new AutomationField("node/cellar/fan_dew_thresh", view.findViewById(R.id.fanthreshold)));
        ventilationFieldList.add(new AutomationField("node/cellar/fan_min_temp", view.findViewById(R.id.fanmintemp)));
        return view;
    }

    @Override
    public void onClick(View view) {
        ObservableMqttClient mqttClient = null;
        try {
            mqttClient = IMqtt.getInstance().getClient();
            if (!mqttClient.isConnected()) {
                pushToast("Client not connected");
                return;
            }
            for (AutomationField af : ventilationFieldList){
                mqttClient.publish(af.getTopic(), PublishMessage.create(af.getEditText().getText().toString().getBytes(), 1, true)).subscribe();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
