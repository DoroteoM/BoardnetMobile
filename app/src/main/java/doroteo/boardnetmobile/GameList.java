package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class GameList extends AppCompatActivity {
    EditText bggUsernameBox;
    Button addLibraryGamesButton;
    String API ="https://boardnetapi.000webhostapp.com/api/";
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        setTitle("All games");

        this.gameListView();
    }

    private void gameListView()
    {
        bggUsernameBox = (EditText)findViewById(R.id.bggUsernameBox);
        addLibraryGamesButton = (Button)findViewById(R.id.addLibraryGamesButton);

        addLibraryGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(GameList.this);
                progress.setTitle("Please Wait!");
                progress.setMessage("Adding games");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                progress.setCancelable(false);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            RequestQueue requestQueue = Volley.newRequestQueue(GameList.this);
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.GET,
                                    API+"games/library/addgames/user/" +bggUsernameBox.getText().toString(),
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.e("Poruka", response.toString());
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("Poruka","Request filed: " + error.toString());
                                            progress.dismiss();
                                            Toast.makeText(GameList.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
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
}
