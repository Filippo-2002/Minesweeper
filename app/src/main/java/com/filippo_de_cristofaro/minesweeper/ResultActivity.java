package com.filippo_de_cristofaro.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize UI components
        TextView resultTextView = findViewById(R.id.resultTextView);
        TextView timeTakenTextView = findViewById(R.id.timeTakenTextView);
        Button restartButton = findViewById(R.id.restartButton);

        // Get data from intent
        Intent intent = getIntent();
        String result = intent.getStringExtra("RESULT");
        int time = intent.getIntExtra("TIME", 0);

        // Set result text
        if (result != null) {
            if (result.equals("won")) {
                resultTextView.setText(R.string.You_won);
            } else {
                resultTextView.setText(R.string.You_lost);
            }
        }

        // Set time taken
        timeTakenTextView.setText("Time: " + time + "s");

        // Set restart button listener
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent restartIntent = new Intent(ResultActivity.this, MainActivity.class);
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(restartIntent);
                finish();
            }
        });
    }
}
