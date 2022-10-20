package pitko.erik.homecontrol.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.HashMap;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import lombok.Getter;
import pitko.erik.homecontrol.LifeCycleObserver;
import pitko.erik.homecontrol.mqtt.MqttManager;
import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.AutomationFragment;
import pitko.erik.homecontrol.fragments.GraphFragment;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;
import pitko.erik.homecontrol.models.SensorShared;

public class MainActivity extends AppCompatActivity {
    @Getter
    private static String uniqueID = null;
    @Getter
    private static String deviceName;

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static final String PREF_SENSOR_SHARED = "PREF_SENSOR_SHARED";

    public static CompositeDisposable COMPOSITE_DISPOSABLE;
    private static Activity act;
    public static HashMap<String, SensorShared> sensorPrefs;

    private BottomNavigationView navigation;

    @Getter
    private HomeFragment homeFragment;
    @Getter
    private RelayFragment relayFragment;
    @Getter
    private GraphFragment graphFragment;
    @Getter
    private AutomationFragment automationFragment;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

    public static void pushToast(String msg) {
        if (act != null)
            act.runOnUiThread(() -> Toast.makeText(act.getApplicationContext(), msg,
                    Toast.LENGTH_SHORT).show());
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
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

    private synchronized static String id(Context context) {
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
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        MqttManager mqttManager = new MqttManager(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifeCycleObserver(mqttManager));
    }

    @Override
    protected void onDestroy() {
        setSensorPrefs(this, sensorPrefs);
        super.onDestroy();
    }
}
