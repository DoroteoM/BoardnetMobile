package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class Friend extends AppCompatActivity {
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private TextView nameValueTextView, surnameValueTextView, usernameValueTextView, emailValueTextView, bggUsernameValueTextView, dateOfBirthValueTextView;
    private Button btnAddFriend;
    private String myUsername, friendUsername, name;
    private Boolean befriended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        setTitle("User");

        nameValueTextView = (TextView) findViewById(R.id.nameValueTextView);
        surnameValueTextView = (TextView) findViewById(R.id.surnameValueTextView);
        usernameValueTextView = (TextView) findViewById(R.id.usernameValueTextView);
        emailValueTextView = (TextView) findViewById(R.id.emailValueTextView);
        bggUsernameValueTextView = (TextView) findViewById(R.id.bggUsernameValueTextView);
        dateOfBirthValueTextView = (TextView) findViewById(R.id.dateOfBirthValueTextView);
        btnAddFriend = (Button) findViewById(R.id.btnAddFriend);
        myUsername = preferences.getString("username", "test");
        friendUsername = getIntent().getStringExtra("username");

        this.getUser();

        this.isFriend();

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageFriends();
            }
        });
    }

    private void isFriend() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL + "/friends/are-friends/user/" + myUsername + "/friend/" + friendUsername,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            befriended = response.getBoolean("result");
                                            if (befriended) btnAddFriend.setText("Remove friend");
                                        } else {
                                            Toast.makeText(Friend.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e.toString());
                                        Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    Log.e("Poruka", "Error: " + e.toString());
                                    Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    Log.e("Poruka", "Error: " + e.toString());
                }
            }
        }).start();
    }

    private void manageFriends() {
        progress = new ProgressDialog(Friend.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Loading list");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        if (befriended == false) addFriend();
        else removeFriend();
    }

    private void addFriend() {
        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", myUsername);
                    params.put("friend_username", friendUsername);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            URL + "/friends",
                            new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            Toast.makeText(Friend.this, "Friend added", Toast.LENGTH_LONG).show();
                                            befriended = true;
                                            btnAddFriend.setText("Remove friend");
                                        } else {
                                            Toast.makeText(Friend.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e.toString());
                                        Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    Log.e("Poruka", "Error: " + e.toString());
                                    Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    progress.dismiss();
                                }
                            });
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    progress.dismiss();
                    Log.e("Poruka", "Error: " + e.toString());
//                    Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    private void removeFriend() {
        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", myUsername);
                    params.put("friend_username", friendUsername);
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.DELETE,
                            URL + "/friends/user/" + myUsername + "/friend/" + friendUsername,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getBoolean("success")) {
                                            Toast.makeText(Friend.this, "Friend removed", Toast.LENGTH_LONG).show();
                                            befriended = false;
                                            btnAddFriend.setText("Add friend");
                                        } else {
                                            Toast.makeText(Friend.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", "Error: " + e.toString());
                                        Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    Log.e("Poruka", "Error: " + e.toString());
                                    Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    progress.dismiss();
                                }
                            });
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    progress.dismiss();
                    Log.e("Poruka", "Error: " + e.toString());
//                    Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    private void getUser() {
        RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/users/" + friendUsername,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //ako je success = true znaci da je registracija uspjela
                            if (response.getBoolean("success")) {
                                if (!response.getJSONObject("result").getString("name").equals("null"))
                                    nameValueTextView.setText(response.getJSONObject("result").getString("name"));
                                if (!response.getJSONObject("result").getString("surname").equals("null"))
                                    surnameValueTextView.setText(response.getJSONObject("result").getString("surname"));
                                if (!response.getJSONObject("result").getString("date_of_birth").equals("null"))
                                    dateOfBirthValueTextView.setText(response.getJSONObject("result").getString("date_of_birth").substring(0, 10));
                                if (!response.getJSONObject("result").getString("username").equals("null"))
                                    usernameValueTextView.setText(response.getJSONObject("result").getString("username"));
                                if (!response.getJSONObject("result").getString("email").equals("null"))
                                    emailValueTextView.setText(response.getJSONObject("result").getString("email"));
                                if (!response.getJSONObject("result").getString("bgg_username").equals("null"))
                                    bggUsernameValueTextView.setText(response.getJSONObject("result").getString("bgg_username"));
                                name = nameValueTextView.getText().toString() + ' ' + surnameValueTextView.getText().toString();
                                if (name.equals(" ")) setTitle(friendUsername);
                                else setTitle(name);
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", "Profile: failed reading");
                            Toast.makeText(Friend.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka", "Request filed: " + error.toString());
                        Toast.makeText(Friend.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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
