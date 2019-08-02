package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlaySoloScore extends AppCompatActivity {
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private String myUsername, bgg_game_id, gameMode;
    private EditText pointsEditText, durationEditText;
    private Switch wonSwitch;
    private Button saveSoloPlayButton;
    private Integer playId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_solo_score);
        setTitle("Score");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        myUsername = preferences.getString("username", "test");
        gameMode = "SOLO";
        pointsEditText = (EditText) findViewById(R.id.pvpPlayerPointsEditText);
        durationEditText = (EditText) findViewById(R.id.durationEditText);
        wonSwitch = (Switch) findViewById(R.id.wonSwitch);
        saveSoloPlayButton = (Button) findViewById(R.id.saveSoloPlayButton);

        saveSoloPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSoloScore();
            }
        });
    }

    private void saveSoloScore() {
        progress = new ProgressDialog(PlaySoloScore.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Saving play");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        addPlay();
    }

    private void addPlay() {
        new Thread(new Runnable() {
            public void run() {
                RequestQueue requestQueue = Volley.newRequestQueue(PlaySoloScore.this);
                Map<String, String> params = new HashMap<String, String>();
                params.put("bgg_game_id", bgg_game_id);
                params.put("username", myUsername);
                params.put("mode", gameMode);
                if (!durationEditText.getText().toString().equals(""))
                    params.put("duration", durationEditText.getText().toString());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        URL + "/play",
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getString("success").equals("true")) {
                                        playId = response.getJSONObject("result").getInt("id");
                                        addPlayer();
                                    } else {
                                        Toast.makeText(PlaySoloScore.this, "Play: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        progress.dismiss();
                                    }
                                } catch (JSONException e) {
                                    Log.e("Poruka", e.toString());
                                    Toast.makeText(PlaySoloScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    progress.dismiss();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError e) {
                                Log.e("Poruka", "Error: " + e.toString());
                                Toast.makeText(PlaySoloScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                progress.dismiss();
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> header = new HashMap<String, String>();
                        header.put("Authorization","Bearer " + preferences.getString("token", ""));
                        return header;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            }
        }).start();
    }

    private void addPlayer() {
        String won = wonSwitch.isChecked() ? "1" : "0";
        RequestQueue requestQueue = Volley.newRequestQueue(PlaySoloScore.this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("play_id", playId.toString());
        params.put("username", myUsername);
        params.put("won", won);
        if (!pointsEditText.getText().toString().equals(""))
            params.put("points", pointsEditText.getText().toString());
        if (!durationEditText.getText().toString().equals(""))
            params.put("duration", durationEditText.getText().toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL + "/player",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("success").equals("true")) {
                                Toast.makeText(PlaySoloScore.this, "Play added successfully", Toast.LENGTH_LONG).show();
                                progress.dismiss();
                            } else {
                                Toast.makeText(PlaySoloScore.this, "Player: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                progress.dismiss();
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", e.toString());
                            Toast.makeText(PlaySoloScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            progress.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.e("Poruka", "Error: " + e.toString());
                        Toast.makeText(PlaySoloScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> header = new HashMap<String, String>();
                header.put("Authorization","Bearer " + preferences.getString("token", ""));
                return header;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    //Back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
