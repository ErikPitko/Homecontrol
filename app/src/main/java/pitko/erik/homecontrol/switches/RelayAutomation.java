package pitko.erik.homecontrol.switches;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import pitko.erik.homecontrol.activity.AutomationConfig;
import pitko.erik.homecontrol.activity.MainActivity;

public class RelayAutomation extends Relay implements TextView.OnClickListener {
    public RelayAutomation(String name, String topic) {
        super(name, topic);
    }

    @Override
    public void onClick(View view) {
        Activity act = MainActivity.getAct();
        Intent intent = new Intent(act, AutomationConfig.class);
        intent.putExtra("config", AutomationConfig.CFG.VENTILATION);
        act.startActivity(intent);

    }
}
