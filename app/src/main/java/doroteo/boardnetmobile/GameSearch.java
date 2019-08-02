package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSearch extends AppCompatActivity {
    private SharedPreferences preferences;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private ProgressDialog progress, progress2;
    private String type, search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_search);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        search = getIntent().getStringExtra("search");
        type = getIntent().getStringExtra("type");
        setTitle("Search: " + search);
        this.getGamesList();
    }

    private void getGamesList() {
        progress = new ProgressDialog(GameSearch.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(GameSearch.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/games/search/" + type + "/" + search,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            JSONArray gameList = response.getJSONArray("result");
                                            List<JSONObject> listOfGames = new ArrayList<JSONObject>();
                                            for (int i = 0; i < gameList.length(); i++) {
                                                listOfGames.add(gameList.getJSONObject(i));
                                            }
                                            new CreateList().execute(listOfGames);
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(GameSearch.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "GameSearch: " + e);
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    if (e.networkResponse.statusCode == 404) {
                                        Toast.makeText(GameSearch.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 401) {
                                        Toast.makeText(GameSearch.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent myIntent = new Intent(getBaseContext(), Login.class);
                                        startActivity(myIntent);
                                    } else if (e.networkResponse.statusCode == 403) {
                                        Toast.makeText(GameSearch.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 500) {
                                        Toast.makeText(GameSearch.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(GameSearch.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
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
                } catch (Exception e) {
                    e.printStackTrace();
                    progress.dismiss();
                }
            }
        }).start();
    }

    private class CreateList extends AsyncTask<List<JSONObject>, Void, ArrayList<Map<String, Object>>> {
        @Override
        protected void onPreExecute() {
            progress2 = new ProgressDialog(GameSearch.this);
            progress2.setTitle("Please Wait!");
            progress2.setMessage("Loading list");
            progress2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress2.show();
            progress2.setCancelable(false);
        }

        @Override
        protected ArrayList<Map<String, Object>> doInBackground(List<JSONObject>... listOfGames) {
            ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

            String name = null;
            String bgg_game_id = null;
            java.net.URL imageURL = null;
            Bitmap bmp = null;
            for (JSONObject game : listOfGames[0]) {
                try {
                    bgg_game_id = game.getString("bgg_game_id");
                    name = game.getString("name");
                    imageURL = new URL(game.getString("thumbnail"));
                    bmp = BitmapFactory.decodeStream(imageURL.openStream());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Map<String, Object> listItemMap = new HashMap<String, Object>();
//            listItemMap.put("imageId", R.mipmap.ic_launcher);
                listItemMap.put("image", bmp);
                listItemMap.put("bgg_game_id", bgg_game_id);
                listItemMap.put("name", name);
                itemDataList.add(listItemMap);
            }

            return itemDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, Object>> itemDataList) {

            SimpleAdapter simpleAdapter = new SimpleAdapter(GameSearch.this, itemDataList, R.layout.layout_games,
                    new String[]{"image", "name", "bgg_game_id"}, new int[]{R.id.gameImageView, R.id.gameNameTextView, R.id.bggIdTextView});

            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder(){
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                        ImageView iv = (ImageView) view;
                        Bitmap bm = (Bitmap) data;
                        iv.setImageBitmap(bm);
                        return true;
                    }
                    return false;
                }
            });

            ListView listView = (ListView) findViewById(R.id.gameListView);
            listView.setAdapter(simpleAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                    Object clickItemObj = adapterView.getAdapter().getItem(index);
                    HashMap clickItemMap = (HashMap) clickItemObj;
                    String bggGameId = (String) clickItemMap.get("bgg_game_id");

                    Intent myIntent = new Intent(getBaseContext(), Game.class);
                    myIntent.putExtra("bgg_game_id", bggGameId);
                    startActivity(myIntent);
                }
            });

            progress2.dismiss();
        }
    }
}
