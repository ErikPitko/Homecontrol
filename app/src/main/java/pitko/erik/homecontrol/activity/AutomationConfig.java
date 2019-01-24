package pitko.erik.homecontrol.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.configs.VentilationFragment;

public class AutomationConfig extends AppCompatActivity {
    public enum CFG {VENTILATION}

    private static Activity act;

    public static void pushToast(String msg) {
        if (act != null)
            act.runOnUiThread(() -> Toast.makeText(act.getApplicationContext(), msg,
                    Toast.LENGTH_SHORT).show());
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automation_config);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e("AutomationConfig", "No extra data");
            finish();
        }

        switch ((CFG) extras.get("config")) {
            case VENTILATION:
                Fragment fragment = new VentilationFragment();
                setFragment(fragment);
                break;
        }

    }

}
