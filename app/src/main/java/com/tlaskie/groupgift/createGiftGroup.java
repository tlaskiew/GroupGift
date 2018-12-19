package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class createGiftGroup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift_group);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            returnToMain();
        }

        showList();
    }



    void createGroup(View v){
        EditText groupName = findViewById(R.id.textGroupName);
        if(!groupName.getText().toString().isEmpty()) {
            String members = "";
            recyclerView = findViewById(R.id.recyclerviewgroup);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("groups").child(groupName.getText().toString());

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkboxadd));
                String friend = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textfriend)).getText().toString();
                if (current.isChecked()) {
                    if (i == recyclerView.getChildCount() - 1) {
                        members = members + friend;
                    } else {
                        members = members + friend + "~";
                    }
                }
            }

            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("Name", groupName.getText().toString());
            groupInfo.put("Members", members);
            myRef.setValue(groupInfo);

            Toast.makeText(getApplicationContext(), "Group Created!", Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent(this, secretGiftGroup.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "Select A Group Name!", Toast.LENGTH_LONG).show();
        }
    }

    void showList(){
        final List<String> friends = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String Friends = dataSnapshot.getValue().toString();
                    String arrayOfFriends[] = Friends.split("~");
                    friends.addAll(Arrays.asList(arrayOfFriends));
                    recyclerView = findViewById(R.id.recyclerviewgroup);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(new AdapterGroup(getApplicationContext(), friends));
                }else{
                    TextView none = findViewById(R.id.textViewNone);
                    none.setText("No Friends Found!");
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
}
