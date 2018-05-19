package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.switches.Relay;
import pitko.erik.homecontrol.switches.RelayFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class RelayFragment extends Fragment {
    private List<Relay> relays = new ArrayList<>();

    public RelayFragment() {
        RelayFactory rf = new RelayFactory();
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
        relays.add(rf.getRelay("Pump"));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relay,
                container, false);

        for (Relay relay : relays) {
            relay.drawRelay(this, (RelativeLayout) view.findViewById(R.id.relayLayout));
            relay.getSingleRelay().setSwitchListener(relay);
        }
        return view;

    }

}
