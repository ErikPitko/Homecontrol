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
                case R.id.navigation_settings:
                    setFragment(settingsFragment);
                    return true;
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
        Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_LONG).show();
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

        try {
            mqttClient = IMqtt.getInstance().getClient();
            mqttClient.connect().subscribe(() -> {
                homeFragment.setStatusMsg(getString(R.string.stat_conn));

                mqttClient.subscribe("brno", 1).subscribe(msg -> {
                    Log.d("MQTT", new String(msg.getPayload()));
                }, e -> {
                    Log.d("MQTT", e.getMessage());
                });

            }, e -> {
                pushToast("Connection failed");
                homeFragment.setStatusMsg(getString(R.string.stat_err));

            });


        } catch (MqttException e) {
            homeFragment.setStatusMsg(getString(R.string.stat_err));
            pushToast(e.getMessage());
        }

    }

    @Override
    protected void onDestroy() {
        mqttClient.close();
        super.onDestroy();
    }
}
