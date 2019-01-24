package pitko.erik.homecontrol.switches;

public class RelayFactory {
    public enum RF {AUTOMATION}

    public Relay getRelay(String name, String topic, RF purpose) {
        Relay relay;
        switch (purpose) {
            case AUTOMATION:
                relay = new RelayAutomation(name, topic);
                break;
            default:
                relay = new Relay(name, topic);
        }

        return relay;
    }

    public Relay getRelay(String name, String topic) {
        return new Relay(name, topic);
    }
}
