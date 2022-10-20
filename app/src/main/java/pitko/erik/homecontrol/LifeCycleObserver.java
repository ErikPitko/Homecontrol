package pitko.erik.homecontrol;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import lombok.RequiredArgsConstructor;
import pitko.erik.homecontrol.mqtt.MqttManager;

@RequiredArgsConstructor
public class LifeCycleObserver implements DefaultLifecycleObserver {
    private final MqttManager mqttManager;

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        mqttManager.authorizeAndConnectMqtt();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        mqttManager.mqttDisconnect();
    }
}
