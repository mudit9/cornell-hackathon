package com.vansuita.pickimage.sample.act;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.vansuita.pickimage.sample.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

public class AsyncTaskActivity extends Activity{

    Button btn;
    TextView t1;
    AsyncTask<?, ?, ?> runningTask;
    Bitmap bmp;
    String prediction;
    TextView t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        t1 = findViewById(R.id.textID2);
        t2 = findViewById(R.id.help);
        t2.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.poppinsmedium));
        t1.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.poppinsmedium));

        LongOperation  task1 = new LongOperation();
        task1.execute();
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, "Eye doctors around me"); // query contains search string
                startActivity(intent);
            }
        });
        // Because we implement OnClickListener, we only
        // have to pass "this" (much easier)
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running task(s) to avoid memory leaks
        if (runningTask != null)
            runningTask.cancel(true);
    }


    private final class LongOperation extends AsyncTask<Void, Void, String> {
        JSONObject jsono;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            t1.setVisibility(View.GONE);
        }



        @Override
        protected String doInBackground(Void... voids) {
            try {
                System.out.println("Here");

                //------------------>>
                String url = "https://hackncheese-app.herokuapp.com/predict";
                // Build the JSON object to pass parameters
                JSONObject jsonObj = new JSONObject();
// Create the POST object and add the parameters
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
                jsonObj.put("data",imageEncoded);
                HttpPost httpPost = new HttpPost(url);
                StringEntity entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
                httpPost.setEntity(entity);
                entity.setContentType("application/json");

                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(httpPost);

                System.out.println("response" + response.toString());
                int status = response.getStatusLine().getStatusCode();
                System.out.println("status" + status);

                if (status == 200) {
                    HttpEntity entity2 = response.getEntity();
                    String data = EntityUtils.toString(entity2);
                    System.out.println("Data" + data);

                    jsono = new JSONObject(data);


                }else{
                    jsono = new JSONObject();
                    jsono.put("data","Invalid, status code: " + status);
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

                e.printStackTrace();
            }
            return null;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String result = "Invalid";
            TextView t1 = ((TextView) findViewById(R.id.textID2));
            System.out.println(jsono + jsono.toString());
            View view = findViewById(R.id.view);
            String pred = "";
            final TickPlusDrawable tickPlusDrawable = new TickPlusDrawable(getResources().getDimensionPixelSize(R.dimen.stroke_width), getResources().getColor(android.R.color.holo_blue_dark), Color.WHITE);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(tickPlusDrawable);
            } else {
                view.setBackground(tickPlusDrawable);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tickPlusDrawable.toggle();
                }
            });
            try {
                pred = (String) jsono.get("prediction");
                result = "The result is: \n" +  pred;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            t1.setText(result);
            t1.setTextSize(30);
            result = result.trim();
            System.out.println(result + result.toString());
            if (pred.equals("Mild") || pred.equals("Moderate")){
                t2.setVisibility(View.VISIBLE);
                t1.setTextColor(Color.parseColor("#FF0000"));
                t2.setText("Oh no. You should seek professional help! Click here to get help resources!");
                t1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                t2.setPaintFlags(t2.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

            }
            else if (pred.equals("Severe") || pred.equals("Proliferative Diabetic Retinopathy")){
                t2.setVisibility(View.VISIBLE);
                t1.setTextColor(Color.parseColor("#FF0000"));
                t2.setText("Oh no. You should URGENTLY seek professional help NOW! Click here to get help resources!");
                t1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                t2.setPaintFlags(t2.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

            }
            else{
                t1.setTextColor(Color.parseColor("#0062cc"));
                t2.setText("You're good!");
                t1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);

            }
        }
    }
}