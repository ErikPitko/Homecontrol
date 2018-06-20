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

import java.util.concurrent.Semaphore;

import io.reactivex.disposables.CompositeDisposable;
import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;

public class MainActivity extends AppCompatActivity {
    public static CompositeDisposable COMPOSITE_DISPOSABLE;
    private ObservableMqttClient mqttClient;
    //    private ReentrantLock connectionLock = new ReentrantLock();
    private static final Semaphore connectionLock = new Semaphore(1);

    private BottomNavigationView navigation;

    private HomeFragment homeFragment;
    private RelayFragment relayFragment;
//    private SettingsFragment settingsFragment;

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
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_SHORT).show());
    }

    private void mqttConnect() {
        try {
            connectionLock.acquire();
            mqttClient = IMqtt.getInstance().getClient();
            if (mqttClient.isConnected()) {
                Log.i("MQTT", "Already connected");
                return;
            }
            COMPOSITE_DISPOSABLE.add(
                    mqttClient.connect().subscribe(() -> {
                        connectionLock.release();
                        pushToast(getString(R.string.stat_conn));
                        homeFragment.subscribeSensors();
                        relayFragment.subscribeRelays();
                    }, e -> {
                        connectionLock.release();
                        if (e.getCause() != null) {
                            pushToast(e.getCause().getLocalizedMessage());
                        } else {
                            pushToast(getString(R.string.stat_err));
                        }
                    })
            );
        } catch (MqttException e) {
            pushToast(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void mqttDisconnect() {
        if (!mqttClient.isConnected())
            return;
        try {
            connectionLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        homeFragment.unsubscribeSensors();
        relayFragment.unsubscribeRelays();
        COMPOSITE_DISPOSABLE.add(mqttClient.disconnect().subscribe(connectionLock::release, e -> connectionLock.release()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        COMPOSITE_DISPOSABLE = new CompositeDisposable();
        homeFragment = new HomeFragment();
        relayFragment = new RelayFragment();
//        settingsFragment = new SettingsFragment();
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
        COMPOSITE_DISPOSABLE.dispose();
        super.onDestroy();
    }
}
