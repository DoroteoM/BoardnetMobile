package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
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

import static doroteo.boardnetmobile.ErrorResponse.errorResponse;

public class Login extends MainClass {
    private SharedPreferences preferences;
    private EditText usernameBox, passwordBox;
    private Button loginButton;
    private TextView registerLink;
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

        usernameBox = (EditText) findViewById(R.id.usernameBox);
        passwordBox = (EditText) findViewById(R.id.passwordBox);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerLink = (TextView) findViewById(R.id.registerLink);
        saveLoginCheckBox = (CheckBox) findViewById(R.id.saveLoginCheckBox);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            usernameBox.setText(loginPreferences.getString("username", ""));
            passwordBox.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
            if (getIntent().getStringExtra("loggedOut") == null)
                login();
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
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
                                    onSuccessDo(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    errorResponse(e, Login.this);
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

    private void onSuccessDo(JSONObject response) {
        try {
            if (response.getString("response").equals("success")) {
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
                finish();
                startActivity(new Intent(Login.this, MainActivity.class));
            } else {
                try {
                    if (response.getString("message").equals("invalid_credentials")) {
                        Log.e("Poruka", "Wrong username or password.");
                        Toast.makeText(Login.this, "Wrong username or password.", Toast.LENGTH_LONG).show();
                    } else if (response.getString("message").equals("failed_to_create_token")) {
                        Log.e("Poruka", "Failed to create token.");
                        Toast.makeText(Login.this, "Failed to create token.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("Poruka", e.toString());
                    Toast.makeText(Login.this, e.toString(), Toast.LENGTH_LONG).show();
                }
                progress.dismiss();
            }
        } catch (JSONException e) {
            Log.e("Poruka", "User: failed reading");
            Toast.makeText(Login.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}