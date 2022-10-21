package pitko.erik.homecontrol.switches;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import lombok.Setter;
import pitko.erik.homecontrol.activity.AutomationConfig;
import pitko.erik.homecontrol.activity.MainActivity;

public class RelayAutomation extends Relay implements TextView.OnClickListener {
    private final transient AutomationConfig.CFG purpose;
    @Setter
    private Activity act;

    public RelayAutomation(String name, String topic, AutomationConfig.CFG purpose) {
        super(name, topic);
        this.purpose = purpose;
    }

    @Override
    public void onClick(View view) {
        if (act == null)
            return;
        Intent intent = new Intent(act, AutomationConfig.class);
        intent.putExtra("config", purpose);
        act.startActivity(intent);

    }
}
