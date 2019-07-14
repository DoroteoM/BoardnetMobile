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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pvp_score);
        setTitle("Score");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        myUsername = preferences.getString("username", "test");
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        playId = getIntent().getStringExtra("playId");

        this.getFriendList();
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

    private void createList(List<JSONObject> listOfUsers) throws JSONException {
//        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();
//
//        for (JSONObject user : listOfUsers) {
//            String playerId, name, won, points, score;
//            playerId = user.getString("id");
//            name = !user.getString("name").equals("null") ? user.getString("name"): "";
//            points = !user.getString("points").equals("null") ? user.getString("points") + "   " : "";
//            won = user.getString("won").equals("1") ? "WINNER" : "";
//            score = points + won;
//
//            Map<String, Object> listItemMap = new HashMap<String, Object>();
//            listItemMap.put("playerId", playerId);
//            listItemMap.put("name", name);
//            listItemMap.put("score", score);
//            itemDataList.add(listItemMap);
//        }
//
//        SimpleAdapter simpleAdapter = new SimpleAdapter(PlayPvpScore.this, itemDataList, R.layout.activity_friend_search,
//                new String[]{"name", "score", "playId"}, new int[]{R.id.friendSearchNameTextView, R.id.friendSearchUsernameTextView, R.id.friendIdTextView});
//
//        ListView listView = (ListView) findViewById(R.id.friendListView);
//        listView.setAdapter(simpleAdapter);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
//                Object clickItemObj = adapterView.getAdapter().getItem(index);
//                HashMap clickItemMap = (HashMap) clickItemObj;
//                String username = (String) clickItemMap.get("username");
//
//                Intent myIntent = new Intent(getBaseContext(), Friend.class);
//                myIntent.putExtra("username", username);
//                startActivity(myIntent);
//            }
//        });
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
