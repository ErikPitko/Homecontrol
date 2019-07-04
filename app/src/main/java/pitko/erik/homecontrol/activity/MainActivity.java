package pitko.erik.homecontrol.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.danlew.android.joda.JodaTimeAndroid;
import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.AutomationFragment;
import pitko.erik.homecontrol.fragments.GraphFragment;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;

public class MainActivity extends AppCompatActivity {
    public static CompositeDisposable COMPOSITE_DISPOSABLE;
    private ObservableMqttClient mqttClient;
    private static Activity act;
    /**
     * Disables multiple simultaneous connections to mqtt server
     */
    private static final Semaphore connectionLock = new Semaphore(1);

    private BottomNavigationView navigation;

    private HomeFragment homeFragment;
    private RelayFragment relayFragment;
    private AutomationFragment automationFragment;
    private GraphFragment graphFragment;

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
                case R.id.navigation_automation:
                    setFragment(automationFragment);
                    return true;
                case R.id.navigation_graphs:
                    setFragment(graphFragment);
                    return true;
            }
            return false;
        }
    };

    public static Activity getAct() {
        return act;
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    public static void pushToast(String msg) {
        if (act != null)
            act.runOnUiThread(() -> Toast.makeText(act.getApplicationContext(), msg,
                    Toast.LENGTH_SHORT).show());
    }

    /***
     * Utility function, get string resource based on given name
     * @param name name of the resource
     * @return string resource in device language
     */
    public static String getResourcebyId(String name) {
        Resources res = act.getApplicationContext().getResources();
        return res.getString(res.getIdentifier(name, "string", act.getApplicationContext().getPackageName()));
    }

    /***
     * Connect to the MQTT broker, function uses global variable COMPOSITE_DISPOSABLE
     * in order to store RX callback function.
     */
    private void mqttConnect() {
        try {
            mqttClient = IMqtt.getInstance().getClient();
            if (mqttClient.isConnected()) {
                Log.i("MQTT", getString(R.string.stat_already_connected));
                mqttSubscribe();
                return;
            }
            if (!connectionLock.tryAcquire(3, TimeUnit.SECONDS)) {
                pushToast(getString(R.string.stat_err));
                return;
            }
            COMPOSITE_DISPOSABLE.add(
                    mqttClient.connect().subscribe(() -> {
                        connectionLock.release();
                        pushToast(getString(R.string.stat_conn));
                        mqttSubscribe();
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

    public void mqttSubscribe(){
        if (!mqttClient.isConnected())
            return;
        homeFragment.subscribeSensors();
        relayFragment.subscribeRelays();
        automationFragment.subscribeRelays();
    }

    private void mqttUnsubscribe(){
        if (!mqttClient.isConnected())
            return;
        homeFragment.unsubscribeSensors();
        relayFragment.unsubscribeRelays();
        automationFragment.unsubscribeRelays();
    }

    private void mqttDisconnect() {
        if (!mqttClient.isConnected())
            return;
        try {
            connectionLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mqttUnsubscribe();
        COMPOSITE_DISPOSABLE.add(mqttClient.disconnect().subscribe(connectionLock::release, e -> connectionLock.release()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;
        JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_main);

//        Object storing RX functions to prevent garbage collection
        COMPOSITE_DISPOSABLE = new CompositeDisposable();

//        Create fragments
        homeFragment = new HomeFragment();
        relayFragment = new RelayFragment();
        automationFragment = new AutomationFragment();
        graphFragment = new GraphFragment();
//        Set home fragment
        setFragment(homeFragment);

//        Create navigation menu
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
//        mqttDisconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mqttDisconnect();
        mqttClient.close();
        super.onDestroy();
    }
}
