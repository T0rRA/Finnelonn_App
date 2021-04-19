package com.bachelor_group54.funnregistrering;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.CheckedOutputStream;

import javax.net.ssl.HttpsURLConnection;

public class UploadToServer extends AsyncTask<String, Void, String> {
    private Context context;

    public UploadToServer(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        //Makes the url
        StringBuilder query = new StringBuilder("https://funnapi.azurewebsites.net/" + strings[0] + "?");
        for (int i = 2; i < strings.length; i++) {
            if (i > 2) {
                query.append("&");
            }
            query.append(strings[i]);
        }

        try {
            JSONObject jsonObject = new JSONObject();

            //Makes jsonObject for the image, and puts the base64 string of the image
            String[] nameValueStrings = strings[1].split("="); //Element 1 in string contains the image (image=imageString)
            jsonObject.put(nameValueStrings[0], nameValueStrings[1]);

            String data = jsonObject.toString();

            URL url = new URL(query.toString());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(data.getBytes().length); //Enables streaming of HTTP request body without internal buffering
            connection.setRequestProperty("ENCTYPE", "multipart/form-data");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write(data);

            Log.d("ImageUploadLog", "Data to php = " + data);
            writer.flush();
            writer.close();
            out.close();
            connection.connect();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String result = sb.toString();
            Log.d("ImageUploadLog", "Response from php = " + result);
            //Response = new JSONObject(result);
            connection.disconnect();

            return result;
        } catch (Exception e) {
            Log.d("ImageUploadLog", "Error Encountered");
            e.printStackTrace();
            return "No good";
        }
    }

    @Override
    protected void onPostExecute(String string) {
        if (context != null) {
            Toast.makeText(context, string, Toast.LENGTH_LONG).show();
        }
    }
}