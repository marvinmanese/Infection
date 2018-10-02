package api.infection.infection;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This is the main menu activity, it is also the launcher activity.
 */
public class MainActivity extends Activity {

    // Linking activities taken from: http://www.mkyong.com/android/android-activity-from-one-screen-to-another-screen/
    Button button1, button2, button3, button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();

        ((GlobalVariables) this.getApplication()).load_settings();
    }

    /**
     * This waits for any button clicks.
     */
    public void addListenerOnButton() {

        final Context context = this;

        button1 = (Button) findViewById(R.id.playButton);
        button2 = (Button) findViewById(R.id.optionsButton);
        button3 = (Button) findViewById(R.id.hsButton);
        button4 = (Button) findViewById(R.id.exitButton);

        button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, CameraActivity.class);
                startActivity(intent);
            }

        });

        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, OptionsActivity.class);
                startActivity(intent);
            }

        });

        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, HighScoreActivity.class);
                startActivity(intent);
            }

        });

        button4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }

        });
    }
}