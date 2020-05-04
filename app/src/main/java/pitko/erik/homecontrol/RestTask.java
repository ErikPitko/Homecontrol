package pitko.erik.homecontrol;

import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleGraph;

/**
 * Android RestTask (REST) from the Android Recipes book.
 */
public class RestTask extends AsyncTask<String, Void, String> {
    private FragmentSingleGraph graph;

    public RestTask(FragmentSingleGraph graph) {
        this.graph = graph;
    }

    @Override
    protected String doInBackground(String... params) {
        String stringUrl = params[0];
        String inputLine;
        String result;
        try {
            URL myUrl = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(3000);
            //Connect to our url
            conn.connect();
            //Create a new InputStreamReader
            InputStreamReader streamReader = new
                    InputStreamReader(conn.getInputStream());
            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();
        } catch (Exception e) {
            // TODO handle this properly
            e.printStackTrace();
            return "";
        }

        return result;
    }

    /**
     * `onPostExecute` is run after `doInBackground`, and it's
     * run on the main/ui thread, so you it's safe to update ui
     * components from it. (this is the correct way to update ui
     * components.)
     */
    @Override
    protected void onPostExecute(String result) {
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
            for (int i = 0; i < arr.length(); ++i) {
                DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime(arr.getJSONObject(i).getString("datetime"));
                if (week != dateTime.getWeekOfWeekyear() && dateTime.getHourOfDay() == 0) {
                    week = dateTime.getWeekOfWeekyear();
                    graph.addDayLimitLineValue(dateTime.getMillis());
                }
                data.add(new Entry(dateTime.getMillis(), (float) arr.getJSONObject(i).getDouble("value")));
            }

            graph.addSeries(new LineDataSet(data, "Dataset1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}