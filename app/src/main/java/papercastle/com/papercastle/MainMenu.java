package papercastle.com.papercastle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void startGame(final View view) {
        final Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.LEVEL_MESSAGE, 0);
        startActivity(intent);
    }
}
