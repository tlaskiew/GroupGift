package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class secretGiftGroup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<String> groupNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_gift_group);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            returnToMain();
        }

        groupNames.add("Select Group To View");
        getGroups();
        showDetails();

    }

    void getGroups(){
        //Get groups from database and split them with details
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dsp : dataSnapshot.getChildren()) {
                    groupNames.add(dsp.child("Name").getValue().toString());
                }
                addToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void showDetails(){
        //Insert Useful Code...
        List<String> details = new ArrayList<>();
        details.add("Name~Description~Price~Location");
        details.add("NameTemp~DescriptionTemp~PriceTemp~Location~temp");
        recyclerView = findViewById(R.id.recyclerviewsecretgroup);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new AdapterWishlist(getApplicationContext(), details));
    }

    void addToList(){
        Spinner groups = findViewById(R.id.spinnerGroups);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, groupNames);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        groups.setAdapter(dataAdapter);
    }

    void goToCreateGroup(View v){
        Intent intent = new Intent(this, createGiftGroup.class);
        startActivity(intent);
    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
}
