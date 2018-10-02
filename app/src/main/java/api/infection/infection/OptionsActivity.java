package api.infection.infection;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.File;

/**
 * This is our options activity. It only has 2 functions.
 * One for changing crosshair size, and one for resetting highscores/settings to default.
 */
public class OptionsActivity extends Activity{
    private SeekBar seekbar;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        seekbar = (SeekBar) findViewById(R.id.crosshair_bar);
        seekbar.setProgress(((GlobalVariables) this.getApplication()).crosshair_size);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                get_settings(progress - 1);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button reset = (Button) findViewById(R.id.button1);
        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new AlertDialog.Builder(context).setTitle("Reset settings").setMessage("Are you sure you want to reset settings and highscore?").setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reset();
                            }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ((GlobalVariables) this.getApplication()).save_settings();
    }

    private void reset() {
        File file = new File(context.getFilesDir(), "settings.txt");
        file.delete();
        ((GlobalVariables) this.getApplication()).init();
    }

    private void get_settings(int progress) {
        ((GlobalVariables) this.getApplication()).crosshair_size = progress;
    }

}
