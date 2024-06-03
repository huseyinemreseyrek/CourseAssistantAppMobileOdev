package com.huseyinemreseyrek.courseassistantapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SelectedPoll extends AppCompatActivity {

    FirebaseFirestore db;
    String name;
    String statusValue;
    String courseIDValue;
    String prompt;
    String email;
    String option_count;
    TextView pollPrompt;

    RadioGroup optionsRadioGroup;
    TextView resultsTextView;
    Button confirm;
    Button endPollButton;
    Button download;
    FirebaseAuth mAuth;
    String userId;

    ArrayList<String> info = new ArrayList<>();
    String fileName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_poll);

        pollPrompt = findViewById(R.id.textView1);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        resultsTextView = findViewById(R.id.resultsTextView);
        confirm = findViewById(R.id.confirmButton);
        endPollButton = findViewById(R.id.endPollButton);
        download = findViewById(R.id.downloadButton);

        name = getIntent().getStringExtra("pollName");
        courseIDValue = getIntent().getStringExtra("courseID");
        statusValue = getIntent().getStringExtra("status");


        db = FirebaseFirestore.getInstance();
        loadPollData();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        email = user.getEmail();
        userId = user.getUid(); // Get the user's ID

        // Show the "End Poll" button if the user is an instructor
        if (email.endsWith("@yildiz.edu.tr")) {
            endPollButton.setVisibility(View.VISIBLE);
        }
        if (email.endsWith("@std.yildiz.edu.tr")) {
            confirm.setVisibility(View.VISIBLE);
        }
        if (Objects.equals(statusValue, "ended") && email.endsWith("@yildiz.edu.tr")) {
            download.setVisibility(View.VISIBLE);
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.endsWith("@yildiz.edu.tr")) {
                    Toast.makeText(SelectedPoll.this, "Instructors cannot vote", Toast.LENGTH_SHORT).show();
                } else {
                    confirmVote();
                }
            }
        });

        endPollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPoll();
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepCSV();
            }

        });

    }

    private void loadPollData() {
        db.collection("Polls").document(name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Extract prompt and options
                                prompt = document.getString("prompt");
                                pollPrompt.setText(prompt);

                                statusValue = document.getString("status");
                                if ("ended".equals(statusValue)) {
                                    displayResults(document);
                                } else {
                                    Map<String, Long> options = (Map<String, Long>) document.get("options");
                                    if (options != null) {
                                        for (Map.Entry<String, Long> entry : options.entrySet()) {
                                            addOptionRadioButton(entry.getKey());
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void addOptionRadioButton(String optionName) {
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(optionName);
        optionsRadioGroup.addView(radioButton);
    }

    private void displayResults(DocumentSnapshot document) {
        optionsRadioGroup.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);
        endPollButton.setVisibility(View.GONE);
        resultsTextView.setVisibility(View.VISIBLE);

        Map<String, Long> options = (Map<String, Long>) document.get("options");



        if (options != null) {
            StringBuilder results = new StringBuilder("Poll Results:\n\n");
            for (Map.Entry<String, Long> entry : options.entrySet()) {
                results.append(entry.getKey()).append(": ").append(entry.getValue()).append(" votes\n");
                info.add(entry.getKey() + "," + entry.getValue());
            }
            resultsTextView.setText(results.toString());
        }
    }
    private boolean checkAndRequestStoragePermissions() {
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 3);
            return false;
        }
        return true;
    }
    private void savePollResultsToCSV() {
        StringBuilder csvData = new StringBuilder();
        for (String inf : info) {
            csvData.append(inf).append("\n");
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName;

        try {
            File file = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(csvData.toString().getBytes());
            fileOutputStream.close();
            Toast.makeText(SelectedPoll.this, "File saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {

            Toast.makeText(SelectedPoll.this, "File not saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3) {
            savePollResultsToCSV();
        }
    }
    private void prepCSV() {

        fileName = name + "_results.csv";
        fileName = fileName.replace(" ", "");
        if (checkAndRequestStoragePermissions()) {
            savePollResultsToCSV();
        }
    }

    private void confirmVote() {
        int selectedRadioButtonId = optionsRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(SelectedPoll.this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        final String selectedOption = selectedRadioButton.getText().toString();

        db.collection("Polls").document(name).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> voters = (List<String>) document.get("voters");
                                if (voters == null) {
                                    voters = new ArrayList<>();
                                }

                                if (voters.contains(userId)) {
                                    Toast.makeText(SelectedPoll.this, "You have already voted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Map<String, Long> options = (Map<String, Long>) document.get("options");
                                    if (options != null && options.containsKey(selectedOption)) {
                                        // get the current vote count for the selected option
                                        Long currentCount = options.get(selectedOption);
                                        if (currentCount != null) {
                                            // increment the count by one
                                            long newCount = currentCount + 1;
                                            // update the count of the selected option in the options map
                                            options.put(selectedOption, newCount);
                                            // add the user to the voters list
                                            voters.add(userId);
                                            // update the   options map and the voters list in Firestore
                                            db.collection("Polls").document(name)
                                                    .update("options", options, "voters", voters)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(SelectedPoll.this, "Vote recorded!", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(SelectedPoll.this, "Failed to record vote", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(SelectedPoll.this, "Option count not found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(SelectedPoll.this, "Selected option not found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(SelectedPoll.this, "Poll document not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SelectedPoll.this, "Failed to get poll document", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void endPoll() {
        db.collection("Polls").document(name)
                .update("status", "ended")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SelectedPoll.this, "Poll ended!", Toast.LENGTH_SHORT).show();
                            loadPollData(); // reload data to shw results
                        } else {
                            Toast.makeText(SelectedPoll.this, "Failed to end poll", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}