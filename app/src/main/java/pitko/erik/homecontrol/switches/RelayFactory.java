package pitko.erik.homecontrol.switches;

import android.app.Activity;

import pitko.erik.homecontrol.activity.AutomationConfig;

public class RelayFactory {
    public Relay getRelay(String name, String topic, AutomationConfig.CFG purpose) {
        return new RelayAutomation(name, topic, purpose);
    }

    public Relay getRelay(String name, String topic) {
        return new Relay(name, topic);
    }
}
