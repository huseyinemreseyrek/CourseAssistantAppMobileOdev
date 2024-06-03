package com.huseyinemreseyrek.courseassistantapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huseyinemreseyrek.courseassistantapp.R;

import java.util.HashMap;
import java.util.Map;

public class CreatePoll extends AppCompatActivity {

    EditText pollPrompt;
    EditText pollName;
    LinearLayout optionsContainer;  // dynamic linear layout, hold options

    Button confirm;

    FirebaseFirestore fStore;

    String courseID;
    String group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
        Bundle extras = getIntent().getExtras();
        courseID = extras.getString("courseID").toString();
        group = extras.getString("group").toString();

        pollPrompt = findViewById(R.id.poll_prompt);
        pollName = findViewById(R.id.poll_name);
        Spinner optionCountSpinner = findViewById(R.id.option_count_spinner);
        optionsContainer = findViewById(R.id.options_container);
        confirm = findViewById(R.id.btn_confirm);
        fStore = FirebaseFirestore.getInstance();

        // Initialize the spinner with values from 2 to 6
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getOptionCountValues());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        optionCountSpinner.setAdapter(adapter);

        // Item selection spinner
        optionCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                int selectedNumber = (int) parent.getItemAtPosition(position);
                createOptionFields(selectedNumber);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    submitPollToFirestore();
                } else {
                    Toast.makeText(CreatePoll.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean validateInputs() {
        if (pollPrompt.getText().toString().isEmpty() || pollName.getText().toString().isEmpty())
        {
            return false;
        }

        for (int i = 0; i < optionsContainer.getChildCount(); i++)
        {
            EditText optionField = (EditText) optionsContainer.getChildAt(i);

            if (optionField.getText().toString().isEmpty())
            {
                return false;
            }
        }
        return true;
    }
    // create poll and put it into database
    private void submitPollToFirestore() {
        String prompt = pollPrompt.getText().toString().trim();
        String name = pollName.getText().toString().trim();
        int optionCount = optionsContainer.getChildCount();
        Map<String, Object> poll = new HashMap<>();
        poll.put("prompt", prompt);             // put prompt to database
        poll.put("option_count", optionCount);  // put option count to database

        // put options and their vote count to database, initialize them to 0
        Map<String, Integer> options = new HashMap<>();
        for (int i = 0; i < optionCount; i++) {
            EditText optionField = (EditText) optionsContainer.getChildAt(i);
            options.put(optionField.getText().toString(), 0);
        }
        poll.put("options", options);
        poll.put("course", courseID);
        poll.put("name", name);
        poll.put("group", group);
        poll.put("status", "Pending");

        fStore.collection("Polls")
                .document(name)
                .set(poll)
                .addOnSuccessListener(documentReference -> Toast.makeText(CreatePoll.this, "Poll created successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CreatePoll.this, "Failed to create poll", Toast.LENGTH_SHORT).show());
    }

    // integer array from 2 to 9
    private Integer[] getOptionCountValues()
    {
        Integer[] values = new Integer[5];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = i + 2;
        }
        return values;
    }

    private void createOptionFields(int count)
    {
        optionsContainer.removeAllViews(); // Clear existing fields
        for (int i = 0; i < count; i++)
        {
            EditText optionField = new EditText(this);
            optionField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            optionField.setHint("Option " + (i + 1));
            optionField.setInputType(InputType.TYPE_CLASS_TEXT);
            optionsContainer.addView(optionField);
        }
    }
}

