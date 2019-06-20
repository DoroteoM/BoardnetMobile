package doroteo.boardnetmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.*;

//import com.loopj.android.http.*;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "doroteo.boardnetmobile.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main menu");

        this.mainListView();
    }

    // This method use SimpleAdapter to show data in ListView.
    private void mainListView()
    {
        String[] navigationTo = { "Profile", "Players", "Library", "Games"};
        //String[] descArr = { "Jerry", "Male", "43", "Singapore" };

        //ArrayList<Map<String,Object>> itemDataList = new ArrayList<Map<String,Object>>();;
        ArrayList<String> itemDataList = new ArrayList<String>();;

        int navigationLen = navigationTo.length;
        for(int i =0; i < navigationLen; i++) {
//            Map<String,Object> listItemMap = new HashMap<String,Object>();
//            listItemMap.put("title", titleArr[i]);
//            listItemMap.put("description", descArr[i]);
            itemDataList.add(navigationTo[i]);
        }

        ArrayAdapter<String> adapterNavigationList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemDataList);
//        SimpleAdapter simpleAdapter = new SimpleAdapter(this,itemDataList,android.R.layout.simple_list_item_2,
//                new String[]{"title","description"},new int[]{android.R.id.text1,android.R.id.text2});

        ListView listView = (ListView)findViewById(R.id.mainListView);
        listView.setAdapter(adapterNavigationList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                //Toast.makeText(MainActivity.this, "You clicked " + clickItemObj.toString(), Toast.LENGTH_SHORT).show();
                Log.e("Poruka", "Navigation: " + clickItemObj);
                if (clickItemObj.equals("Profile"))
                {
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    startActivity(intent);
                }
                else if (clickItemObj.equals("Games"))
                {
                    Intent intent = new Intent(MainActivity.this, Games.class);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, Test.class);
                    startActivity(intent);
                }
            }
        });

    }
}


//!   TEST
//    public void sendMessage(View view)
//    {
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText username = (EditText) findViewById(R.id.loginUsername);
//        String message = username.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//    }
//
//    public void testSmething(View view)
//    {
//        Intent intent = new Intent(this, Test.class);
//        startActivity(intent);
//    }

