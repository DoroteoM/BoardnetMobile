package doroteo.boardnetmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Game extends AppCompatActivity {
    private SharedPreferences preferences;
    private String URL = "https://boardnetapi.000webhostapp.com/api";
    private String bgg_game_id;
    private TextView publishedValueTextView, playersValueTextView, timeValueTextView, ratingValueTextView, rankValueTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Game");
        setContentView(R.layout.activity_game);
        preferences = getSharedPreferences("API", MODE_PRIVATE);

        this.getGame();
    }

    private void getGame() {
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        publishedValueTextView = (TextView) findViewById(R.id.publishedValueTextView);
        playersValueTextView = (TextView) findViewById(R.id.playersValueTextView);
        timeValueTextView = (TextView) findViewById(R.id.timeValueTextView);
        ratingValueTextView = (TextView) findViewById(R.id.ratingValueTextView);
        rankValueTextView = (TextView) findViewById(R.id.rankValueTextView);

        RequestQueue requestQueue = Volley.newRequestQueue(Game.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/games/" + bgg_game_id,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                if (!response.getJSONObject("result").getString("name").equals("null"))
                                    setTitle(response.getJSONObject("result").getString("name"));
                                if (!response.getJSONObject("result").getString("year_published").equals("null"))
                                    publishedValueTextView.setText(response.getJSONObject("result").getString("year_published"));
                                String maxP, minP;
                                if (!response.getJSONObject("result").getString("min_players").equals("null"))
                                    minP = response.getJSONObject("result").getString("min_players");
                                else
                                    minP = "";
                                if (!response.getJSONObject("result").getString("max_players").equals("null"))
                                    maxP = response.getJSONObject("result").getString("max_players");
                                else
                                    maxP = "";
                                if (maxP.equals(minP))
                                    playersValueTextView.setText(minP);
                                else
                                    playersValueTextView.setText(minP+"-"+maxP);
                                if (!response.getJSONObject("result").getString("playing_time").equals("null"))
                                    timeValueTextView.setText(response.getJSONObject("result").getString("playing_time"));
                                if (!response.getJSONObject("result").getString("average_rating").equals("null"))
                                    ratingValueTextView.setText(response.getJSONObject("result").getString("average_rating"));
                                if (!response.getJSONObject("result").getString("rank").equals("null"))
                                    rankValueTextView.setText(response.getJSONObject("result").getString("rank"));
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", "Game: failed reading. \r\n" + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka", "Request filed: " + error.toString());
                        Toast.makeText(Game.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}
