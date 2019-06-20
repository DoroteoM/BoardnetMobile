package doroteo.boardnetmobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GameAlphabet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_alphabet);

        Toast.makeText(GameAlphabet.this, "Intent: " + getIntent().getStringExtra("letter"), Toast.LENGTH_LONG).show();
        Log.e("Poruka", "Intent: " + getIntent().getStringExtra("letter"));
    }
}
