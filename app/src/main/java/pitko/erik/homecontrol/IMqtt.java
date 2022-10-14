package pitko.erik.homecontrol;

import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.paho.PahoObservableMqttClient;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import pitko.erik.homecontrol.activity.MainActivity;

/**
 * Created by kosec on 15.2.18.
 */

public class IMqtt {
    private static IMqtt instance = null;
    private ObservableMqttClient client;
    private String userName;
    private String password;
    private static final String SERVER_URI = "ssl://" + MainActivity.SERVER_HOST + ":" + MainActivity.MQTT_SSL_PORT;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ObservableMqttClient buildClient() throws MqttException {
        MemoryPersistence dataStore = new MemoryPersistence();
        final IMqttAsyncClient paho = new MqttAsyncClient(SERVER_URI, MqttAsyncClient.generateClientId(), dataStore);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(userName);
        connectOptions.setPassword(password.toCharArray());
        connectOptions.setConnectionTimeout(3);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        connectOptions.setMaxInflight(40);

        client = PahoObservableMqttClient.builder(paho)
                .setConnectOptions(connectOptions)
                .build();
        return client;
    }

    private IMqtt() {
    }

    public ObservableMqttClient getClient() {
        return client;
    }

    public static IMqtt getInstance() {
        if (instance == null)
            instance = new IMqtt();
        return instance;
    }

}
