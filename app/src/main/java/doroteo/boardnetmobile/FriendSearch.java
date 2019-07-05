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

public class FriendSearch extends AppCompatActivity {
    private String URL = "https://boardnetapi.000webhostapp.com/api";
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private String search, by;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        search = getIntent().getStringExtra("search");
        by = getIntent().getStringExtra("by");
        setTitle("Serch by " + by + ": " + search);

        this.getUsersList();
    }

    private void getUsersList() {
        progress = new ProgressDialog(FriendSearch.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(FriendSearch.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/users/search/" + by + "/" + search,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            if (!response.get("result").equals(null)) {
                                                JSONArray usersList = response.getJSONArray("result");
                                                List<JSONObject> listOfUsers = new ArrayList<JSONObject>();
                                                for (int i = 0; i < usersList.length(); i++) {
                                                    listOfUsers.add(usersList.getJSONObject(i));
                                                }
                                                if (listOfUsers.size() > 0)
                                                    createList(listOfUsers);
                                            } else {
                                                Toast.makeText(FriendSearch.this, "There is no users with this " + by, Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(FriendSearch.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e);
                                        Toast.makeText(FriendSearch.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Poruka", "Request filed: " + error.toString());
                                    Toast.makeText(FriendSearch.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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
        ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

        for (JSONObject user : listOfUsers) {
            String username, name, surname;
            username = (String) user.get("username");
            name = !user.get("name").equals(null) ? user.get("name").toString() : "";
            surname = !user.get("surname").equals(null) ? user.get("surname").toString() : "";

            if ( !preferences.getString("username", "test").equals(username) ) {
                Map<String, Object> listItemMap = new HashMap<String, Object>();
                listItemMap.put("imageId", R.mipmap.ic_launcher);
                listItemMap.put("username", username);
                listItemMap.put("name", name + ' ' + surname);
                itemDataList.add(listItemMap);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(FriendSearch.this, itemDataList, R.layout.activity_friend_search,
                new String[]{"imageId", "username", "name"}, new int[]{R.id.friendImageView, R.id.friendSearchUsernameTextView, R.id.friendSearchNameTextView});

        ListView listView = (ListView) findViewById(R.id.friendListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String username = (String) clickItemMap.get("username");

                Intent myIntent = new Intent(getBaseContext(), Friend.class);
                myIntent.putExtra("username", username);
                startActivity(myIntent);
            }
        });
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
