package com.example.joshantonson.pacemate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    final private static String WEB_SERVER_IP_ADDR = "http://192.168.0.16:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button create_workout = (Button) findViewById(R.id.submit_button);
        create_workout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText coach_text = (EditText) findViewById(R.id.coach_val);
                String coach_val = coach_text.getText().toString();

                EditText athlete_text = (EditText) findViewById(R.id.athlete_val);
                String athlete_val = athlete_text.getText().toString();

                EditText desc_text = (EditText) findViewById(R.id.description_val);
                String desc_val = desc_text.getText().toString();

                EditText pace_text = (EditText) findViewById(R.id.pace_val);
                String pace_val = pace_text.getText().toString();

                EditText dist_text = (EditText) findViewById(R.id.distance_val);
                String dist_val = dist_text.getText().toString();

                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                new CreateNewWorkout().execute(currentDate, coach_val, athlete_val,
                                               desc_val, pace_val, dist_val);
            }
        });

        final Button view_results = (Button) findViewById(R.id.results_button);
        view_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view_intent = new Intent("android.intent.action.VIEW",
                                               Uri.parse(WEB_SERVER_IP_ADDR));
                startActivity(view_intent);
            }
        });
    }

    private class CreateNewWorkout extends AsyncTask<String, Void, String> {

        private String current_pace_val;
        private String current_dist_val;

        @Override
        protected String doInBackground(String... params) {

            // Send the data up to the database
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(WEB_SERVER_IP_ADDR + "/create_new_workout");

            // POST Data
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(5);
            nameValuePair.add(new BasicNameValuePair("date", params[0]));
            nameValuePair.add(new BasicNameValuePair("coach", params[1]));
            nameValuePair.add(new BasicNameValuePair("athlete", params[2]));
            nameValuePair.add(new BasicNameValuePair("description", params[3]));
            nameValuePair.add(new BasicNameValuePair("target_pace", params[4]));
            nameValuePair.add(new BasicNameValuePair("target_distance", params[5]));

            current_pace_val = params[4];
            current_dist_val = params[5];

            // Encoding POST data
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "failure";
            }

            // Making the POST request
            try {
                Log.d("TAG", "Sending post request to web server...");
                HttpResponse response = httpclient.execute(httpPost);
                return EntityUtils.toString(response.getEntity());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return "failure";
            } catch (IOException e) {
                e.printStackTrace();
                return "failure";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("TAG", "result of create workout: " + result);
            if (!result.equals("failure")) {
                Intent intent = new Intent(getApplicationContext(), WorkoutActivity.class);
                intent.putExtra("workout_id", result);
                intent.putExtra("pace_val", current_pace_val);
                intent.putExtra("dist_val", current_dist_val);
                startActivity(intent);
            } else {
                // Create failure message
                TextView textView = new TextView(getApplicationContext());
                textView.setText("failed to create the workout...");

                final RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);

                params.addRule(RelativeLayout.BELOW, R.id.submit_button);
                textView.setLayoutParams(params);

                RelativeLayout currentLayout = (RelativeLayout) findViewById(R.id.layout);
                currentLayout.addView(textView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
