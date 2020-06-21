package pitko.erik.homecontrol;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestTask extends AsyncTask<String, Void, String> {
    public enum METHOD {GET, POST, PUT}

    private METHOD method;
    private Callback backgroundCallback;
    private Callback postExecuteCallback;
    private int readTimeout = 10000;
    private int connectTimeout = 3000;
    private JSONObject jsonOut = null;
    private HttpURLConnection conn;

    private Object backgroundCallbackResult;

    public interface Callback {
        void doJob(RestTask task, String data);
    }

    public RestTask(METHOD method) {
        this.method = method;
    }

    public void setBackgroundCallback(Callback cb) {
        this.backgroundCallback = cb;
    }

    public void setPostExecuteCallback(Callback cb) {
        this.postExecuteCallback = cb;
    }

    public void setBackgroundCallbackResult(Object backgroundCallbackResult) {
        this.backgroundCallbackResult = backgroundCallbackResult;
    }

    public Object getBackgroundCallbackResult() {
        return backgroundCallbackResult;
    }

    public HttpURLConnection getConn() {
        return conn;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setJsonOut(JSONObject jsonOut) {
        this.jsonOut = jsonOut;
    }

    @Override
    protected String doInBackground(String... params) {
        String stringUrl = params[0];
        String inputLine;
        String result = "";
        int responseCode = -1;
        try {
            URL myUrl = new URL(stringUrl);
            conn = (HttpURLConnection) myUrl.openConnection();
            conn.setRequestMethod(method.toString());
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectTimeout);
            //Connect to our url
            if (jsonOut != null)
                conn.setDoOutput(true);
            else
                conn.setDoOutput(false);
            conn.connect();

            if (jsonOut != null) {
                OutputStream out = conn.getOutputStream();
                out.write(jsonOut.toString().getBytes(StandardCharsets.UTF_8));
            }
            responseCode = conn.getResponseCode();
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
            //Close our InputStream, Buffered reader and connection
            reader.close();
            streamReader.close();
            conn.disconnect();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            if (responseCode >= 400 && responseCode <= 499) {
                Log.w("MQTT", "Error code from api");
            }
        } catch (Exception e) {
            // TODO handle this properly
            e.printStackTrace();
            return "";
        }

        if (this.backgroundCallback != null) {
            this.backgroundCallback.doJob(this, result);
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
        if (this.postExecuteCallback != null) {
            this.postExecuteCallback.doJob(this, result);
        }
    }
}