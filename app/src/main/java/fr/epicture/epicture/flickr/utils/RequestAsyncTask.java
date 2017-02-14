package fr.epicture.epicture.flickr.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Stephane on 03/11/2016.
 */

public class RequestAsyncTask extends AsyncTask<Void, Integer, Void> {

    private static final Integer SECONDE = 1000;

    private static final Integer GET_CONNECTION_TIMEOUT = 3 * SECONDE;
    private static final Integer GET_READ_TIMEOUT = 15 * SECONDE;

    protected HttpsURLConnection httpsURLConnection;
    protected Integer httpResponseCode = null;
    protected Object json = null;
    protected JSONObject jsonError = null;

    private Context context;
    private boolean running;

    public RequestAsyncTask(@NonNull Context context) {
        this.context = context;
    }

    public void execute() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean isRunning() {
        return (running);
    }

    @NonNull
    protected Context getContext() {
        return (context);
    }

    @Override
    protected void onPreExecute() {
        running = true;
    }

    @Override
    @Nullable
    protected Void doInBackground(@Nullable Void... params) {

        return (null);
    }

    @Override
    protected void onPostExecute(Void result) {
        running = false;
    }

    @Override
    protected void onCancelled(@Nullable Void result) {
        running = false;
        try {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        super.onProgressUpdate(values);
    }

    protected void GET(String url) {
        try {
            this.initHttp(url);

            httpsURLConnection.setConnectTimeout(GET_CONNECTION_TIMEOUT);
            httpsURLConnection.setReadTimeout(GET_READ_TIMEOUT);
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.setUseCaches(false);

            try {
                this.getResponse();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                this.closeHttpClient();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private void initHttp(String path) throws IOException {
        try {
            URL url = new URL(path);
            httpsURLConnection = (HttpsURLConnection)url.openConnection();
        } catch (IOException e) {
            throw e;
        }
    }

    private void getResponse() throws Exception {
            httpResponseCode = httpsURLConnection.getResponseCode();

            Log.i("httpResponseCode", httpResponseCode + "");
            if (httpResponseCode == 200) {
                InputStream input = httpsURLConnection.getInputStream();
                if (input != null) {
                    try {
                        StringBuilder responseStrBuilder = new StringBuilder();
                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                        String inputStr;
                        while ((inputStr = streamReader.readLine()) != null)
                            responseStrBuilder.append(inputStr);
                        Log.i("response", responseStrBuilder.toString());
                        json = new JSONTokener(responseStrBuilder.toString()).nextValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    input.close();
                } else {
                    Log.i("response", "null");
                }
            } else {
                InputStream error = httpsURLConnection.getErrorStream();
                if (error != null) {
                    try {
                        StringBuilder errorStrBuilder = new StringBuilder();
                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(error, "UTF-8"));
                        String inputStr;
                        while ((inputStr = streamReader.readLine()) != null)
                            errorStrBuilder.append(inputStr);

                        Log.i("response", errorStrBuilder.toString());
                        jsonError = new JSONObject(errorStrBuilder.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    error.close();
                }
            }
        return;
    }

    private void closeHttpClient() {
        running = false;
        try {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
