package doroteo.boardnetmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

public class UserDetails extends AppCompatActivity {
    private SharedPreferences preferences;
    private EditText nameEditText, surnameEditText, dateOfBirthEditText, usernameEditText, emailEditText, bggUsernameEditText;
    private Button btnSave, btnAddTeammate;
    private String URL ="https://boardnetapi.000webhostapp.com/api/user/details/";
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        preferences = getSharedPreferences("API", MODE_PRIVATE);

        nameEditText = (EditText)findViewById(R.id.nameEditText);
        surnameEditText = (EditText)findViewById(R.id.surnameEditText);
        dateOfBirthEditText = (EditText)findViewById(R.id.dateOfBirthEditText);
        usernameEditText = (EditText)findViewById(R.id.usernameEditText);
        emailEditText = (EditText)findViewById(R.id.emailEditText);
        bggUsernameEditText = (EditText)findViewById(R.id.bggUsernameEditText);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnAddTeammate = (Button)findViewById(R.id.btnAddTeammate);

        RequestQueue requestQueue = Volley.newRequestQueue(UserDetails.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL + preferences.getString("username", "test"),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            //ako je success = true znaci da je registracija uspjela
                            if (response.getBoolean("success"))
                            {
                                if (!response.getJSONObject("result").getString("name").equals("null"))
                                    nameEditText.setText(response.getJSONObject("result").getString("name"));
                                if (!response.getJSONObject("result").getString("surname").equals("null"))
                                    surnameEditText.setText(response.getJSONObject("result").getString("surname"));
                                if (!response.getJSONObject("result").getString("date_of_birth").equals("null"))
                                    dateOfBirthEditText.setText(response.getJSONObject("result").getString("date_of_birth"));
                                if (!response.getJSONObject("result").getString("username").equals("null"))
                                    usernameEditText.setText(response.getJSONObject("result").getString("username"));
                                if (!response.getJSONObject("result").getString("email").equals("null"))
                                    emailEditText.setText(response.getJSONObject("result").getString("email"));
                                if (!response.getJSONObject("result").getString("bgg_username").equals("null"))
                                    bggUsernameEditText.setText(response.getJSONObject("result").getString("bgg_username"));
                                user_id = response.getJSONObject("result").getInt("id");
                            }
                        }
                        catch (JSONException e)
                        {
                            Log.e("Poruka", "UserDetails: failed reading" );
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka","Request filed: " + error.toString());
                        Toast.makeText(UserDetails.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue requestQueue = Volley.newRequestQueue(UserDetails.this);
                final Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=utf-8");
                params.put("name", nameEditText.getText().toString());
                params.put("surname", surnameEditText.getText().toString());
                params.put("date_of_birth", dateOfBirthEditText.getText().toString());
                params.put("username", usernameEditText.getText().toString());
                params.put("email", emailEditText.getText().toString());
                params.put("bgg_username", bggUsernameEditText.getText().toString());

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.PUT,
                        URL + user_id,
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try
                                {
                                    //ako je success = true znaci da je registracija uspjela
                                    if (response.getBoolean("success"))
                                    {
                                        preferences.edit().putString("username", response.getJSONObject("result").getString("username")).apply();
                                        Toast.makeText(UserDetails.this, "Save succesfsull", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        //ako je response = false znaci da je registracija nije uspjela, prolazi se kroz errors da se vidi u cemu je problem
                                        try {
                                            if (!response.getJSONObject("result").getString("username").equals("")){
                                                String a =  response.getJSONObject("result").getString("username");
                                            }
                                        }
                                        catch (JSONException e)
                                        {
                                            Log.e("Poruka", e.toString());
                                            Toast.makeText(UserDetails.this, e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                                catch (JSONException e)
                                {
                                    Log.e("Poruka", "User: failed reading" );
                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Poruka","Error: " + error.toString());
                                Toast.makeText(UserDetails.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
                    }

//                    @Override
//                    public String getBodyContentType() {
//                        return "application/json";
//                    }

//                    @Override
//                    protected Map<String, String> getParams()
//                    {
//                        final Map<String, String> params = new HashMap<>();
//                        params.put("name", nameEditText.getText().toString());
//                        params.put("surname", surnameEditText.getText().toString());
//                        params.put("date_of_birth", dateOfBirthEditText.getText().toString());
//                        params.put("username", usernameEditText.getText().toString());
//                        params.put("email", emailEditText.getText().toString());
//                        params.put("bgg_username", bggUsernameEditText.getText().toString());
//                        params.put("Content-Type", "application/json; charset=utf-8");
//
//                        return params;
//                    }
                };
                requestQueue.add(jsonObjectRequest);
            }
        });
    }
}
