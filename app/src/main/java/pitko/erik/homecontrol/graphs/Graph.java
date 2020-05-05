package pitko.erik.homecontrol.graphs;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import pitko.erik.homecontrol.RestTask;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleGraph;

import static android.widget.RelativeLayout.BELOW;

public class Graph {
    private static final String REST_HOST = "https://" + MainActivity.SERVER_HOST;
    private final String topic;
    private final String title;
    private final String layout;
    private static int graphCount = 1;

    private FragmentSingleGraph singleGraph;

    public enum TimePeriod {MONTH, WEEK, DAY}

    public Graph(String topic, String title, String layout) {
        this.topic = topic;
        this.title = title;
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public void loadChartData(TimePeriod timePeriod) {
        RestTask task = new RestTask(singleGraph);
        task.execute(REST_HOST + "/nodered/dataperiod?topic=" + this.topic + "&timeperiod=" + timePeriod.name());
    }

    public void draw(Fragment instance, RelativeLayout placeHolder) {
        Context context = instance.getContext();

        RelativeLayout fl = new RelativeLayout(context);
        RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        View lastView = placeHolder.getChildAt(placeHolder.getChildCount() - 1);
        if (lastView != null)
            flp.addRule(BELOW, lastView.getId());
        fl.setLayoutParams(flp);
        fl.setId(graphCount++);


//        Adding the RelativeLayout to the placeholder as a child
        placeHolder.addView(fl);

        singleGraph = new FragmentSingleGraph(this);
        singleGraph.setText(MainActivity.getResourcebyId(this.title));
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), singleGraph);
        transaction.commit();

        loadChartData(TimePeriod.DAY);
    }
}
