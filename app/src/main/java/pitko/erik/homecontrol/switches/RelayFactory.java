package pitko.erik.homecontrol.switches;

public class RelayFactory {
    public Relay getRelay(String name) {
        Relay relay;

        switch (name) {
//            case "Pump":
//                relay = new RPump(name);
//                break;
            default:
                relay = new Relay(name);
        }

        return relay;
    }
}
