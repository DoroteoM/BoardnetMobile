package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPlays extends MainClass {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plays);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        myUsername = preferences.getString("username", "test");
        setTitle("My plays");

        getPlaysList();
    }

    private void getPlaysList() {
        progress = new ProgressDialog(MyPlays.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(MyPlays.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/play/user/" + myUsername,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            if (!response.get("result").equals(null)) {
                                                JSONArray playsList = response.getJSONArray("result");
                                                List<JSONObject> listOfPlays = new ArrayList<JSONObject>();
                                                for (int i = 0; i < playsList.length(); i++) {
                                                    listOfPlays.add(playsList.getJSONObject(i));
                                                }
                                                if (listOfPlays.size() > 0)
                                                    fillPlayListView(listOfPlays);
                                                progress.dismiss();
                                            }
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(MyPlays.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e);
                                        Toast.makeText(MyPlays.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Poruka", "Request filed: " + error.toString());
                                    Toast.makeText(MyPlays.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                                    progress.dismiss();
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

    private void fillPlayListView(List<JSONObject> listOfPlays) throws JSONException {
        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

        for (JSONObject play : listOfPlays) {
            String game, mode, time, title;
            game = play.getJSONObject("game").getString("name");
            mode = play.getString("mode");
            time = play.getString("created_at");
            title = game + " (" + mode + ") \n" + time;


            Map<String, Object> listItemMap = new HashMap<String, Object>();
            listItemMap.put("title", title);
            listItemMap.put("playId", play.getString("id"));
            listItemMap.put("mode", mode);
            itemDataList.add(listItemMap);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(MyPlays.this, itemDataList, R.layout.activity_my_plays,
                new String[]{"title", "playId", "mode"}, new int[]{R.id.myPlayTitleTextView, R.id.myPlayIdTextView, R.id.myPlayModeTextView});

        ListView listView = (ListView) findViewById(R.id.myPlaysListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String playId = (String) clickItemMap.get("playId");
                String mode = (String) clickItemMap.get("mode");
                Intent myIntent = null;
                if (mode.equals("SOLO"))
                    myIntent = new Intent(getBaseContext(), PlaySoloScore.class);
                else if (mode.equals("PVP"))
                    myIntent = new Intent(getBaseContext(), PlayPvpScore.class);
                myIntent.putExtra("playId", playId);
                startActivity(myIntent);
            }
        });
    }
}
