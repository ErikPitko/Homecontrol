package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.eusashead.iot.mqtt.ObservableMqttClient;

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

//        sensorText must be defined in strings.xml for language translation
//        sensors.add(new Sensor("sensor/podtatranskeho/temp", "temp", "home", "°C"));
        sensors.add(new TimeSensor("sensor/garden/time", "time", "garden", "min"));
        sensors.add(new Sensor("sensor/attic/temp", "temp", "attic", "°C"));
        sensors.add(new Sensor("sensor/raspberry/temperature", "temp", "garden", "°C"));
        sensors.add(new Sensor("sensor/raspberry/humidity", "hum", "garden", "%"));
        sensors.add(new Sensor("sensor/raspberry/dew_point", "dewPoint", "garden", "°C"));
        sensors.add(new TimeSensor("sensor/cellar/time", "time", "cellar", "min"));
        sensors.add(new Sensor("sensor/cellar/temperature", "temp", "cellar", "°C"));
        sensors.add(new Sensor("sensor/cellar/humidity", "hum", "cellar", "%"));
        sensors.add(new Sensor("sensor/cellar/dewpoint", "dewPoint", "cellar", "°C"));
        sensors.add(new Sensor("sensor/cellar/depth", "depth", "cellar", "cm"));

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Add sensor to its respective layout
        for (Sensor sensor : sensors) {
            switch (sensor.getLayout()) {
//                case "home":
//                    sensor.drawSensor(this, (RelativeLayout) view.findViewById(R.id.homeSensorLayout), getActivity());
//                    break;
                case "attic":
                    sensor.drawSensor(this, (RelativeLayout) view.findViewById(R.id.atticSensorLayout), getActivity());
                    break;
                case "garden":
                    sensor.drawSensor(this, (RelativeLayout) view.findViewById(R.id.gardenSensorLayout), getActivity());
                    break;
                case "cellar":
                    sensor.drawSensor(this, (RelativeLayout) view.findViewById(R.id.cellarSensorLayout), getActivity());
                    break;
            }
        }
    }
}
