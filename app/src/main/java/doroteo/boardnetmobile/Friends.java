package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

import static doroteo.boardnetmobile.ErrorResponse.errorResponse;

public class Friends extends MainClass {
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private Button findByNameButton, findByUsernameButton;
    private EditText searchFriendEditText;
    private String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        myUsername = preferences.getString("username", "test");
        setTitle("Friends");

        this.getFriendList();

        this.search();
    }

    private void getFriendList() {
        progress = new ProgressDialog(Friends.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Friends.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/friends/user/" + myUsername,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            if (!response.get("result").equals(null)) {
                                                JSONArray friendsList = response.getJSONArray("result");
                                                List<JSONObject> listOfUsers = new ArrayList<JSONObject>();
                                                for (int i = 0; i < friendsList.length(); i++) {
                                                    listOfUsers.add(friendsList.getJSONObject(i).getJSONObject("friend"));
                                                }
                                                if (listOfUsers.size() > 0)
                                                    createList(listOfUsers);
                                            }
                                        } else {
                                            Log.e("Poruka", response.getString("result"));
                                            Toast.makeText(Friends.this, "Error: " + response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e);
                                        Toast.makeText(Friends.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    errorResponse(e, Friends.this);
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
            String username, name, surname;
            username = (String) user.get("username");
            name = !user.get("name").equals(null) ? user.get("name").toString() : "";
            surname = !user.get("surname").equals(null) ? user.get("surname").toString() : "";

            if ( !preferences.getString("username", "test").equals(username) ) {
                Map<String, Object> listItemMap = new HashMap<String, Object>();
                listItemMap.put("username", username);
                listItemMap.put("name", name + ' ' + surname);
                itemDataList.add(listItemMap);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(Friends.this, itemDataList, R.layout.layout_friends,
                new String[]{"username", "name"}, new int[]{R.id.friendUsernameTextView, R.id.friendNameTextView});

        ListView listView = (ListView) findViewById(R.id.friendListView);
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                HashMap clickItemMap = (HashMap) clickItemObj;
                String username = (String) clickItemMap.get("username");

                finish();
                Intent myIntent = new Intent(getBaseContext(), Friend.class);
                myIntent.putExtra("username", username);
                startActivity(myIntent);
            }
        });
    }

    private void search() {
        findByNameButton = (Button) findViewById(R.id.addPlayerButton);
        findByUsernameButton = (Button) findViewById(R.id.savePvpPlayButton);
        findByNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriendEditText = (EditText) findViewById(R.id.searchFriendEditText);
                if (searchFriendEditText.getText().toString().equals("")) {
                    Toast.makeText(Friends.this, "Some name expected", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                    Intent myIntent = new Intent(getBaseContext(), FriendSearch.class);
                    myIntent.putExtra("search", searchFriendEditText.getText().toString());
                    myIntent.putExtra("by", "name");
                    startActivity(myIntent);
                }
            }
        });
        findByUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriendEditText = (EditText) findViewById(R.id.searchFriendEditText);
                if (searchFriendEditText.getText().toString().equals("")) {
                    Toast.makeText(Friends.this, "Some name expected", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                    Intent myIntent = new Intent(getBaseContext(), FriendSearch.class);
                    myIntent.putExtra("search", searchFriendEditText.getText().toString());
                    myIntent.putExtra("by", "username");
                    startActivity(myIntent);
                }
            }
        });
    }
}
