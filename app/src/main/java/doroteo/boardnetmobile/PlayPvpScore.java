package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

import doroteo.boardnetmobile.models.Player;

public class PlayPvpScore extends MainClass {
    private String myUsername, bgg_game_id, playId;
    private EditText pvpPlayerNameEditText, pvpPlayerPointsEditText;
    private Spinner friendsSpinner;
    private Switch wonSwitch;
    private Button addPlayerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pvp_score);
        setTitle("Score");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        myUsername = preferences.getString("username", "test");
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        playId = getIntent().getStringExtra("playId");

        pvpPlayerNameEditText = findViewById(R.id.pvpPlayerNameEditText);
        pvpPlayerPointsEditText = findViewById(R.id.pvpPlayerPointsEditText);
        friendsSpinner = findViewById(R.id.friendsSpinner);
        wonSwitch = findViewById(R.id.wonSwitch);
        addPlayerButton = findViewById(R.id.addPlayerButton);

        if (playId == null)
            this.createPlay();
        else
            this.getFriendList();


        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pvpPlayerNameEditText.getText().equals("") || !friendsSpinner.getSelectedItem().toString().equals("")) {
                    addPlayer();
                } else
                    Toast.makeText(PlayPvpScore.this, "You need to enter player name or select friend", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createPlay() {
        RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpScore.this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("bgg_game_id", bgg_game_id);
        params.put("username", myUsername);
        params.put("mode", "PVP");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL + "/play",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("success").equals("true")) {
                                playId = response.getJSONObject("result").getString("id");
                                getFriendList();
                            } else {
                                Toast.makeText(PlayPvpScore.this, "Play: " + response.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", e.toString());
                            Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        if (e.networkResponse.statusCode == 404) {
                            Toast.makeText(PlayPvpScore.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                        } else if (e.networkResponse.statusCode == 401) {
                            Toast.makeText(PlayPvpScore.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                            finish();
                            Intent myIntent = new Intent(getBaseContext(), Login.class);
                            startActivity(myIntent);
                        } else if (e.networkResponse.statusCode == 403) {
                            Toast.makeText(PlayPvpScore.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                        } else if (e.networkResponse.statusCode == 500) {
                            Toast.makeText(PlayPvpScore.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
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

    private void getFriendList() {
        progress = new ProgressDialog(PlayPvpScore.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpScore.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/play/" + playId,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            if (!response.get("result").equals(null)) {
                                                JSONArray playersList = response.getJSONObject("result").getJSONArray("players");
                                                List<JSONObject> listOfUsers = new ArrayList<JSONObject>();
                                                for (int i = 0; i < playersList.length(); i++) {
                                                    listOfUsers.add(playersList.getJSONObject(i));
                                                }
                                                if (listOfUsers.size() > 0)
                                                    createList(listOfUsers);
                                            }
                                            fillSpinner();
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(PlayPvpScore.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e);
                                        Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    if (e.networkResponse.statusCode == 404) {
                                        Toast.makeText(PlayPvpScore.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 401) {
                                        Toast.makeText(PlayPvpScore.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent myIntent = new Intent(getBaseContext(), Login.class);
                                        startActivity(myIntent);
                                    } else if (e.networkResponse.statusCode == 403) {
                                        Toast.makeText(PlayPvpScore.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 500) {
                                        Toast.makeText(PlayPvpScore.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
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
                    progress.dismiss();
                }
            }
        }).start();
    }

    private void createList(List<JSONObject> listOfUsers) throws JSONException {
        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

        for (JSONObject user : listOfUsers) {
            String playerId, name, won, points, score;
            playerId = user.getString("id");
            name = !user.getString("name").equals("null") ? user.getString("name") : "";
            points = !user.getString("points").equals("null") ? user.getString("points") + "   " : "";
            won = user.getString("won").equals("1") ? "WINNER" : "";
            score = points + won;

            Map<String, Object> listItemMap = new HashMap<String, Object>();
            listItemMap.put("playerId", playerId);
            listItemMap.put("name", name);
            listItemMap.put("score", score);
            itemDataList.add(listItemMap);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(PlayPvpScore.this, itemDataList, R.layout.layout_players,
                new String[]{"name", "score", "playId"}, new int[]{R.id.playerNameTextView, R.id.playerScoreTextView, R.id.playerIdTextView});

        ListView listView = (ListView) findViewById(R.id.playerListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String playerId = clickItemMap.get("playerId").toString();
                finish();
                Intent myIntent = new Intent(getBaseContext(), PlayPvpNewPlayer.class);
                myIntent.putExtra("playId", playId);
                myIntent.putExtra("playerId", playerId);
                myIntent.putExtra("bgg_game_id", bgg_game_id);
                startActivity(myIntent);
            }
        });
    }

    private void addPlayer() {
        RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpScore.this);
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
                Request.Method.POST,
                URL + "/player",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("success").equals("true")) {
                                Toast.makeText(PlayPvpScore.this, "Player stats saved", Toast.LENGTH_LONG).show();
                                emptyPlayerForm();
                                getFriendList();
                            } else {
                                Toast.makeText(PlayPvpScore.this, response.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", e.toString());
                            Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        if (e.networkResponse.statusCode == 404) {
                            Toast.makeText(PlayPvpScore.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                        } else if (e.networkResponse.statusCode == 401) {
                            Toast.makeText(PlayPvpScore.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                            finish();
                            Intent myIntent = new Intent(getBaseContext(), Login.class);
                            startActivity(myIntent);
                        } else if (e.networkResponse.statusCode == 403) {
                            Toast.makeText(PlayPvpScore.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                        } else if (e.networkResponse.statusCode == 500) {
                            Toast.makeText(PlayPvpScore.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
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

    private void fillSpinner() {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(PlayPvpScore.this);
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
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlayPvpScore.this, android.R.layout.simple_spinner_item, friendsArray);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        friendsSpinner.setAdapter(adapter);
                                    }
                                } else {
                                    Log.e("Poruka", response.getString("result"));
                                    Toast.makeText(PlayPvpScore.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Log.e("Poruka", "Error: " + e);
                                Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError e) {
                            if (e.networkResponse.statusCode == 404) {
                                Toast.makeText(PlayPvpScore.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                            } else if (e.networkResponse.statusCode == 401) {
                                Toast.makeText(PlayPvpScore.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                                finish();
                                Intent myIntent = new Intent(getBaseContext(), Login.class);
                                startActivity(myIntent);
                            } else if (e.networkResponse.statusCode == 403) {
                                Toast.makeText(PlayPvpScore.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                            } else if (e.networkResponse.statusCode == 500) {
                                Toast.makeText(PlayPvpScore.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PlayPvpScore.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
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

    private void emptyPlayerForm() {
        pvpPlayerNameEditText.setText("");
        pvpPlayerPointsEditText.setText("");
        friendsSpinner.setSelection(0);
        wonSwitch.setChecked(false);
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
