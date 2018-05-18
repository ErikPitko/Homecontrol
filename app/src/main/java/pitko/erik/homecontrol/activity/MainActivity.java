package pitko.erik.homecontrol.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;

import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;
import pitko.erik.homecontrol.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private ObservableMqttClient mqttClient;

    private BottomNavigationView navigation;

    private HomeFragment homeFragment;
    private RelayFragment relayFragment;
    private SettingsFragment settingsFragment;
    private String[] subscribeTopics = {"brno"};
    private int[] subscribeQos = {0};

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setFragment(homeFragment);
                    return true;
                case R.id.navigation_relays:
                    setFragment(relayFragment);
                    return true;
//                case R.id.navigation_settings:
//                    setFragment(settingsFragment);
//                    return true;
            }
            return false;
        }
    };

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    private void pushToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg,
                    Toast.LENGTH_LONG).show();
        });
    }

    private void updateStatusMsg(HomeFragment frag, String msg) {
        runOnUiThread(() -> {
            frag.setStatusMsg(msg);
        });
    }

    private synchronized void mqttConnect() {
        try {
            mqttClient = IMqtt.getInstance().getClient();
            if (mqttClient.isConnected()) {
                Log.d("MQTT", "Already connected");
                return;
            }
            mqttClient.connect().subscribe(() -> {
                this.updateStatusMsg(homeFragment, getString(R.string.stat_conn));

                mqttClient.subscribe(subscribeTopics, subscribeQos).subscribe(msg -> {
                    Log.d("MQTT", new String(msg.getPayload()));
                });
                pushToast(getString(R.string.stat_conn));
            }, e -> {
                pushToast(getString(R.string.stat_err));
                this.updateStatusMsg(homeFragment, getString(R.string.stat_err));
            });

        } catch (MqttException e) {
            this.updateStatusMsg(homeFragment, getString(R.string.stat_err));
            pushToast(e.getMessage());
        }
    }

    private synchronized void mqttDisconnect() {
        if (!mqttClient.isConnected())
            return;
        mqttClient.unsubscribe(subscribeTopics).subscribe(() -> {
            Log.d("MQTT", "Unsubscribe successful");
        }, e -> {
            Log.d("MQTT", "Unsubscribe failed");
        });
        mqttClient.disconnect().subscribe(() -> {
            Log.d("MQTT", "Disconnect successful");
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        relayFragment = new RelayFragment();
        settingsFragment = new SettingsFragment();
        setFragment(homeFragment);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mqttConnect();
    }

    @Override
    protected void onPause() {
        mqttDisconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mqttDisconnect();
        mqttClient.close();
        super.onDestroy();
    }
}
