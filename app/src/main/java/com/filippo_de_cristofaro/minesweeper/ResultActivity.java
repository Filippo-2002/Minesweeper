package com.filippo_de_cristofaro.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        TextView resultTextView = findViewById(R.id.resultTextView);
        TextView timeTakenTextView = findViewById(R.id.timeTakenTextView);
        Button restartButton = findViewById(R.id.restartButton);

        //get data
        Intent intent = getIntent();
        String result = intent.getStringExtra("RESULT");
        int time = intent.getIntExtra("TIME", 0);

        //set data
        if (result != null) {
            if (result.equals("won")) {
                resultTextView.setText(getString(R.string.You_won));
            } else {
                resultTextView.setText(getString(R.string.You_lost));
            }
        } else {
            resultTextView.setText(getString(R.string.game_over));
        }


        timeTakenTextView.setText(getString(R.string.time_taken_text, time));

        //restart
        restartButton.setOnClickListener(view -> {
            Intent restartIntent = new Intent(ResultActivity.this, MainActivity.class);
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(restartIntent);
            finish();
        });
    }
}
