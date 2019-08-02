package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.security.NetworkSecurityPolicy;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class Login extends AppCompatActivity {
    private SharedPreferences preferences;
    private EditText usernameBox, passwordBox;
    Button loginButton;
    TextView registerLink;
    ProgressBar loading;
    private String URL ="http://boardnetapi.hostingerapp.com/api";
    private ProgressDialog progress;
    private CheckBox saveLoginCheckBox;
    private Boolean saveLogin;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences("API", MODE_PRIVATE);

        usernameBox = (EditText)findViewById(R.id.usernameBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);
        loginButton = (Button)findViewById(R.id.loginButton);
        registerLink = (TextView)findViewById(R.id.registerLink);
        loading = (ProgressBar)findViewById(R.id.LoadingBar);
        saveLoginCheckBox = (CheckBox)findViewById(R.id.saveLoginCheckBox);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            usernameBox.setText(loginPreferences.getString("username", ""));
            passwordBox.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }


        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                login();
            }
        });


        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

    private void login() {
        progress = new ProgressDialog(Login.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Attempting to Login");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);

        //Thread je potreban kako bi se prikazivao loading screen
        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", usernameBox.getText().toString());
                    params.put("password", passwordBox.getText().toString());
                    loginPrefsEditor = loginPreferences.edit();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        URL + "/auth/login",
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                try
                            {
                                //ako je success = true znaci da je registracija uspjela
                                if (response.getString("response").equals("success"))
                                {
                                    Log.e("Poruka", "Token: " + response.getJSONObject("result").getString("token"));
                                    preferences.edit().putString("token", response.getJSONObject("result").getString("token")).apply();
                                    preferences.edit().putString("username", usernameBox.getText().toString()).apply();

                                    if (saveLoginCheckBox.isChecked()) {
                                        loginPrefsEditor.putBoolean("saveLogin", true);
                                        loginPrefsEditor.putString("username", usernameBox.getText().toString()).apply();
                                        loginPrefsEditor.putString("password", passwordBox.getText().toString()).apply();
                                    } else {
                                        loginPrefsEditor.clear();
                                        loginPrefsEditor.commit();
                                    }

                                    progress.dismiss();
                                    //Toast.makeText(Login.this, "Login successful", Toast.LENGTH_LONG).show();
                                    finish();
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                }
                                else {
                                    //ako je response = error znaci da je registracija nije uspjela, prolazi se kroz errors da se vidi u cemu je problem
                                    try {
                                        if (response.getString("message").equals("invalid_credentials"))
                                        {
                                            Log.e("Poruka", "Wrong username or password.");
                                            Toast.makeText(Login.this, "Wrong username or password.", Toast.LENGTH_LONG).show();
                                        }
                                        else if (response.getString("message").equals("failed_to_create_token"))
                                        {
                                            Log.e("Poruka", "Failed to create token.");
                                            Toast.makeText(Login.this, "Failed to create token.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    catch (JSONException e)
                                    {
                                        Log.e("Poruka", e.toString());
                                        Toast.makeText(Login.this, e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
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
                            public void onErrorResponse(VolleyError e) {
                                if (e.networkResponse.statusCode == 404) {
                                    Toast.makeText(Login.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                                } else if (e.networkResponse.statusCode == 401) {
                                    Toast.makeText(Login.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                                    finish();
                                    Intent myIntent = new Intent(getBaseContext(), Login.class);
                                    startActivity(myIntent);
                                } else if (e.networkResponse.statusCode == 403) {
                                    Toast.makeText(Login.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                                } else if (e.networkResponse.statusCode == 500) {
                                    Toast.makeText(Login.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Login.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                                progress.dismiss();
                            }
                        }
                    );
                    requestQueue.add(jsonObjectRequest);
                } catch (Exception e) {
                    progress.dismiss();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}