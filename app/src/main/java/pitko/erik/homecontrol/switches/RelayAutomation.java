package pitko.erik.homecontrol.switches;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import pitko.erik.homecontrol.activity.AutomationConfig;
import pitko.erik.homecontrol.activity.MainActivity;

public class RelayAutomation extends Relay implements TextView.OnClickListener {
    private final transient AutomationConfig.CFG purpose;
    private final Activity act;

    public RelayAutomation(String name, String topic, AutomationConfig.CFG purpose, Activity activity) {
        super(name, topic);
        this.purpose = purpose;
        this.act = activity;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(act, AutomationConfig.class);
        intent.putExtra("config", purpose);
        act.startActivity(intent);

    }
}
