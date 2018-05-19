package pitko.erik.homecontrol.switches;

import android.util.Log;
import android.widget.CompoundButton;

public class RPump extends Relay {

    public RPump(String relayName) {
        super(relayName);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            super.setState(true);
            Log.d("Pump", "Enabled");
        } else {
            super.setState(false);
            Log.d("Pump", "Disabled");
        }
    }
}
