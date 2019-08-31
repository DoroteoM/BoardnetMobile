package doroteo.boardnetmobile;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.*;

//import com.loopj.android.http.*;

public class MainActivity extends MainClass {
    public static final String EXTRA_MESSAGE = "doroteo.boardnetmobile.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Boardnet");

        this.mainListView();
    }

    private void mainListView() {
        String[] navigationTo = {"Profile", "Friends", "Library", "My Plays", "All Games", "Log Out"};

        ArrayList<String> itemDataList = new ArrayList<String>();

        int navigationLen = navigationTo.length;
        for (int i = 0; i < navigationLen; i++) {
            itemDataList.add(navigationTo[i]);
        }

        ArrayAdapter<String> adapterNavigationList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemDataList);

        ListView listView = (ListView) findViewById(R.id.mainListView);
        listView.setAdapter(adapterNavigationList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                Log.e("Poruka", "Navigation: " + clickItemObj);
                if (clickItemObj.equals("Profile")) {
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    startActivity(intent);
                } else if (clickItemObj.equals("Friends")) {
                    Intent intent = new Intent(MainActivity.this, Friends.class);
                    startActivity(intent);
                } else if (clickItemObj.equals("Library")) {
                    Intent intent = new Intent(MainActivity.this, Library.class);
                    startActivity(intent);
                } else if (clickItemObj.equals("My Plays")) {
                    Intent intent = new Intent(MainActivity.this, MyPlays.class);
                    startActivity(intent);
                } else if (clickItemObj.equals("All Games")) {
                    Intent intent = new Intent(MainActivity.this, Games.class);
                    startActivity(intent);
                } else if (clickItemObj.equals("Log Out")) {
                    finish();
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    intent.putExtra("loggedOut", "true");
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

