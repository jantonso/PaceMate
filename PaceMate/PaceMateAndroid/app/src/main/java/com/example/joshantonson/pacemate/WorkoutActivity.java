package com.example.joshantonson.pacemate;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class WorkoutActivity extends ActionBarActivity {

    final private static String WEB_SERVER_IP_ADDR = "http://192.168.0.16:3000";
    final private static String ARDUINO_IP_ADDR = "http://192.168.43.243";
    private String current_workout_id = null;
    private Integer current_pace_val = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        String temp_pace_val = getIntent().getExtras().getString("pace_val");
        String temp_dist_val = getIntent().getExtras().getString("dist_val");
        current_workout_id = getIntent().getExtras().getString("workout_id").replace("\"", "");

        Log.d("intent_test", "temp_pace_val = " + temp_pace_val);
        Log.d("intent_test", "temp_dist_val = " + temp_dist_val);
        Log.d("intent_test", "w_id = " + current_workout_id);

        // Send the pace and distance up to the arduino
        String url = ARDUINO_IP_ADDR + "/arduino/pace/" + temp_pace_val + "?td=" +
                temp_dist_val + "&?w_id=" + current_workout_id;
        new SendToArduino().execute(url);

        // Set the values for the temporary set pace
        TextView current_pace = (TextView) findViewById(R.id.tp);
        current_pace.setText("Target pace: " + temp_pace_val + "s");

        // Set the values for the workout id
        TextView workout_id = (TextView) findViewById(R.id.w_id);
        workout_id.setText("workout_id: " + current_workout_id);

        // Set the values for the target distance
        TextView current_dist = (TextView) findViewById(R.id.td);
        current_dist.setText("Target distance: " + temp_dist_val + "m");

        final Button start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_workout_id != null && current_pace_val != null) {
                    String url = ARDUINO_IP_ADDR + "/arduino/start/true";
                    new SendToArduino().execute(url);
                }
            }
        });

        final Button stop = (Button) findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_workout_id != null && current_pace_val != null) {
                    String url = ARDUINO_IP_ADDR + "/arduino/stop/true";
                    new SendToArduino().execute(url);
                }
            }
        });

        final Button view_results = (Button) findViewById(R.id.results_button);
        view_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), WorkoutResults.class);
                //startActivity(intent);
                Intent view_intent = new Intent("android.intent.action.VIEW",
                        Uri.parse(WEB_SERVER_IP_ADDR));
                startActivity(view_intent);
            }
        });
    }

    private class SendToArduino extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Send the data up to the database
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(params[0]);

            try {
                Log.d("TAG", "Sending http get to arduino...");
                HttpResponse response = httpclient.execute(httpget);
                String responseString = new BasicResponseHandler().handleResponse(response);
                return responseString;
            } catch (Exception e) {
                return "failure";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("TAG", "arduino get result: " + result);
            TextView status = (TextView) findViewById(R.id.status);
            if (result.equals("failure")) {
                status.setText("Status: failed to set pace/distance...");
            } else{
                try {
                    current_pace_val = Integer.parseInt(result);
                    status.setText("Status: pace/distance have been set!");
                } catch (NumberFormatException e) {
                    status.setText("Status: failed to set pace/distance...");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout, menu);
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
