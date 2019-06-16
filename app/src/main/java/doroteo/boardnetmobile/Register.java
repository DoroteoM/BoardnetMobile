package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
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
    EditText emailBox, usernameBox, passwordBox, passwordConfirmationBox;
    Button registerButton;
    TextView loginLink;
    String URL ="https://boardnetapi.000webhostapp.com/api";
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailBox = (EditText)findViewById(R.id.emailBox);
        usernameBox = (EditText)findViewById(R.id.usernameBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);
        passwordConfirmationBox = (EditText)findViewById(R.id.passwordConfirmationBox);
        registerButton = (Button)findViewById(R.id.registerButton);
        loginLink = (TextView)findViewById(R.id.loginLink);

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(Register.this);
                progress.setTitle("Please Wait!");
                progress.setMessage("Attempting to Register");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                progress.setCancelable(false);

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
                                            Log.e("Poruka", "Success: " + response.toString());
                                            //Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                            try
                                            {
                                                //ako je success = true znaci da je registracija uspjela
                                                if (response.getString("success").equals("true"))
                                                {
                                                    Log.e("Poruka", "User: " + response.getString("user"));
                                                    Log.e("Poruka", "Username: " + response.getJSONObject("user").getString("username"));
                                                    progress.dismiss();
                                                    Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(Register.this, Login.class)); //TODO register:username -> login:username
                                                }
                                                else {
                                                    //ako je success = false znaci da je registracija nije uspjela, prolazi se kroz errors da se vidi u cemu je problem
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
                                            catch (JSONException e)
                                            {
                                                Log.e("Poruka", e.toString() );
                                                progress.dismiss();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("Poruka","Error: " + error.toString());
                                            progress.dismiss();
                                            Toast.makeText(Register.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }
}