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
    //    private final String serverURI = "ssl://kosec-cloud.ddns.net:8883";
    private final String serverURI = "ssl://" + MainActivity.SERVER_HOST + ":" + MainActivity.MQTT_SSL_PORT;

    private IMqtt() throws MqttException {
//        RxJavaPlugins.setErrorHandler(e -> Log.e("RXJava", e.getMessage()));
        MemoryPersistence dataStore = new MemoryPersistence();
        final IMqttAsyncClient paho = new MqttAsyncClient(serverURI, MqttAsyncClient.generateClientId(), dataStore);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName("kosec");
        connectOptions.setPassword("rangerkondor31".toCharArray());
        connectOptions.setConnectionTimeout(3);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setKeepAliveInterval(30);
        connectOptions.setMaxInflight(40);

        client = PahoObservableMqttClient.builder(paho)
                .setConnectOptions(connectOptions)
                .build();
    }

    public ObservableMqttClient getClient() {
        return client;
    }

    public static IMqtt getInstance() throws MqttException {
        if (instance == null)
            instance = new IMqtt();
        return instance;
    }

}
