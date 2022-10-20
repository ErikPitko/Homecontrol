package pitko.erik.homecontrol.mqtt;

import android.content.res.Resources;
import android.util.Log;

import net.eusashead.iot.mqtt.ObservableMqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.RestTask;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.AutomationFragment;
import pitko.erik.homecontrol.fragments.GraphFragment;
import pitko.erik.homecontrol.fragments.HomeFragment;
import pitko.erik.homecontrol.fragments.RelayFragment;

public class MqttManager {
    private final MainActivity mainActivity;

    private ObservableMqttClient mqttClient;
    public static final String SERVER_HOST = "kosec-cloud.ddns.net";
    public static final String MOSQUITTO_BACKEND = "https://" + SERVER_HOST + "/api/v1/";
    public static final int MQTT_SSL_PORT = 8883;

    private HomeFragment homeFragment;
    private GraphFragment graphFragment;
    private AutomationFragment automationFragment;
    private RelayFragment relayFragment;
    private Resources res;

    public MqttManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        if (mainActivity != null) {
            homeFragment =  mainActivity.getHomeFragment();
            graphFragment = mainActivity.getGraphFragment();
            automationFragment = mainActivity.getAutomationFragment();
            relayFragment = mainActivity.getRelayFragment();
            res = mainActivity.getApplicationContext().getResources();
        }
    }

    private void registerNewDevice(RestTask restTask, String result) {
        try {
            int code = restTask.getConn().getResponseCode();
            switch (code) {
                case 202:
                case 409:
                    MainActivity.pushToast("Unauthorized");
                    break;
                case 400:
                    MainActivity.pushToast("Invalid ID");
                    break;
                case 500:
                default:
                    MainActivity.pushToast("Authorization server error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryToGetCredentials(RestTask restTask, String result) {
        try {
            int responseCode = restTask.getConn().getResponseCode();
            if (responseCode == 200 || responseCode == 404) {
                Mqtt mqtt = Mqtt.getInstance();
                JSONObject obj = new JSONObject(result);
                mqtt.setUserName(obj.getJSONObject("username").getString("String"));
                mqtt.setPassword(obj.getJSONObject("password").getString("String"));
                homeFragment.parseSensors(obj.getJSONArray("sensors"));
                graphFragment.parseSensors(homeFragment.getSensors());
                mqttConnect();
                //        Set home fragment
                mainActivity.setFragment(homeFragment);
            } else {
                MainActivity.pushToast("Unauthorized");
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
            json.put("androidID", MainActivity.getUniqueID());
            json.put("name", MainActivity.getDeviceName());
            task.setJsonOut(json);
            task.execute(MOSQUITTO_BACKEND + "putAndroidID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void authorizeAndConnectMqtt() {
        if (mqttClient != null && mqttClient.isConnected()) {
            Log.i("MQTT", res.getString(R.string.stat_already_connected));
            mqttSubscribe();
        } else {
            RestTask task = new RestTask(RestTask.METHOD.GET);
            task.setPostExecuteCallback(this::tryToGetCredentials);
            task.execute(MOSQUITTO_BACKEND + "getCredentials?androidID=" + MainActivity.getUniqueID());
        }
    }

    /***
     * Connect to the MQTT broker, function uses global variable COMPOSITE_DISPOSABLE
     * in order to store RX callback function.
     */
    private void mqttConnect() {
        try {
            Mqtt mqtt = Mqtt.getInstance();
            mqttClient = mqtt.buildClient();
            MainActivity.COMPOSITE_DISPOSABLE.add(
                    mqttClient.connect().subscribe(() -> {
                        MainActivity.pushToast(res.getString(R.string.stat_conn));
                        mqttSubscribe();
                    }, e -> {
                        if (e.getCause() != null) {
                            MainActivity.pushToast(e.getCause().getLocalizedMessage());
                        } else {
                            MainActivity.pushToast(res.getString(R.string.stat_err));
                        }
                    })
            );
        } catch (MqttException e) {
            MainActivity.pushToast(e.getMessage());
        }
    }

    public void mqttSubscribe() {
        if (!mqttClient.isConnected())
            return;
        homeFragment.subscribeSensors();
        relayFragment.subscribeRelays();
        automationFragment.subscribeRelays();
    }

    public void mqttUnsubscribe() {
        if (!mqttClient.isConnected())
            return;
        homeFragment.unsubscribeSensors();
        relayFragment.unsubscribeRelays();
        automationFragment.unsubscribeRelays();
    }

    public void mqttDisconnect() {
        if (!mqttClient.isConnected())
            return;
        mqttUnsubscribe();
        MainActivity.COMPOSITE_DISPOSABLE.add(mqttClient.disconnect().subscribe(mqttClient::close, e -> mqttClient.close()));
    }
}
