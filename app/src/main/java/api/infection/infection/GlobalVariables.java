package api.infection.infection;

import android.app.Application;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;


public class GlobalVariables extends Application {
    private static final int MAX_HIGHSCORE = 5;
    private static final String settings_file = "settings.txt";

    public boolean sound;
    public int crosshair_size;
    public int[] highscore = new int[5];

    public void init() {
        //Initialize first time run
        int zeroes[] = {0, 0, 0, 0, 0};
        System.arraycopy(zeroes, 0, highscore, 0, MAX_HIGHSCORE);
        crosshair_size = 5;
        sound = false;
    }

    public void load_settings() {
        try {
            InputStream file = openFileInput(settings_file);

            if(file != null) {
                InputStreamReader is = new InputStreamReader(file);
                BufferedReader in = new BufferedReader(is);

                String line = "";

                line = in.readLine();
                if(line.equals("1")) sound = true; else sound = false;
                crosshair_size = Integer.parseInt(in.readLine());

                highscore[0] = Integer.parseInt(in.readLine());
                highscore[1] = Integer.parseInt(in.readLine());
                highscore[2] = Integer.parseInt(in.readLine());
                highscore[3] = Integer.parseInt(in.readLine());
                highscore[4] = Integer.parseInt(in.readLine());
                in.close();
            }
        } catch(FileNotFoundException e) {
            /*new AlertDialog.Builder(this)
                    .setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();*/
            init();
            save_settings();
        } catch(Exception e) {

        }
    }

    public void save_settings() {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(settings_file, MODE_MULTI_PROCESS));
            if(sound) out.write("1\n"); else out.write("0\n");
            out.write(String.valueOf(crosshair_size) + "\n");
            out.write(String.valueOf(highscore[0]) + "\n");
            out.write(String.valueOf(highscore[1]) + "\n");
            out.write(String.valueOf(highscore[2]) + "\n");
            out.write(String.valueOf(highscore[3]) + "\n");
            out.write(String.valueOf(highscore[4]) + "\n");
            out.close();
        } catch (Exception e) {
            /*new AlertDialog.Builder(this)
                    .setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();*/
        }
    }
}
