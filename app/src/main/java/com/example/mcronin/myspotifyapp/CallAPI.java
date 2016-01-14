package com.example.mcronin.myspotifyapp;

/**
 * Created by mcronin on 1/14/2016.
 */
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by mattcronin on 9/29/15.
 */
public class CallAPI extends AsyncTask<String, String, String> {

    private String apiUrl = "";
    private PostExecutable executable;
    private String authToken;

    private static final String AUTHORIZATION = "Authorization";

    public CallAPI(String apiUrlBase){
        this(apiUrlBase, null);
    }

    public CallAPI(String apiUrlBase, String authToken){
        this(apiUrlBase, authToken, null);
    }

    public CallAPI(String apiUrlBase, String authToken, PostExecutable executable){
        this.apiUrl = apiUrlBase;
        this.authToken = authToken;
        this.executable = executable;
    }

    public void setExecutable(PostExecutable executable) {
        this.executable = executable;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        HttpsURLConnection urlConnection;
        try {
            URL url = new URL(apiUrl + params[0]);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod(RequestMethod.GET.toString());
            /*if (authToken != null) {
                urlConnection.setRequestProperty(AUTHORIZATION, authToken);
            }*/
            urlConnection.connect();
            //InputStream is = urlConnection.getInputStream();
            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            result = readStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String readStream(InputStream inputStream) {
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (executable != null){
            executable.Execute(result);
        }
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    public interface PostExecutable {
        void Execute(String results);
    }
}

