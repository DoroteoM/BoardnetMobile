package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

public class GameSearch extends AppCompatActivity {
    private SharedPreferences preferences;
    private String URL = "https://boardnetapi.000webhostapp.com/api";
    private String search;
    private ProgressDialog progress;
    private List<JSONObject> listOfGames = new ArrayList<JSONObject>();
    private boolean waitForResponse = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_search);
        search = getIntent().getStringExtra("search");
        setTitle("Search: " + search);
        preferences = getSharedPreferences("API", MODE_PRIVATE);

        try {
            this.getGamesList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        
    }

    private void getGamesList() throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(GameSearch.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/games/search/" + search,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                JSONArray gameList = response.getJSONArray("result");
                                for (int i = 0; i < gameList.length(); i++) {
                                    listOfGames.add(gameList.getJSONObject(i));
                                }
                                waitForResponse = false;
                            }
                        } catch (JSONException e) {
                            waitForResponse = false;
                            Log.e("Poruka", "GameSearch: " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka", "Request filed: " + error.toString());
                        Toast.makeText(GameSearch.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void createList(List<JSONObject> listOfGames) throws JSONException {
        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

        for (JSONObject game : listOfGames) {
            String bgg_game_id = (String) game.get("bgg_game_id");
            String name = (String) game.get("name");

            Map<String, Object> listItemMap = new HashMap<String, Object>();
            listItemMap.put("imageId", R.mipmap.ic_launcher);
            listItemMap.put("bgg_game_id", bgg_game_id);
            listItemMap.put("name", name);
            itemDataList.add(listItemMap);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(GameSearch.this, itemDataList, R.layout.activity_game_search,
                new String[]{"imageId", "bgg_game_id", "name"}, new int[]{R.id.gameImageView, R.id.bggGameIdTextView, R.id.gameNameTextView});

        ListView listView = (ListView) findViewById(R.id.gameListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String gameName = (String) clickItemMap.get("name");
                String bggGameId = (String) clickItemMap.get("bgg_game_id");

                Toast.makeText(GameSearch.this, "You select item is  " + gameName + " , " + bggGameId, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
