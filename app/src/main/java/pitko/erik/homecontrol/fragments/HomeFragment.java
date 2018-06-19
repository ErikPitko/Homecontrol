package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.sensors.Sensor;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private List<Sensor> sensors;

    public HomeFragment() {
        sensors = new ArrayList<>();

//        sensorText must be defined in strings.xml
        sensors.add(new Sensor("sensor/raspberry/temperature", "rasp_temp", "C"));
        sensors.add(new Sensor("sensor/raspberry/humidity", "rasp_hum", "%"));
        sensors.add(new Sensor("sensor/raspberry/dew_point", "rasp_dew_point", "C"));
        sensors.add(new Sensor("sensor/raspberry/vapor_pressure", "rasp_vapor_pressure"));
        sensors.add(new Sensor("sensor/depth", "depth", "cm"));

    }

    public void subscribeSensors(){
        for (Sensor sensor:sensors){
            sensor.subscribe();
        }
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

        for (Sensor sensor:sensors){
            sensor.drawSensor(this, (RelativeLayout) view.findViewById(R.id.sensorLayout), getActivity());
        }
    }
}
