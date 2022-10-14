package pitko.erik.homecontrol.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.switches.Relay;
import pitko.erik.homecontrol.switches.RelayFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class RelayFragment extends Fragment {
    private final List<Relay> relays = new ArrayList<>();
    private static final String RELAY_TOPIC = "node/cellar/relay";

    public RelayFragment() {
        RelayFactory rf = new RelayFactory();
        relays.add(rf.getRelay("Darling", RELAY_TOPIC));
        relays.add(rf.getRelay("EVd", RELAY_TOPIC));
        relays.add(rf.getRelay("Pump", RELAY_TOPIC));
        relays.add(rf.getRelay("Fan", RELAY_TOPIC));
        relays.add(rf.getRelay("Dryer", RELAY_TOPIC));
        relays.add(rf.getRelay("Boiler", RELAY_TOPIC));
    }

    public void subscribeRelays() {
        ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
        MainActivity.COMPOSITE_DISPOSABLE.add(
                mqttClient.subscribe("relay", 1).subscribe(msg -> {
                    List<Relay> relays;
                    JSONArray json = new JSONArray(new String(msg.getPayload()));
                    if (json.length() > 0) {
                        Type collectionType = new TypeToken<List<Relay>>() {
                        }.getType();
                        relays = new Gson().fromJson(json.toString(), collectionType);
                    } else {
                        return;
                    }
                    for (Relay inRelay : relays) {
                        for (Relay relay : this.relays) {
                            if (inRelay.getRelayName().compareTo(relay.getRelayName()) == 0) {
                                if (getActivity() != null && relay.isNotify_subs()) {
                                    relay.pushToast((inRelay.isState() ? getString(R.string.relay_enabled) : getString(R.string.relay_disabled)));
                                    relay.unsetNotify_subs();
                                }
                                relay.setState(inRelay.isState());
                            }

                        }
                    }
                })
        );
    }

    public void unsubscribeRelays() {
        ObservableMqttClient mqttClient = IMqtt.getInstance().getClient();
        mqttClient.unsubscribe("relay").subscribe();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relay,
                container, false);

        for (Relay relay : relays) {
            relay.draw(this, (RelativeLayout) view.findViewById(R.id.relayLayout));
            relay.getSingleRelay().setSwitchListener(relay);
        }
        return view;

    }

}
