package api.infection.infection;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This class creates the high scores page, and displays high scores from the high score file.
 */
public class HighScoreActivity extends Activity {
    TextView scores;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        scores = (TextView) findViewById(R.id.Scores);
        scores.setText(((GlobalVariables) this.getApplication()).highscore[4] + "\n\n"
                     + ((GlobalVariables) this.getApplication()).highscore[3] + "\n\n"
                     + ((GlobalVariables) this.getApplication()).highscore[2] + "\n\n"
                     + ((GlobalVariables) this.getApplication()).highscore[1] + "\n\n"
                     + ((GlobalVariables) this.getApplication()).highscore[0]);
    }

}
