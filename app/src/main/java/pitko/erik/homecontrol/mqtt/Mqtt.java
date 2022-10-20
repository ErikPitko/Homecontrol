package pitko.erik.homecontrol.mqtt;

import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.paho.PahoObservableMqttClient;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by kosec on 15.2.18.
 */

public class Mqtt {
    private static Mqtt instance = null;
    private ObservableMqttClient client;
    private String userName;
    private String password;
    private static final String SERVER_URI = "ssl://" + MqttManager.SERVER_HOST + ":" + MqttManager.MQTT_SSL_PORT;

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

    private Mqtt() {
    }

    public ObservableMqttClient getClient() {
        return client;
    }

    public static Mqtt getInstance() {
        if (instance == null)
            instance = new Mqtt();
        return instance;
    }

}
