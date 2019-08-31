package doroteo.boardnetmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static doroteo.boardnetmobile.ErrorResponse.*;

public class PlayPvpEditPlayer extends MainClass {
    private SharedPreferences preferences;
    private String myUsername, playId, playerId, playerUsername, bgg_game_id;
    private Spinner friendsSpinner;
    private Switch wonSwitch;
    private EditText pvpPlayerNameEditText, pvpPlayerPointsEditText;
    private Button savePlayerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pvp_edit_player);
        setTitle("Add player");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        myUsername = preferences.getString("username", "test");
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        playId = getIntent().getStringExtra("playId");
        playerId = getIntent().getStringExtra("playerId");
        playerUsername = "";
        pvpPlayerNameEditText = (EditText) findViewById(R.id.pvpPlayerNameEditText);
        pvpPlayerPointsEditText = (EditText) findViewById(R.id.pvpPlayerPointsEditText);
        wonSwitch = (Switch) findViewById(R.id.wonSwitch);
        friendsSpinner = (Spinner) findViewById(R.id.friendsSpinner);
        savePlayerButton = (Button) findViewById(R.id.savePlayerButton);


        if (playerId != null)
            fillPlayerData();
        else
            fillSpinner();

        savePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlayer();
            }
        });
    }

    private void fillPlayerData() {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpEditPlayer.this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    URL + "/player/" + playerId,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")) {
                                    if (!response.get("result").equals(null)) {
                                        Boolean won = response.getJSONObject("result").getString("won").equals("1") ? true : false;
                                        pvpPlayerNameEditText.setText(response.getJSONObject("result").getString("name"));
                                        if (!response.getJSONObject("result").getString("points").equals("null"))
                                            pvpPlayerPointsEditText.setText(response.getJSONObject("result").getString("points"));
                                        wonSwitch.setChecked(won);
                                        if (!response.getJSONObject("result").getString("user").equals("null"))
                                            playerUsername = response.getJSONObject("result").getJSONObject("user").getString("username");
                                        fillSpinner();
                                    }
                                } else {
                                    Log.e("Poruka", response.getString("result"));
                                    Toast.makeText(PlayPvpEditPlayer.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Log.e("Poruka", "Error: " + e);
                                Toast.makeText(PlayPvpEditPlayer.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            errorResponse(e, PlayPvpEditPlayer.this);
                            if (e.networkResponse.statusCode == 401) {
                                finish();
                                Intent myIntent = new Intent(getBaseContext(), Login.class);
                                startActivity(myIntent);
                            }
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> header = new HashMap<String, String>();
                    header.put("Authorization", "Bearer " + preferences.getString("token", ""));
                    return header;
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillSpinner() {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpEditPlayer.this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    URL + "/play/friends-not-in-play/" + playId,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")) {
                                    if (!response.get("result").equals(null)) {
                                        List<String> friendsArray = new ArrayList<String>();
                                        JSONArray friendsList = response.getJSONArray("result");
                                        friendsArray.add("");
                                        for (int i = 0; i < friendsList.length(); i++) {
                                            friendsArray.add(friendsList.getJSONObject(i).getString("username"));
                                        }
                                        if (!playerUsername.equals(""))
                                            friendsArray.add(playerUsername);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlayPvpEditPlayer.this, android.R.layout.simple_spinner_item, friendsArray);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        friendsSpinner.setAdapter(adapter);
                                        friendsSpinner.setSelection(adapter.getPosition(playerUsername));
                                    }
                                } else {
                                    Log.e("Poruka", response.getString("result"));
                                    Toast.makeText(PlayPvpEditPlayer.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Log.e("Poruka", "Error: " + e);
                                Toast.makeText(PlayPvpEditPlayer.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            errorResponse(e, PlayPvpEditPlayer.this);
                            if (e.networkResponse.statusCode == 401) {
                                finish();
                                Intent myIntent = new Intent(getBaseContext(), Login.class);
                                startActivity(myIntent);
                            }
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> header = new HashMap<String, String>();
                    header.put("Authorization", "Bearer " + preferences.getString("token", ""));
                    return header;
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePlayer() {
        RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpEditPlayer.this);
        Map<String, String> params = new HashMap<String, String>();
        if (!pvpPlayerNameEditText.getText().toString().equals(""))
            params.put("name", pvpPlayerNameEditText.getText().toString());
        if (!friendsSpinner.getSelectedItem().toString().equals(""))
            params.put("username", friendsSpinner.getSelectedItem().toString());
        if (!pvpPlayerPointsEditText.getText().toString().equals(""))
            params.put("points", pvpPlayerPointsEditText.getText().toString());
        params.put("won", wonSwitch.isChecked() ? "1" : "0");
        params.put("play_id", playId);
        if (!pvpPlayerPointsEditText.getText().toString().equals(""))
            params.put("points", pvpPlayerPointsEditText.getText().toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                URL + "/player/" + playerId,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("success").equals("true")) {
                                Toast.makeText(PlayPvpEditPlayer.this, "Player stats saved", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PlayPvpEditPlayer.this, response.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", e.toString());
                            Toast.makeText(PlayPvpEditPlayer.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        errorResponse(e, PlayPvpEditPlayer.this);
                        if (e.networkResponse.statusCode == 401) {
                            finish();
                            Intent myIntent = new Intent(getBaseContext(), Login.class);
                            startActivity(myIntent);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> header = new HashMap<String, String>();
                header.put("Authorization", "Bearer " + preferences.getString("token", ""));
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
            finish();
            Intent myIntent = new Intent(PlayPvpEditPlayer.this, PlayPvpScore.class);
            myIntent.putExtra("bgg_game_id", bgg_game_id);
            myIntent.putExtra("playId", playId);
            startActivity(myIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
