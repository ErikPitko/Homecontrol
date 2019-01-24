package pitko.erik.homecontrol;

import android.os.AsyncTask;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import pitko.erik.homecontrol.activity.MainActivity;
import pitko.erik.homecontrol.fragments.FragmentSingleGraph;

/**
 * Android RestTask (REST) from the Android Recipes book.
 */
public class RestTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "AARestTask";

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
//        Log.i(TAG, "RESULT = " + result);
        try {
            result = "{ \"data\": " + result + " }";
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONArray("data");
            if (arr.length() < 2) {
                MainActivity.pushToast(MainActivity.getResourcebyId("stat_no_data"));
                return;
            }
            DataPoint data[] = new DataPoint[arr.length()];
            Date min = null, max = null;
            for (int i = 0; i < arr.length(); ++i) {
                DateTime dateTime = ISODateTimeFormat.dateTime().parseDateTime(arr.getJSONObject(i).getString("datetime"));
                data[i] = new DataPoint(dateTime.toCalendar(Locale.getDefault()).getTime(), arr.getJSONObject(i).getDouble("value"));
                if (i == 0) {
                    min = dateTime.toCalendar(Locale.getDefault()).getTime();
                } else if (i == arr.length() - 1) {
                    max = dateTime.toCalendar(Locale.getDefault()).getTime();
                }
            }
            graph.setBounds(min, max);
            graph.addSeries(new LineGraphSeries<>(data));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}