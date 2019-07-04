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

public class DryoutFragment extends Fragment implements View.OnClickListener {
    private ArrayList<AutomationField> dryoutFieldList = new ArrayList<>();

    public DryoutFragment() {
        // Required empty public constructor
    }

    public void subscribeFields() {
        try {
            ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
            MainActivity.COMPOSITE_DISPOSABLE.add(
                mqttClient.subscribe("node/cellar/#", 1).subscribe(msg -> {
                    for (AutomationField df : dryoutFieldList) {
                        if (df.getTopic().equals(msg.getTopic())) {
                            getActivity().runOnUiThread(()->{
                                df.getEditText().setText(new String(msg.getPayload()));
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
        View view = inflater.inflate(R.layout.fragment_dryout, container, false);
        dryoutFieldList.add(new AutomationField("node/cellar/dryer_on_time", view.findViewById(R.id.dryerontime)));
        dryoutFieldList.add(new AutomationField("node/cellar/dryer_threshold", view.findViewById(R.id.dryerthresh)));
        subscribeFields();
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
            for (AutomationField af : dryoutFieldList){
                mqttClient.publish(af.getTopic(), PublishMessage.create(af.getEditText().getText().toString().getBytes(), 1, true)).subscribe();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
