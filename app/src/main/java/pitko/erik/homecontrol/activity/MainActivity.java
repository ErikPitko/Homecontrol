package pitko.erik.homecontrol.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.JodaTimeAndroid;
import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import pitko.erik.homecontrol.IMqtt;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.RestTask;
import pitko.erik.homecontrol.fragments.AutomationFragment;
import pitko.erik.homecontrol.fragments.GraphFragment;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;
import pitko.erik.homecontrol.models.SensorShared;

public class MainActivity extends AppCompatActivity {
    private static String uniqueID = null;
    private static String deviceName;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static final String PREF_SENSOR_SHARED = "PREF_SENSOR_SHARED";
    public static final String SERVER_HOST = "kosec-cloud.ddns.net";
    public static final String MOSQUITTO_BACKEND = "https://" + SERVER_HOST + "/api/v1/";
    public static final int MQTT_SSL_PORT = 8883;

    public static CompositeDisposable COMPOSITE_DISPOSABLE;
    private ObservableMqttClient mqttClient;
    private static Activity act;
    public static HashMap<String, SensorShared> sensorPrefs;
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
        int id = res.getIdentifier(name, "string", act.getApplicationContext().getPackageName());
        if (id > 0) {
            return res.getString(id);
        } else {
            return null;
        }
    }

    public static int getResourceId(String name, String defType) {
        Resources res = act.getApplicationContext().getResources();
        return res.getIdentifier(name, defType, act.getApplicationContext().getPackageName());
    }

    private void registerNewDevice(RestTask restTask, String result) {
        try {
            int code = restTask.getConn().getResponseCode();
            switch (code) {
                case 202:
                case 409:
                    pushToast("Unauthorized");
                    break;
                case 400:
                    pushToast("Invalid ID");
                    break;
                case 500:
                default:
                    pushToast("Authorization server error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void tryToGetCredentials(RestTask restTask, String result) {
        try {
            int responseCode = restTask.getConn().getResponseCode();
            if (responseCode == 200 || responseCode == 404) {
                IMqtt mqtt = IMqtt.getInstance();
                JSONObject obj = new JSONObject(result);
                mqtt.setUserName(obj.getJSONObject("username").getString("String"));
                mqtt.setPassword(obj.getJSONObject("password").getString("String"));
                homeFragment.parseSensors(obj.getJSONArray("sensors"));
                graphFragment.parseSensors(homeFragment.getSensors());
                mqttConnect();
                //        Set home fragment
                setFragment(homeFragment);
            } else {
                pushToast("Unauthorized");
            }
            return;
        } catch (JSONException | IOException e) {
            Log.w("REST", "Could not get credentials to MQTT broker");
            e.printStackTrace();
        }

        try {
            RestTask task = new RestTask(RestTask.METHOD.PUT);
            task.setPostExecuteCallback(this::registerNewDevice);
            JSONObject json = new JSONObject();
            json.put("androidID", uniqueID);
            json.put("name", deviceName);
            task.setJsonOut(json);
            task.execute(MOSQUITTO_BACKEND + "putAndroidID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void authorizeAndConnectMqtt() {
        if (mqttClient != null && mqttClient.isConnected()) {
            Log.i("MQTT", getString(R.string.stat_already_connected));
            mqttSubscribe();
        } else {
            RestTask task = new RestTask(RestTask.METHOD.GET);
            task.setPostExecuteCallback(this::tryToGetCredentials);
            task.execute(MOSQUITTO_BACKEND + "getCredentials?androidID=" + uniqueID);
        }
    }

    /***
     * Connect to the MQTT broker, function uses global variable COMPOSITE_DISPOSABLE
     * in order to store RX callback function.
     */
    private void mqttConnect() {
        try {
            IMqtt mqtt = IMqtt.getInstance();
            mqttClient = mqtt.buildClient();
            ;
            if (!connectionLock.tryAcquire(3, TimeUnit.SECONDS)) {
                pushToast("Timeout");
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

    public void mqttSubscribe() {
        if (!mqttClient.isConnected())
            return;
        homeFragment.subscribeSensors();
        relayFragment.subscribeRelays();
        automationFragment.subscribeRelays();
    }

    private void mqttUnsubscribe() {
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

    public synchronized static void setSensorPrefs(Context context, HashMap<String, SensorShared> sensorPrefs) {
        Gson gson = new Gson();
        String sensorPrefsStr;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_SENSOR_SHARED, Context.MODE_PRIVATE);

        sensorPrefsStr = gson.toJson(sensorPrefs);
        sharedPrefs.edit().putString(PREF_SENSOR_SHARED, sensorPrefsStr).apply();
    }


    public synchronized static HashMap<String, SensorShared> getSensorPrefs(Context context) {
        Gson gson = new Gson();
        String sensorPrefsStr;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_SENSOR_SHARED, Context.MODE_PRIVATE);
        sensorPrefsStr = sharedPrefs.getString(PREF_SENSOR_SHARED, null);
        if (sensorPrefsStr == null)
            return new HashMap<String, SensorShared>();

        java.lang.reflect.Type type = new TypeToken<HashMap<String, SensorShared>>(){}.getType();
        return gson.fromJson(sensorPrefsStr, type);
    }

    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueID = id(this);
        sensorPrefs = getSensorPrefs(this);
        deviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        if (deviceName == null) {
            deviceName = "Undefined";
        }
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

//        Create navigation menu
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("INSTANCE_ID", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("INSTANCE_ID", msg);
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        authorizeAndConnectMqtt();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mqttDisconnect();
    }

    @Override
    protected void onDestroy() {
        setSensorPrefs(this, sensorPrefs);
        if (mqttClient != null) {
            mqttDisconnect();
            mqttClient.close();
        }
        super.onDestroy();
    }
}
