package pitko.erik.homecontrol;

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

public class IMqtt {
    private static IMqtt instance = null;
    private ObservableMqttClient client;

    private IMqtt() throws MqttException {
        MemoryPersistence dataStore = new MemoryPersistence();
        final IMqttAsyncClient paho = new MqttAsyncClient("ssl://kosec.ddns.net:8883", MqttAsyncClient.generateClientId(), dataStore);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setUserName("kosec");
        connectOptions.setPassword("rangerkondor31".toCharArray());

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
