package doroteo.boardnetmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.security.NetworkSecurityPolicy;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.*;

import org.json.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.*;

public class Test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        final SharedPreferences preferences = getSharedPreferences("API", MODE_PRIVATE);

        TextView testView = findViewById(R.id.testView);
        testView.setText(preferences.getString("token", "Token fail!"));

//
//// Instantiate the RequestQueue.
//        String $temp = "teo";
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        String URL ="https://boardnetapi.000webhostapp.com/api/test/hello1";
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("test", "doroteo");
//// Request a string response from the provided URL.
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.POST,
//                URL,
//                new JSONObject(params),
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        JSONObject temp =  response;
//                        Log.e("Poruka", "Success: " + response.toString());
//                        TextView testView = findViewById(R.id.testView);
//                        try {
//                            testView.setText(response.getString("result"));
//                        } catch (JSONException e) {
//                            testView.setText("Cathced");
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("Poruka","Fail: " + error.toString());
//                    }
//                });
//        requestQueue.add(jsonObjectRequest);

    }
}
