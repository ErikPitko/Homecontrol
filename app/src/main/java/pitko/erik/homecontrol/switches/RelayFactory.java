package pitko.erik.homecontrol.switches;

import android.app.Activity;

import pitko.erik.homecontrol.activity.AutomationConfig;

public class RelayFactory {
    public Relay getRelay(String name, String topic, AutomationConfig.CFG purpose, Activity activity) {
        Relay relay = new RelayAutomation(name, topic, purpose, activity);
        return relay;
    }

    public Relay getRelay(String name, String topic) {
        return new Relay(name, topic);
    }
}
