package com.huseyinemreseyrek.courseassistantapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.huseyinemreseyrek.courseassistantapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;







public class ShowPolls extends AppCompatActivity {

    String courseID;
    String group;
    String email;
    FirebaseFirestore db;
    String pollName;
    String status;
    ArrayList<Poll> polls = new ArrayList<>();
    RecyclerViewShowPollsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_polls);

        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            courseID = extras.getString("courseID");
            group = extras.getString("group");
            email = extras.getString("email");
        }


        db = FirebaseFirestore.getInstance();
        CollectionReference pollsRef = db.collection("Polls");
        Query query = pollsRef.whereEqualTo("group", group)
                .whereEqualTo("course", courseID);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Query successful, handle the QuerySnapshot
                    QuerySnapshot querySnapshot = task.getResult();
                    // Iterate through the documents
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            polls.add(new Poll(
                                    document.getString("name"),
                                    document.getString("course")+"-" + group,
                                    document.getString("status")
                            ));
                        }
                        // Notify adapter of data change
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        Toast.makeText(ShowPolls.this, "Failed to create poll", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        adapter = new RecyclerViewShowPollsAdapter(this, polls);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}