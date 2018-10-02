package api.infection.infection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Random;

enum sound_type { SOUND_BUZZER, SOUND_CLICK, SOUND_HIT, SOUND_PING, SOUND_SHOOT };

/**
 * This is where the main gameplay activity occurs.
 * This is run after pressing the play button from the main menu.
 */
public class CameraActivity extends Activity implements SensorEventListener {
    //Set globals
    private static final int MAX_TIME = 60000;
    private static final int MAX_ENEMY = 50;
    private static final int MAX_VIRUS_SIZE = 32;
    private static final int MAX_X = 800;
    private static final int MAX_Y = 1600;
    private static final float ALPHA = 0.25f;

    private static final float X_MOV = 0.1f;
    private static final float MOVSEN1 = 0.02f;
    private static final float MOVSEN2 = 0.01f;
    private static final float MULTIP = 150.0f;

    class Enemy {
        public int              x;
        public int              y;
        public int              health = 100;
        public ImageView        src;
        public RelativeLayout   src_lay;
        public int              virus_type; //future use

        Enemy() {
            src = new ImageView(getApplicationContext());
            src_lay = new RelativeLayout(getApplicationContext());
        }
    };

    //Interface variables
    private Context             context;
    private CameraSurfaceView   camera_view;
    private CountDownTimer      game_timer;
    private FrameLayout         frame_layout;
    private Button              shoot_button;
    private ImageView           sound_button;
    private SensorManager       sensor_manager;
    private TextView            accuracy_text, timer_text, score_text;

    //Sensor variables
    float[] sensor_magnet = new float[3];
    float[] sensor_accel = new float[3];
    float[] sensor_orientation = new float[3];
    float[] sensor_matrix = new float[9];
    float old_x = 0;
    float old_y = 0;
    float old_z = 0;
    float old_ax = 0;
    float old_ay = 0;
    float old_az = 0;

    //Game objects
    private Enemy               enemy[] = new Enemy[MAX_ENEMY];

    //Game settings
    private float       ammo_used = 0;
    private float       ammo_hit = 0;
    private int         crosshair_size;
    private int[]       high_score = new int[5];
    private int         player_score = 0;
    private boolean     sound_on;
    private MediaPlayer mplayer = null;

    /**
     * Main activity functions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        context = this;

        //Load settings
        get_settings();
        set_crosshair(((GlobalVariables) this.getApplication()).crosshair_size);

        //Setup the camera surface
        camera_view = new CameraSurfaceView(getApplicationContext());

        //Setup sensor
        set_sensor();

        //Initialize and setup the layout
        frame_layout = (FrameLayout) findViewById(R.id.frame_camera);
        shoot_button = (Button) findViewById(R.id.btn_shoot);
        shoot_button.setOnClickListener
             (new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    player_shoots();
                }
             });
        sound_button = (ImageView) findViewById(R.id.btn_sound);
        if(sound_on) sound_button.setImageResource(R.drawable.img_sound1);
        else sound_button.setImageResource(R.drawable.img_sound2);
        sound_button.setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        set_sound();
                    }
                });
        timer_text = (TextView) findViewById(R.id.txt_timer);
        score_text = (TextView) findViewById(R.id.txt_score);
        accuracy_text = (TextView) findViewById(R.id.txt_acc);

        //Initialize score
        score_text.setText("Score: " + player_score);
        accuracy_text.setText("Accuracy: 0%");

        //Add the camera to the layout
        frame_layout.addView(camera_view);


        //Create a timer
        game_timer = create_gametimer(MAX_TIME);
        game_timer.start();

        create_enemies();
    }

    /**
     * This is when the game gets exited.
     */
    @Override
    protected void onPause() {
        super.onPause();

        ((GlobalVariables) this.getApplication()).sound = sound_on;
        ((GlobalVariables) this.getApplication()).crosshair_size = crosshair_size;
        ((GlobalVariables) this.getApplication()).save_settings();

        unreg();
        finish();
        System.exit(0);
    }

    /**
     * Camera measurements are done here.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensor_accel = lowpass_filter(event.values.clone(), sensor_accel);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensor_magnet = lowpass_filter(event.values.clone(), sensor_magnet);
        }

        sensor_manager.getRotationMatrix(sensor_matrix, null, sensor_accel, sensor_magnet);
        sensor_manager.getOrientation(sensor_matrix, sensor_orientation);

        //Calculate orientation difference
        float new_x, new_y, new_z, new_ax, new_ay, new_az;
        long time_delay = System.currentTimeMillis(), last_time = 0;

        //Now lets check with time delay, and see if users oriented cellphone
        if(time_delay - last_time > 100) { //&& orient_set) {
            float avg;
            last_time = time_delay;

            //Calculate orientation
            new_x = (old_x - sensor_orientation[2]);
            new_y = (old_y - sensor_orientation[1]);
            new_z = (old_z - sensor_orientation[0]);

            old_x = sensor_orientation[2];
            old_y = sensor_orientation[1];
            old_z = sensor_orientation[0];

            new_ax = (old_ax - sensor_accel[0]);
            new_ay = (old_ay - sensor_accel[1]);
            new_az = (old_az - sensor_accel[2]);

            old_ax = sensor_accel[0];
            old_ay = sensor_accel[1];
            old_az = sensor_accel[2];

            //Check if there is acceleration in x
            if(Math.abs(old_ax) > X_MOV) {
                //Get accelaration and multiply by 120
                avg =  Math.abs(new_ax) * MULTIP;

                //Check if phone tilted. If tilted left, multiply avg by -1
                if(new_x > MOVSEN1)
                    avg *= -1;
                else if(new_x < -MOVSEN2);
                else avg = 0;

                //Move each enemy object by how much phone is tilted
                for (int i = 0; i != MAX_ENEMY; ++i) {
                    enemy[i].x += ((int) avg);
                    enemy[i].src_lay.setPadding(enemy[i].x, enemy[i].y, 0, 0);

                }
            }
            //Check if there is acceleration in y
            if(Math.abs(old_ay) > 0.2) {
                //Get accelaration and multiply by 120
                avg = Math.abs(new_ay) * MULTIP;

                //Check if phone tilted. If tilted down, multiply avg by -1
                if(new_y < -MOVSEN1)
                    avg *= -1;
                else if(new_y > MOVSEN2);
                else avg = 0;

                //Move each enemy object by how much phone is tilted
                for (int i = 0; i != MAX_ENEMY; ++i) {
                    enemy[i].y += ((int) avg);
                    enemy[i].src_lay.setPadding(enemy[i].x, enemy[i].y, 0, 0);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * Simply creates enemies in the environment.
     */
    private void create_enemies() {
        FrameLayout fr = (FrameLayout) findViewById(R.id.frame_game);
        for(int i = 0; i != MAX_ENEMY; ++i) {
            create_one(i);
            fr.addView(enemy[i].src_lay);
        }
    }

    /**
     * Create an enemy.
     */
    private void create_one(int i) {
        enemy[i] = new Enemy();
        enemy[i].x = get_ranCoord('x');
        enemy[i].y = get_ranCoord('y');
        enemy[i].virus_type = get_random(0, 2);

        //Check virus types
        switch(enemy[i].virus_type) {
            case 0:
                enemy[i].health = 100;
                break;
            case 1:
                enemy[i].health = 120;
                break;
            case 2:
                enemy[i].health = 150;
                break;
        }

        //Set images
        enemy[i].src.setImageResource(R.drawable.img_virus1);

        enemy[i].src_lay.setPadding(enemy[i].x, enemy[i].y, 0, 0);
        enemy[i].src_lay.addView(enemy[i].src);
    }

    /**
     * Creates our in-game timer.
     */
    private CountDownTimer create_gametimer(int max_time) {
        return new CountDownTimer(max_time, 1000) {

            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished < 6000) {
                    timer_text.setTextColor(Color.RED);

                    if(sound_on)
                        play_sound(sound_type.SOUND_PING);
                }

                timer_text.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //Play buzzer
                if(sound_on)
                    play_sound(sound_type.SOUND_BUZZER);

                //Check if highscore
                player_score += player_score * (ammo_hit / ammo_used);
                if(player_score > high_score[0]) {
                   set_high();
                }

                //Unregister stuffs
                unreg();

                //Show dialog
                new AlertDialog.Builder(context).setTitle("Gameover").setMessage("GameOver! Your score: " + player_score).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        };
    }

    /**
     * Performs animations if object is shot.
     */
    private void do_animate(final ImageView img) {
        new CountDownTimer(120, 60) {

            public void onTick(long millisUntilFinished) {
                img.setImageResource(R.drawable.img_virus2);
            }

            public void onFinish() {
                img.setImageResource(R.drawable.img_virus1);
            }
        }.start();
    }

    /**
     * Increments player score for successful shots.
     */
    private void increment_score() {
        player_score += 25;
    }

    /**
     * Gets center of current position.
     */
    private int get_center_point(char c) {
        Display d = getWindowManager().getDefaultDisplay();
        int ret = 0;
        Point s = new Point();

        d.getSize(s);

        if(c == 'x')
            ret = (s.x / 2);
        else if(c == 'y')
            ret = (s.y / 2);

        return ret;
    }

    /**
     * Applies low pass filter for some signal smoothness.
     */
    private float[] lowpass_filter(float[] in, float[] out) {
        if(out == null) return in;
        else {
            for(int i = 0; i < in.length; ++i) {
                out[i] = out[i] + ALPHA * (in[i] - out[i]);
            }
        }

        return out;
    }

    /**
     * Applies saved settings.
     */
    private void get_settings() {
        sound_on = ((GlobalVariables) this.getApplication()).sound;
        crosshair_size = ((GlobalVariables) this.getApplication()).crosshair_size;
        System.arraycopy(((GlobalVariables) this.getApplication()).highscore, 0, high_score, 0, 5);
    }

    /**
     * Gets a random integer with bounding.
     */
    private int get_random(int left, int right) {
        int ret;
        Random rand = new Random();

        ret = rand.nextInt((right - left) + 1) + left;

        return ret;
    }

    /**
     * Gets a random coordinate for an enemy.
     */
    private int get_ranCoord(char c) {
        int ret = 0;
        int LEFT_LIMIT = 0, RIGHT_LIMIT = 0;
        Random rand = new Random();

        if(c =='x') {
            LEFT_LIMIT = -MAX_X;
            RIGHT_LIMIT = MAX_X;
        } else if(c == 'y') {
                LEFT_LIMIT = -MAX_Y;
                RIGHT_LIMIT = MAX_Y;
        }

        ret = get_random(LEFT_LIMIT, RIGHT_LIMIT);

        return ret;
    }

    /**
     * Performs functionality for shooting enemies.
     */
    private void player_shoots() {
        if (sound_on) {
            play_sound(sound_type.SOUND_SHOOT);
        }

        ++ammo_used;

        //Did it hit enemy?
        for(int i = 0; i != MAX_ENEMY; ++i) {
            if( (enemy[i].x >= get_center_point('x') - MAX_VIRUS_SIZE * 3) && (enemy[i].x <= get_center_point('x') - MAX_VIRUS_SIZE / 3) ) {
                if( (enemy[i].y >= get_center_point('y') - MAX_VIRUS_SIZE * 3) && (enemy[i].y <= get_center_point('y') - MAX_VIRUS_SIZE / 3) ) {
                    play_sound(sound_type.SOUND_HIT);
                    enemy[i].health -= 50;
                    ++ammo_hit;

                    if(enemy[i].health <= 0) {
                        enemy[i].src_lay.removeAllViews();
                        create_one(i);
                    } else
                        do_animate(enemy[i].src);

                    set_player_score();
                }
            }
        }

        accuracy_text.setText("Accuracy: " + Math.round((ammo_hit/ammo_used) * 100.0 * (10 / 10.0)) + "%" );
    }

    /**
     * Plays the various sounds in our game.
     */
    private void play_sound(sound_type stype) {
        switch(stype) {
            case SOUND_BUZZER:
                mplayer = mplayer.create(this, R.raw.sound_buzzer);
                break;
            case SOUND_CLICK:
                mplayer = mplayer.create(this, R.raw.sound_click);
                break;
            case SOUND_HIT:
                mplayer = mplayer.create(this, R.raw.sound_blob);
                break;
            case SOUND_PING:
                mplayer = mplayer.create(this, R.raw.sound_ping);
                break;
            case SOUND_SHOOT:
                mplayer = mplayer.create(this, R.raw.sound_laser);
                break;
        }

        mplayer.start();
    }

    /**
     * Sets the crosshair size for a game.
     */
    private void set_crosshair(int crosshair_size) {
        ImageView ch = (ImageView) findViewById(R.id.img_crosshair);

        switch(crosshair_size) {
            case 0:
                ch.setImageResource(R.drawable.img_crosshair24);
                break;
            case 1:
                ch.setImageResource(R.drawable.img_crosshair32);
                break;
            case 2:
                ch.setImageResource(R.drawable.img_crosshair48);
                break;
            case 3:
                ch.setImageResource(R.drawable.img_crosshair);
                break;
            case 4:
                ch.setImageResource(R.drawable.img_crosshair96);
                break;
        }
    }

    /**
     * Sets high scores from saved instances.
     */
    private void set_high() {
        ((GlobalVariables) this.getApplication()).highscore[0] = player_score;
        Arrays.sort(((GlobalVariables) this.getApplication()).highscore);
        ((GlobalVariables) this.getApplication()).save_settings();
    }

    /**
     * Sets the player's current score.
     */
    private void set_player_score() {
        increment_score();
        score_text.setText("Score: " + player_score);
    }

    /**
     * Sets the sound functionality for our in game sound button.
     */
    private void set_sound() {
        if(sound_on) {
            sound_on = false;
            sound_button.setImageResource(R.drawable.img_sound2);
        } else {
            sound_on = true;
            sound_button.setImageResource(R.drawable.img_sound1);
        }
    }

    /**
     * Sets sensor variables appropriately.
     */
    private void set_sensor() {
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensor_manager.SENSOR_DELAY_NORMAL);
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensor_manager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Unregisters the camera.
     */
    private void unreg() {
        sensor_manager.unregisterListener(this);
        camera_view.surface_closecamera();
    }
}
