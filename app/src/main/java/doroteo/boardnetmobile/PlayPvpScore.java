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

public class PlayPvpScore extends AppCompatActivity {
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private Spinner friendsSpinner;
    private String myUsername, bgg_game_id, playId;
    private Switch wonSwitch;
    private EditText pvpPlayerNameEditText, pvpPlayerPointsEditText;
    private Button addPvpPlayerButton, savePvpPlayButton;
    private List<Player> players = new ArrayList<Player>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pvp_score);
        setTitle("Score");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        playId = getIntent().getStringExtra("playId");
        myUsername = preferences.getString("username", "test");
        pvpPlayerNameEditText = (EditText) findViewById(R.id.pvpPlayerNameEditText);
        pvpPlayerPointsEditText = (EditText) findViewById(R.id.pvpPlayerPointsEditText);
        friendsSpinner = (Spinner) findViewById(R.id.friendsSpinner);
        wonSwitch = (Switch) findViewById(R.id.wonSwitch);
        addPvpPlayerButton = (Button) findViewById(R.id.addPvpPlayerButton);
        savePvpPlayButton = (Button) findViewById(R.id.savePvpPlayButton);

        getPlayersList();
        addPvpPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getBaseContext(), PlayPvpNewPlayer.class);
                myIntent.putExtra("bgg_game_id", bgg_game_id);
                myIntent.putExtra("playId", playId);
                startActivity(myIntent);
            }
        });
    }

    private void addPlayer() {
        Player player = new Player();
        player.name = pvpPlayerNameEditText.getText().toString();
        player.username = friendsSpinner.getSelectedItem().toString();
        if (!pvpPlayerPointsEditText.getText().toString().equals(""))
            player.points = Integer.parseInt(pvpPlayerPointsEditText.getText().toString());
        player.won = wonSwitch.isChecked() ? "1" : "0";
        players.add(player);
        emptyPlayerForm();
    }

    private void getPlayersList() {
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
                            URL + "/player/play/" + playId,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            if (!response.get("result").equals(null)) {
                                                JSONArray playersList = response.getJSONArray("result");
                                                List<JSONObject> listOfPlayers = new ArrayList<JSONObject>();
                                                for (int i = 0; i < playersList.length(); i++) {
                                                    listOfPlayers.add(playersList.getJSONObject(i));
                                                }
                                                if (listOfPlayers.size() > 0)
                                                    createList(listOfPlayers);
                                            }
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
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Poruka", "Request filed: " + error.toString());
                                    Toast.makeText(PlayPvpScore.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                                    progress.dismiss();
                                }
                            });
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    progress.dismiss();
                }
            }
        }).start();
    }

    private void createList(List<JSONObject> listOfPlayers) throws JSONException {
        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

        for (JSONObject player : listOfPlayers) {
            String username, name, surname, playerId, playerName, points, won;
            playerName = player.get("name").toString();
            points = player.get("points").toString();
            playerId = player.get("id").toString();
            if (playerName.equals("") && !player.get("user").equals("")) {
                JSONObject user = player.getJSONObject("user");
                username = (String) user.get("username");
                name = !user.get("name").equals(null) ? user.get("name").toString() : "";
                surname = !user.get("surname").equals(null) ? user.get("surname").toString() : "";
                if (name.equals("") && surname.equals(""))
                    playerName = username;
                else
                    playerName = name + " " + surname;
            }

            Map<String, Object> listItemMap = new HashMap<String, Object>();
            listItemMap.put("playerId", playerId);
            listItemMap.put("playerName", playerName);
            if (!points.equals(""))
                listItemMap.put("points", points);
            itemDataList.add(listItemMap);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(PlayPvpScore.this, itemDataList, R.layout.activity_play_pvp_score,
                new String[]{"imageId", "playerName", "points", "playerId"}, new int[]{R.id.friendImageView, R.id.pvpPlayerNameListTextView, R.id.pvpPlayerPointsTextView, R.id.pvpPlayerIdTextView});

        ListView listView = (ListView) findViewById(R.id.playersListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String playerId = (String) clickItemMap.get("playerId");

                Intent myIntent = new Intent(getBaseContext(), PlaySoloScore.class);
                myIntent.putExtra("bgg_game_id", bgg_game_id);
                myIntent.putExtra("playerId", playerId);
                startActivity(myIntent);
            }
        });
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
