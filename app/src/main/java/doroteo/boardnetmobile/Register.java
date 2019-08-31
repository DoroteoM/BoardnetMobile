package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import static doroteo.boardnetmobile.ErrorResponse.errorResponse;

public class Register extends MainClass {
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    EditText emailBox, usernameBox, passwordBox, passwordConfirmationBox;
    Button registerButton;
    TextView loginLink;
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
                                        onSuccessDo(response);
                                    } catch (JSONException e) {
                                        Toast.makeText(Register.this, e.toString(), Toast.LENGTH_LONG).show();
                                        progress.dismiss();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError e) {
                                    errorResponse(e, Register.this);
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

    private void onSuccessDo(JSONObject response) throws JSONException {
        if (response.getString("success").equals("true")) {
            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
            Intent myIntent = new Intent(getBaseContext(), Login.class);
            myIntent.putExtra("username", response.getJSONObject("user").getString("username"));
            startActivity(myIntent); //TODO register:username -> login:username
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