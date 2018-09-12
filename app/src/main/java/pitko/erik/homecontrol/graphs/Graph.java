package pitko.erik.homecontrol.graphs;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;

import pitko.erik.homecontrol.RestTask;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleGraph;

import static android.widget.RelativeLayout.BELOW;

public class Graph {
    private final String topic;
    private final String title;
    private final String layout;
    private static int graphCount = 1;

    public Graph(String topic, String title, String layout) {
        this.topic = topic;
        this.title = title;
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    private String getResourcebyId(Context context, String name) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
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

        FragmentSingleGraph singleGraph = new FragmentSingleGraph();
        singleGraph.setText(getResourcebyId(context, this.title));
        FragmentTransaction transaction = instance.getChildFragmentManager().beginTransaction();
        transaction.replace(fl.getId(), singleGraph);
        transaction.commit();

        RestTask rest = new RestTask(singleGraph);
        String url;
        if (!MainActivity.LOCAL_NET) {
            url = "http://kosec.ddns.net:1880/data?topic=";
        } else {
            url = "http://192.168.1.48:1880/data?topic=";
        }
        rest.execute(url + topic);
    }
}
