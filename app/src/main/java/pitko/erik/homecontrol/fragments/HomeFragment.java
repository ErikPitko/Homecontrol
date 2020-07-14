package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.sensors.Sensor;
import pitko.erik.homecontrol.sensors.TimeSensor;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private List<Sensor> sensors;

    public HomeFragment() {
        sensors = new ArrayList<>();
    }

    public void parseSensors(JSONArray sensors) throws JSONException {
        ArrayList<Sensor> sensorArrayList = new ArrayList<>();
        JSONObject sensor;
        for (int i = 0; i < sensors.length(); i++) {
            sensor = sensors.getJSONObject(i);
            switch (sensor.getInt("type")) {
                case 0:
                    sensorArrayList.add(
                            new Sensor(
                                    sensor.getString("topic"),
                                    sensor.getString("label"),
                                    sensor.getString("layout"),
                                    sensor.getString("postfix")));
                    break;
                case 1:
                    sensorArrayList.add(
                            new TimeSensor(
                                    sensor.getString("topic"),
                                    sensor.getString("label"),
                                    sensor.getString("layout"),
                                    sensor.getString("postfix")));
                    break;

            }
        }
        this.sensors = sensorArrayList;
    }

    public void subscribeSensors() {
        ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
        MainActivity.COMPOSITE_DISPOSABLE.add(
                mqttClient.subscribe("sensor/#", 0).subscribe(msg -> {
                    for (Sensor sensor : sensors) {
                        if (sensor.getTopic().equals(msg.getTopic())) {
                            sensor.setSensorStatus(new String(msg.getPayload()));
                        }
                    }
                })
        );
    }

    public void unsubscribeSensors() {
        ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
        MainActivity.COMPOSITE_DISPOSABLE.add(
                mqttClient.unsubscribe("sensor/#").subscribe()
        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Sensor.destroyPlaceHolderMap();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Add sensor to its respective layout
        for (Sensor sensor : sensors) {
            sensor.drawSensor(this, (LinearLayout) view.findViewById(R.id.sensorPlaceholder), getActivity());
        }
    }
}
