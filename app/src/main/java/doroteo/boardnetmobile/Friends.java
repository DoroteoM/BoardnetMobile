package doroteo.boardnetmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Friends extends AppCompatActivity {
    Button findByNameButton, findByUsernameButton;
    EditText searchFriendEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setTitle("Friends");

        this.search();
    }

    private void search() {
        findByNameButton = (Button) findViewById(R.id.findByNameButton);
        findByUsernameButton = (Button) findViewById(R.id.findByUsernameButton);
        findByNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriendEditText = (EditText) findViewById(R.id.searchFriendEditText);
                if (searchFriendEditText.getText().toString().equals("")) {
                    Toast.makeText(Friends.this, "Some name expected", Toast.LENGTH_LONG).show();
                } else {
                    Intent myIntent = new Intent(getBaseContext(), FriendSearch.class);
                    myIntent.putExtra("search", searchFriendEditText.getText().toString());
                    myIntent.putExtra("by", "name");
                    startActivity(myIntent);
                }
            }
        });
        findByUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriendEditText = (EditText) findViewById(R.id.searchFriendEditText);
                if (searchFriendEditText.getText().toString().equals("")) {
                    Toast.makeText(Friends.this, "Some name expected", Toast.LENGTH_LONG).show();
                } else {
                    Intent myIntent = new Intent(getBaseContext(), FriendSearch.class);
                    myIntent.putExtra("search", searchFriendEditText.getText().toString());
                    myIntent.putExtra("by", "username");
                    startActivity(myIntent);
                }
            }
        });
    }
}
