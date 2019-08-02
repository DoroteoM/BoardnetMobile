package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    EditText emailBox, usernameBox, passwordBox, passwordConfirmationBox;
    Button registerButton;
    TextView loginLink;
    String URL = "http://boardnetapi.hostingerapp.com/api";
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailBox = (EditText) findViewById(R.id.emailBox);
        usernameBox = (EditText) findViewById(R.id.usernameBox);
        passwordBox = (EditText) findViewById(R.id.passwordBox);
        passwordConfirmationBox = (EditText) findViewById(R.id.passwordConfirmationBox);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginLink = (TextView) findViewById(R.id.loginLink);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    private void register() {
        progress = new ProgressDialog(Register.this);
        progress.setTitle("Please Wait!");
        progress.setMessage("Attempting to Register");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
        progress.setCancelable(false);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        new Thread(new Runnable() {
            public void run() {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(Register.this);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email", emailBox.getText().toString());
                    params.put("username", usernameBox.getText().toString());
                    params.put("password", passwordBox.getText().toString());
                    params.put("password_confirmation", passwordConfirmationBox.getText().toString());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            URL + "/auth/register",
                            new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getString("success").equals("true")) {
                                            loginPrefsEditor.putBoolean("saveLogin", true);
                                            loginPrefsEditor.putString("username", usernameBox.getText().toString()).apply();
                                            loginPrefsEditor.putString("password", passwordBox.getText().toString()).apply();
                                            progress.dismiss();
                                            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Register.this, Login.class)); //TODO register:username -> login:username
                                        } else {
                                            String errors = "";
                                            try {
                                                errors += response.getJSONObject("errors")
                                                        .getString("email")
                                                        .replace("\"", "")
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace(",", "\n");
                                            } catch (JSONException ignored) {
                                            }

                                            try {
                                                if (!errors.equals("")) errors += "\n";
                                                errors += response.getJSONObject("errors")
                                                        .getString("username")
                                                        .replace("\"", "")
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace(",", "\n");
                                            } catch (JSONException ignored) {
                                            }
                                            try {
                                                if (!errors.equals("")) errors += "\n";
                                                errors += response.getJSONObject("errors")
                                                        .getString("password")
                                                        .replace("\"", "")
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace(",", "\n");
                                            } catch (JSONException ignored) {
                                            }
                                            progress.dismiss();
                                            Toast.makeText(Register.this, errors, Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("Poruka", e.toString());
                                        progress.dismiss();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    if (e.networkResponse.statusCode == 404) {
                                        Toast.makeText(Register.this, "Error 404: Requested resource not found", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 401) {
                                        Toast.makeText(Register.this, "Error 401: The request has not been applied because it lacks valid authentication credentials for the target resource.", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent myIntent = new Intent(getBaseContext(), Login.class);
                                        startActivity(myIntent);
                                    } else if (e.networkResponse.statusCode == 403) {
                                        Toast.makeText(Register.this, "Error 403: The server understood the request but refuses to authorize it.", Toast.LENGTH_LONG).show();
                                    } else if (e.networkResponse.statusCode == 500) {
                                        Toast.makeText(Register.this, "Error 500: Something went wrong at server end", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(Register.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    progress.dismiss();
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
}