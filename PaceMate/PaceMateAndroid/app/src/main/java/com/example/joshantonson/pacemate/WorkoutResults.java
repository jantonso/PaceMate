package com.example.joshantonson.pacemate;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WorkoutResults extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_results);

        new FetchWorkouts().execute("http://pelagic-plexus-87318.appspot.com/?user_name=jantonso");
    }

    private class FetchWorkouts extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // Send the data up to the database
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(params[0]);

            try {
                Log.d("TAG", "Http get request to app engine");
                HttpResponse response = httpclient.execute(httpget);
                Log.d("TAG", response.getStatusLine().toString());
                Log.d("TAG", response.getEntity().toString());
                String responseString = new BasicResponseHandler().handleResponse(response);
                return responseString;
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Context context = getApplicationContext();
            if (result.equals("failure")) {
                Toast toast = Toast.makeText(context, "it failed...", Toast.LENGTH_LONG);
                toast.show();
            } else{
                try {
                    JSONObject json = new JSONObject(result);

                    LinearLayout currentLayout = (LinearLayout) findViewById(R.id.layout);

                    String user_name = json.getString("user_name");
                    Log.d("TAG", "user_name: " + user_name);

                    JSONArray workouts = json.getJSONArray("workouts");
                    Log.d("TAG", workouts.toString());

                    // Add each workout to the view
                    for (int i = 0; i < workouts.length(); i++) {
                        Log.d("TAG", "Looping dog");
                        JSONObject workout = (JSONObject) workouts.get(i);
                        int workout_num = (int) workout.get("workout_num");
                        TextView workoutLabel = new TextView(getApplicationContext());
                        workoutLabel.setText("Workout Number " + workout_num + ":");
                        currentLayout.addView(workoutLabel);

                        JSONArray splits = (JSONArray) workout.get("split_times");
                        JSONArray distances = (JSONArray) workout.get("split_distances");

                        // Add all the splits and their respective distances to the view
                        for (int j = 0; j < splits.length(); j++) {
                            TextView new_split = new TextView(getApplicationContext());
                            new_split.setText(distances.get(j) + " split = " + splits.get(j));
                            currentLayout.addView(new_split);
                        }
                    }
                } catch (JSONException e) {
                    Toast toast = Toast.makeText(context, result, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workout_results, menu);
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
