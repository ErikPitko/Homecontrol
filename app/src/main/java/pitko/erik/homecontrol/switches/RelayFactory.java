package pitko.erik.homecontrol.switches;

import pitko.erik.homecontrol.activity.AutomationConfig;

public class RelayFactory {
    public Relay getRelay(String name, String topic, AutomationConfig.CFG purpose) {
        Relay relay = new RelayAutomation(name, topic, purpose);
        return relay;
    }

    public Relay getRelay(String name, String topic) {
        return new Relay(name, topic);
    }
}
