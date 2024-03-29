package pitko.erik.homecontrol.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import pitko.erik.homecontrol.R;
import pitko.erik.homecontrol.fragments.configs.DryoutFragment;
import pitko.erik.homecontrol.fragments.configs.VentilationFragment;

public class AutomationConfig extends AppCompatActivity {
    public enum CFG {VENTILATION, DRYOUT}

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

        Fragment fragment;
        switch ((CFG) Objects.requireNonNull(extras.get("config"))) {
            case VENTILATION:
                fragment = new VentilationFragment();
                break;
            case DRYOUT:
            default:
                fragment = new DryoutFragment();
                break;
        }
        findViewById(R.id.fab).setOnClickListener((View.OnClickListener)fragment);
        setFragment(fragment);

    }

}
