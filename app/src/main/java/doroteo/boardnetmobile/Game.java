package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class Game extends AppCompatActivity {
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private String bgg_game_id;
    private TextView publishedValueTextView, playersValueTextView, timeValueTextView, ratingValueTextView, rankValueTextView;
    private Button manageLibraryButton, addPlayButton;
    private Boolean inLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setTitle("Game");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");
        manageLibraryButton = (Button) findViewById(R.id.manageLibraryButton);
        addPlayButton = (Button) findViewById(R.id.addPlayButton);

        this.getGame();
        manageLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLibrary();
            }
        });
        addPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getBaseContext(), PlayGameMode.class);
                myIntent.putExtra("bgg_game_id", bgg_game_id);
                startActivity(myIntent);
            }
        });
    }

    private void getGame() {
        publishedValueTextView = (TextView) findViewById(R.id.publishedValueTextView);
        playersValueTextView = (TextView) findViewById(R.id.playersValueTextView);
        timeValueTextView = (TextView) findViewById(R.id.timeValueTextView);
        ratingValueTextView = (TextView) findViewById(R.id.ratingValueTextView);
        rankValueTextView = (TextView) findViewById(R.id.rankValueTextView);

        RequestQueue requestQueue = Volley.newRequestQueue(Game.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/games/" + bgg_game_id + "/" + preferences.getString("username", "test"),
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
                                if (!response.getJSONObject("result").getString("inLibrary").equals("null"))
                                {
                                    inLibrary = response.getJSONObject("result").getBoolean("inLibrary");
                                    if (inLibrary == true)
                                        manageLibraryButton.setText("Remove from library");
                                    manageLibraryButton.setVisibility(View.VISIBLE);
                                }
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
    
    private void manageLibrary() {
        progress = new ProgressDialog(Game.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        if (inLibrary == false) addGameToLibrary();
        else removeGameFromLibrary();
    }

    private void addGameToLibrary() {
        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Game.this);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", preferences.getString("username", "test"));
                    params.put("bgg_game_id", bgg_game_id);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            URL + "/libraries",
                            new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            Toast.makeText(Game.this, "Game added to library", Toast.LENGTH_LONG).show();
                                            inLibrary = true;
                                            manageLibraryButton.setText("Remove from library");
                                        } else {
                                            Toast.makeText(Game.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e.toString());
                                        Toast.makeText(Game.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Poruka", "Error: " + error.toString());
                                    Toast.makeText(Game.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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

    private void removeGameFromLibrary() {
        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Game.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.DELETE,
                            URL + "/libraries/user/" + preferences.getString("username","test") + "/game/" + bgg_game_id,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            Toast.makeText(Game.this, "Game removed from library", Toast.LENGTH_LONG).show();
                                            inLibrary = false;
                                            manageLibraryButton.setText("Add to library");
                                        } else {
                                            Toast.makeText(Game.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e.toString());
                                        Toast.makeText(Game.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Poruka", "Error: " + error.toString());
                                    Toast.makeText(Game.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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
