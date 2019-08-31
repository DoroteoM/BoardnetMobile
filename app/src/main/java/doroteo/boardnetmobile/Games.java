package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Games extends MainClass {
    private SharedPreferences preferences;
    private ProgressDialog progress, progress2;
    private Button btnFirst, btnMinusTen, btnMinusOne, btnPlusOne, btnPlusTen, btnLast;
    private TextView pageTextView;
    private Integer currentPage, lastPage;
    private Button searchGamesButton;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        setTitle("Games");

        if (getIntent().getStringExtra("page") != null)
            currentPage = Integer.parseInt(getIntent().getStringExtra("page"));
        else
            currentPage = 1;
        btnFirst = (Button) findViewById(R.id.btnFirst);
        btnMinusTen = (Button) findViewById(R.id.btnMinusTen);
        btnMinusOne = (Button) findViewById(R.id.btnMinusOne);
        btnPlusOne = (Button) findViewById(R.id.btnPlusOne);
        btnPlusTen = (Button) findViewById(R.id.btnPlusTen);
        btnLast = (Button) findViewById(R.id.btnLast);
        pageTextView = (TextView) findViewById(R.id.pageTextView);
        pageTextView.setText(currentPage.toString());

        this.getGamesList();

        searchGamesButton = (Button) findViewById(R.id.searchButton);
        searchGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText = (EditText) findViewById(R.id.searchEditText);
                if (searchEditText.getText().toString().equals("")) {
                    Toast.makeText(Games.this, "Some game name expected", Toast.LENGTH_LONG).show();
                } else {
                    Intent myIntent = new Intent(getBaseContext(), GameSearch.class);
                    myIntent.putExtra("search", searchEditText.getText().toString());
                    myIntent.putExtra("type", "name");
                    startActivity(myIntent);
                }
            }
        });

        pagiantion();
    }

    private void getGamesList() {
        progress = new ProgressDialog(Games.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Games.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/games?page=" + currentPage,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            JSONArray gameList = response.getJSONObject("result").getJSONArray("data");
                                            lastPage = response.getJSONObject("result").getInt("last_page");
                                            if (currentPage == 1) btnFirst.setEnabled(false);
                                            if (currentPage-10 < 1) btnMinusTen.setEnabled(false);
                                            if (currentPage-1 < 1) btnMinusOne.setEnabled(false);
                                            if (currentPage+1 > lastPage) btnPlusOne.setEnabled(false);
                                            if (currentPage+10 > lastPage) btnPlusTen.setEnabled(false);
                                            if (currentPage == lastPage) btnLast.setEnabled(false);
                                            List<JSONObject> listOfGames = new ArrayList<JSONObject>();
                                            for (int i = 0; i < gameList.length(); i++) {
                                                listOfGames.add(gameList.getJSONObject(i));
                                            }
                                            new Games.CreateList().execute(listOfGames);
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(Games.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Game: " + e);
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    ErrorResponse.errorResponse(e, Games.this);
                                    if (e.networkResponse.statusCode == 401) {
                                        finish();
                                        Intent myIntent = new Intent(getBaseContext(), Login.class);
                                        startActivity(myIntent);
                                    }
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
                } catch (Exception e) {
                    e.printStackTrace();
                    progress.dismiss();
                }
            }
        }).start();
    }

    private void pagiantion() {
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(1);
            }
        });

        btnMinusTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(currentPage-10);
            }
        });

        btnMinusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(currentPage-1);
            }
        });

        btnPlusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(currentPage+1);
            }
        });

        btnPlusTen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(currentPage+10);
            }
        });

        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPage(lastPage);
            }
        });
    }

    private void goToPage(Integer page) {
        finish();
        Intent myIntent = new Intent(getBaseContext(), Games.class);
        myIntent.putExtra("page", page.toString());
        startActivity(myIntent);
    }

    private class CreateList extends AsyncTask<List<JSONObject>, Void, ArrayList<Map<String, Object>>> {
        @Override
        protected void onPreExecute() {
            progress2 = new ProgressDialog(Games.this);
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

            SimpleAdapter simpleAdapter = new SimpleAdapter(Games.this, itemDataList, R.layout.layout_games,
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
