package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

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

public class Games extends AppCompatActivity {
    private SharedPreferences preferences;
    EditText bggUsernameBox, searchEditText;
    Button addLibraryGamesButton, searchGamesButton, alphabetButton;
    String URL = "https://boardnetapi.000webhostapp.com/api";
    ProgressDialog progress;
    static final int PICK_CONTACT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        setTitle("All games");

        this.alphabetSearch();
        this.searchGame();
        this.addBggLibrary();
    }

    private void alphabetSearch() {
        final Spinner spinner = findViewById(R.id.alphabetSpinner);
        String[] items = new String['Z' - 'A' + 1 + '9' - '0' + 1];
        for (char letter = 'A'; letter <= 'Z'; ++letter) {
            items[letter - 'A'] = String.valueOf(letter);
        }
        for (char letter = '0'; letter <= '9'; ++letter) {
            items[letter - '0' + 'Z' - 'A' + 1] = String.valueOf(letter);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);

        alphabetButton = (Button) findViewById(R.id.alphabetButton);
        alphabetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getBaseContext(), GameSearch.class);
                myIntent.putExtra("search", spinner.getSelectedItem().toString());
                myIntent.putExtra("type", "letter");
                startActivity(myIntent);
            }
        });
    }

    private void searchGame() {

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
                    myIntent.putExtra("type", "search");
                    startActivity(myIntent);
                }
            }
        });
    }

    private void addBggLibrary() {
        this.fillBggUsername();

        bggUsernameBox = (EditText) findViewById(R.id.bggUsernameBox);
        addLibraryGamesButton = (Button) findViewById(R.id.addLibraryGamesButton);

        addLibraryGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(Games.this);
                progress.setTitle("Please Wait!");
                progress.setMessage("Adding games");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                progress.setCancelable(false);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("bgg_username", bggUsernameBox.getText().toString());

                            RequestQueue requestQueue = Volley.newRequestQueue(Games.this);
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    URL + "/games/bgg",
                                    new JSONObject(params),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getBoolean("success")) {
                                                    Toast.makeText(Games.this, "Added " + response.getJSONObject("result").getString("added") + " games.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    try {
                                                        Toast.makeText(Games.this, response.getString("result"), Toast.LENGTH_LONG).show();
                                                    } catch (JSONException e) {
                                                        Log.e("Poruka", e.toString());
                                                        Toast.makeText(Games.this, e.toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                Log.e("Poruka", "User: failed reading");
                                            }
                                            progress.dismiss();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("Poruka", "Request filed: " + error.toString());
                                            Toast.makeText(Games.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            requestQueue.add(jsonObjectRequest);
                        } catch (Exception e) {
                            progress.dismiss();
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    private void fillBggUsername() {
        RequestQueue requestQueue = Volley.newRequestQueue(Games.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/users/" + preferences.getString("username", "test"),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //ako je success = true znaci da je registracija uspjela
                            if (response.getBoolean("success")) {
                                if (!response.getJSONObject("result").getString("bgg_username").equals("null"))
                                    bggUsernameBox.setText(response.getJSONObject("result").getString("bgg_username"));
                            }
                        } catch (JSONException e) {
                            Log.e("Poruka", "Profile: failed reading");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka", "Request filed: " + error.toString());
                        Toast.makeText(Games.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }
}
