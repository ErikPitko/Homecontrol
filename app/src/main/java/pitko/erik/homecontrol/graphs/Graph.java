package pitko.erik.homecontrol.graphs;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pitko.erik.homecontrol.RestTask;
import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleGraph;
import pitko.erik.homecontrol.models.SensorShared;

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

    public String getTopic() {
        return topic;
    }

    public String getLayout() {
        return layout;
    }

    public void loadChartData(TimePeriod timePeriod) {
        RestTask task = new RestTask(RestTask.METHOD.GET);
        task.setBackgroundCallback(this::processGraphData);
        task.setPostExecuteCallback(this::drawData);

        task.execute(REST_HOST + "/sensorapi/v1/dataperiod?topic=" + this.topic + "&timeperiod=" + timePeriod.name());
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

        SensorShared sensorShared = MainActivity.sensorPrefs.get(this.topic);
        if (sensorShared != null && sensorShared.isChartHidden()){
            return;
        }
        loadChartData(TimePeriod.DAY);
    }

    private void processGraphData(RestTask task, String result) {
        if (result.equals(""))
            return;
        try {
            result = "{ \"data\": " + result + " }";
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONArray("data");
            if (arr.length() < 2) {
                MainActivity.pushToast(MainActivity.getResourcebyId("stat_no_data"));
                return;
            }
            ArrayList<Entry> data = new ArrayList<>();

            int week = -1;
            int dom = -1;
            for (int i = 0; i < arr.length(); ++i) {
                DateTime dateTime;
                try {
                    dateTime = ISODateTimeFormat.dateTime().parseDateTime(arr.getJSONObject(i).getString("datetime"));
                } catch (IllegalArgumentException e) {
                    dateTime = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(arr.getJSONObject(i).getString("datetime"));
                }
                if (singleGraph.getCurrentTimePeriod() == Graph.TimePeriod.MONTH) {
                    if (week != dateTime.getWeekOfWeekyear() && dateTime.getDayOfWeek() == 1 && dateTime.getHourOfDay() == 0) {
                        week = dateTime.getWeekOfWeekyear();
                        singleGraph.addDayLimitLineValue(dateTime.getMillis());
                    }
                } else {
                    if (dom != dateTime.getDayOfMonth() && dateTime.getHourOfDay() == 0) {
//                        week = dateTime.getWeekOfWeekyear();
                        dom = dateTime.getDayOfMonth();
                        singleGraph.addDayLimitLineValue(dateTime.getMillis());
                    }
                }
                data.add(new Entry(dateTime.getMillis(), (float) arr.getJSONObject(i).getDouble("value")));
            }
            task.setBackgroundCallbackResult(new LineDataSet(data, "Dataset1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawData(RestTask task, String result) {
        Object obj = task.getBackgroundCallbackResult();
        singleGraph.addSeries((LineDataSet) obj);
    }
}
