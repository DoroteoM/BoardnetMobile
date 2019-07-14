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
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayGameMode extends AppCompatActivity {
    private SharedPreferences preferences;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private String myUsername, bgg_game_id, gameMode;
    private Integer playId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game_mode);
        setTitle("Pick game mode");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        myUsername = preferences.getString("username", "test");
        playId = 0;

        this.gameModeList();
    }

    private void gameModeList() {
        String[] navigationTo = { "Solo", "PvP"}; //, "Team", "Co-op", "Master"

        ArrayList<String> itemDataList = new ArrayList<String>();

        int navigationLen = navigationTo.length;
        for(int i =0; i < navigationLen; i++) {
            itemDataList.add(navigationTo[i]);
        }

        ArrayAdapter<String> adapterNavigationList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemDataList);

        ListView listView = (ListView)findViewById(R.id.gameModeListView);
        listView.setAdapter(adapterNavigationList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                if (clickItemObj.equals("Solo"))
                {
                    Intent myIntent = new Intent(getBaseContext(), PlaySoloScore.class);
                    myIntent.putExtra("bgg_game_id", bgg_game_id);
                    startActivity(myIntent);
                }
                else if (clickItemObj.equals("PvP"))
                {
                    createPlay();
                }
                else if (clickItemObj.equals("Team"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
                else if (clickItemObj.equals("Co-op"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
                else if (clickItemObj.equals("Master"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void createPlay() {
        gameMode = "PVP";
        RequestQueue requestQueue = Volley.newRequestQueue(PlayGameMode.this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("bgg_game_id", bgg_game_id);
        params.put("username", myUsername);
        params.put("mode", gameMode);
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
                                Intent myIntent = new Intent(getBaseContext(), PlayPvpScore.class);
                                myIntent.putExtra("bgg_game_id", bgg_game_id);
                                myIntent.putExtra("playId", playId.toString());
                                startActivity(myIntent);
                            } else {
                                Toast.makeText(PlayGameMode.this, "Play: " + response.getString("result"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", e.toString());
                            Toast.makeText(PlayGameMode.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.e("Poruka", "Error: " + e.toString());
                        Toast.makeText(PlayGameMode.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    //Back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }
}
