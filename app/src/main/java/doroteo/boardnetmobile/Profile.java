package doroteo.boardnetmobile;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    private SharedPreferences preferences;
    private String URL = "http://boardnetapi.hostingerapp.com/api";
    private EditText nameEditText, surnameEditText, usernameEditText, emailEditText, bggUsernameEditText, dateOfBirthEditText;
    private DatePicker dateOfBirthDatePicker;
    private Button btnSave, btnAddTeammate, btnDatePicker;
    private int user_id, mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        setTitle("Profile");

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        surnameEditText = (EditText) findViewById(R.id.surnameEditText);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        bggUsernameEditText = (EditText) findViewById(R.id.bggUsernameEditText);
        dateOfBirthEditText = (EditText) findViewById(R.id.dateOfBirthEditText);
        btnDatePicker = (Button) findViewById(R.id.dateOfBirthButton);
        btnSave = (Button) findViewById(R.id.btnSave);

        this.getUser();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void getUser() {
        RequestQueue requestQueue = Volley.newRequestQueue(Profile.this);
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
                                if (!response.getJSONObject("result").getString("name").equals("null"))
                                    nameEditText.setText(response.getJSONObject("result").getString("name"));
                                if (!response.getJSONObject("result").getString("surname").equals("null"))
                                    surnameEditText.setText(response.getJSONObject("result").getString("surname"));
                                if (!response.getJSONObject("result").getString("date_of_birth").equals("null"))
                                    dateOfBirthEditText.setText(response.getJSONObject("result").getString("date_of_birth").substring(0, 10));
                                if (!response.getJSONObject("result").getString("username").equals("null"))
                                    usernameEditText.setText(response.getJSONObject("result").getString("username"));
                                if (!response.getJSONObject("result").getString("email").equals("null"))
                                    emailEditText.setText(response.getJSONObject("result").getString("email"));
                                if (!response.getJSONObject("result").getString("bgg_username").equals("null"))
                                    bggUsernameEditText.setText(response.getJSONObject("result").getString("bgg_username"));
                                user_id = response.getJSONObject("result").getInt("id");
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
                        Toast.makeText(Profile.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Calendar c = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    c.setTime(format.parse(dateOfBirthEditText.getText().toString()));
                    DecimalFormat mFormat= new DecimalFormat("00");
                    mFormat.setRoundingMode(RoundingMode.DOWN);
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(Profile.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int date) {
                                    dateOfBirthEditText.setText(year + "-" + (month < 9 ? "0" : "") + (month + 1) + "-" + (date < 10 ? "0" : "")  + date);
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void save() {
        RequestQueue requestQueue = Volley.newRequestQueue(Profile.this);
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", usernameEditText.getText().toString());
        params.put("email", emailEditText.getText().toString());
        params.put("name", nameEditText.getText().toString());
        params.put("surname", surnameEditText.getText().toString());
        params.put("date_of_birth", dateOfBirthEditText.getText().toString());
        params.put("bgg_username", bggUsernameEditText.getText().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                URL + "/users/" + user_id,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response){
                        try {
                            if (response.getBoolean("success")) {
                                preferences.edit().putString("username", response.getJSONObject("result").getString("username")).apply();
                                Toast.makeText(Profile.this, "Save successful", Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    Toast.makeText(Profile.this, response.getJSONObject("result").toString().replaceAll("[\\[\\]{}\"]", ""), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    Log.e("Poruka", e.toString());
                                    Toast.makeText(Profile.this, e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(Profile.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Poruka", "Error: " + error.toString());
                        Toast.makeText(Profile.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
        };
        requestQueue.add(jsonObjectRequest);
    }

}
