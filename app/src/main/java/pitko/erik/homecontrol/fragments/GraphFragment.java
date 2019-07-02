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

public class GraphFragment extends Fragment {
    private List<Graph> graphs = new ArrayList<>();

    public GraphFragment() {
//        sensorText must be defined in strings.xml
        graphs.add(new Graph("sensor/attic/temp", "temp", "attic"));
        graphs.add(new Graph("sensor/raspberry/temperature", "temp", "garden"));
        graphs.add(new Graph("sensor/raspberry/humidity", "hum", "garden"));
        graphs.add(new Graph("sensor/raspberry/dew_point", "dewPoint", "garden"));
        graphs.add(new Graph("sensor/cellar/temperature", "temp", "cellar"));
        graphs.add(new Graph("sensor/cellar/humidity", "hum", "cellar"));
        graphs.add(new Graph("sensor/cellar/dewpoint", "dewPoint", "cellar"));
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
            }
        }
        return view;

    }
}
