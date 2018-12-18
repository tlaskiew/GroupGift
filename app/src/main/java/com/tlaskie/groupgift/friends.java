package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class friends extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<String> items = new ArrayList<>();
    private List<String> curFriends = new ArrayList<>();
    private static final String TAG = "friends";

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        if(user == null){
            returnToMain();
        }

        TextView email = findViewById(R.id.email);
        email.setText(user.getEmail());


        database.getReference("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    if(!dsp.getKey().equals(user.getDisplayName())) {
                        items.add(dsp.getKey());
                    }
                }
                addToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void addToList(){
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(this, items));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String Friends = dataSnapshot.getValue().toString();
                String arrayOfFriends[] = Friends.split("~");
                curFriends.addAll(Arrays.asList(arrayOfFriends));
                for(int i = 0; i < recyclerView.getChildCount(); i++){
                    CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkbox));
                    String friend = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.item)).getText().toString();
                    if(curFriends.contains(friend)){
                        current.setChecked(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    void updateFriendList(View v){
        List<String> friends = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("friends");
        int count = 0;
        for(int i = 0; i < recyclerView.getChildCount(); i++){
            CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkbox));
            if(current.isChecked()) {
                count++;
                String friend = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.item)).getText().toString();
                friends.add(friend);
            }
        }
        if(count == 0){
            Toast.makeText(getApplicationContext(), "No Friends On Friendlist!", Toast.LENGTH_LONG).show();
        }else if(count == 1){
            Toast.makeText(getApplicationContext(), "Added " + count + " Friend To Your Friendlist!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "Added " + count + " Friends To Your Friendlist!", Toast.LENGTH_LONG).show();
        }
        String temp = "";
        for(int i = 0; i < friends.size(); i++){
            if(i == friends.size()-1){
                temp = temp + friends.get(i);
            }else {
                temp = temp + friends.get(i) + "~";
            }
        }
        myRef.setValue(temp);
    }

}
