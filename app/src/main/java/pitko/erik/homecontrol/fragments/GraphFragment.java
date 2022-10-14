package pitko.erik.homecontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.graphs.Graph;
import pitko.erik.homecontrol.sensors.Sensor;
import pitko.erik.homecontrol.sensors.TimeSensor;

public class GraphFragment extends Fragment {
    private List<Graph> graphs = new ArrayList<>();

    public GraphFragment() {
//        sensorText must be defined in strings.xml
    }

    public void parseSensors(List<Sensor> sensorList) {
        if(!this.graphs.isEmpty()){
            this.graphs.clear();
        }
        for (Sensor sensor : sensorList) {
//            Ignore time sensors
            if (sensor instanceof TimeSensor)
                continue;
            graphs.add(new Graph(sensor.getTopic(), sensor.getSensorText(), sensor.getLayout()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph,
                container, false);

        for (Graph graph : graphs) {
            switch (graph.getLayout()) {
                case "attic":
                    graph.draw(this, view.findViewById(R.id.graphAttic));
                    break;
                case "garden":
                    graph.draw(this, view.findViewById(R.id.graphGarden));
                    break;
                case "cellar":
                    graph.draw(this, view.findViewById(R.id.graphCellar));
                    break;
                case "tahanovce":
                    graph.draw(this, view.findViewById(R.id.graphTahanovce));
                    break;
                default:
                    graph.draw(this, view.findViewById(R.id.graphUnknown));
                    view.findViewById(R.id.graphUnknownTextView).setVisibility(View.VISIBLE);
            }
        }
        return view;

    }
}
