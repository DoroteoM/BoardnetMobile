package doroteo.boardnetmobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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

public class Friend extends AppCompatActivity {
    private String URL = "https://boardnetapi.000webhostapp.com/api";
    private TextView nameValueTextView, surnameValueTextView, usernameValueTextView, emailValueTextView, bggUsernameValueTextView, dateOfBirthValueTextView;
    private Button btnAddFriend;
    private String username, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        setTitle("User");

        nameValueTextView = (TextView) findViewById(R.id.nameValueTextView);
        surnameValueTextView = (TextView) findViewById(R.id.surnameValueTextView);
        usernameValueTextView = (TextView) findViewById(R.id.usernameValueTextView);
        emailValueTextView = (TextView) findViewById(R.id.emailValueTextView);
        bggUsernameValueTextView = (TextView) findViewById(R.id.bggUsernameValueTextView);
        dateOfBirthValueTextView = (TextView) findViewById(R.id.dateOfBirthValueTextView);
        btnAddFriend = (Button) findViewById(R.id.btnAddFriend);

        username = getIntent().getStringExtra("username");
        this.getUser();
    }

    private void getUser() {
        RequestQueue requestQueue = Volley.newRequestQueue(Friend.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + "/users/" + username,
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
                                if (name.equals(" ")) setTitle(username);
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
