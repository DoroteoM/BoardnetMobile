package doroteo.boardnetmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlayGameMode extends AppCompatActivity {
    private SharedPreferences preferences;
    private ProgressDialog progress;
    private String bgg_game_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game_mode);
        setTitle("Pick game mode");
        preferences = getSharedPreferences("API", MODE_PRIVATE);
        bgg_game_id = getIntent().getStringExtra("bgg_game_id");

        this.gameModeList();
    }

    private void gameModeList() {
        String[] navigationTo = { "Solo", "PvP", "Team", "Co-op", "Master"};

        ArrayList<String> itemDataList = new ArrayList<String>();

        int navigationLen = navigationTo.length;
        for(int i =0; i < navigationLen; i++) {
            itemDataList.add(navigationTo[i]);
        }

        ArrayAdapter<String> adapterNavigationList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemDataList);

        ListView listView = (ListView)findViewById(R.id.gameModeListView);
        listView.setAdapter(adapterNavigationList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
//                Intent myIntent = new Intent(getBaseContext(), PlayGameMode.class);
//                myIntent.putExtra("bgg_game_id", bgg_game_id);
//                startActivity(myIntent);
                if (clickItemObj.equals("Solo"))
                {
                    Intent myIntent = new Intent(getBaseContext(), PlaySoloScore.class);
                    myIntent.putExtra("bgg_game_id", bgg_game_id);
                    startActivity(myIntent);
                }
                else if (clickItemObj.equals("PvP"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
                else if (clickItemObj.equals("Team"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
                else if (clickItemObj.equals("Co-op"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
                else if (clickItemObj.equals("Master"))
                {
                    Toast.makeText(PlayGameMode.this, "Error: " + clickItemObj.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
