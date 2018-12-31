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
        final List<String> groupMembers = new ArrayList<>();
        final EditText groupName = findViewById(R.id.textGroupName);
        if(!groupName.getText().toString().isEmpty()) {
            String members = "";
            recyclerView = findViewById(R.id.recyclerviewgroup);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("groups").child(groupName.getText().toString());

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkboxadd));
                String friend = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textfriend)).getText().toString();
                if (current.isChecked()) {
                    groupMembers.add(friend);
                    if (i == recyclerView.getChildCount() - 1) {
                        members = members + friend;
                    } else {
                        members = members + friend + "~";
                    }
                }
            }
            if(groupMembers.size() != 0) {
                final String finalMembers = members + "~" + user.getDisplayName();
                groupMembers.add(user.getDisplayName());
                List<String> secretGroup = secretGiftRandom(groupMembers);

                final DatabaseReference newRef = database.getReference("usernames");
                for (int i = 0; i < secretGroup.size(); i++) {
                    //Split and find out who has who
                    String temp = secretGroup.get(i).replace("-Has->", "~");
                    final String arrayOfPeople[] = temp.split("~");
                    newRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Person Giving Gift
                            String UID1 = dataSnapshot.child(arrayOfPeople[0]).getValue().toString();
                            //Person Receiving Gift
                            String UID2 = dataSnapshot.child(arrayOfPeople[1]).getValue().toString();

                            //Give each user group info
                            Map<String, Object> groupInfo = new HashMap<>();
                            groupInfo.put("Your Secret Person", UID2);
                            groupInfo.put("Name", groupName.getText().toString());
                            groupInfo.put("Members", finalMembers);
                            database.getReference("users").child(UID1).child("groups").child(groupName.getText().toString()).setValue(groupInfo);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                Toast.makeText(getApplicationContext(), "Group Created!", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(this, secretGiftGroup.class);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), "Select Some Friends!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Select A Group Name!", Toast.LENGTH_SHORT).show();
        }
    }

    List<String> secretGiftRandom(List<String> groupMembers){
        List<String> secretGifts = new ArrayList<>();
        List<Integer> chosen = new ArrayList<>();
        List<Integer> choosers = new ArrayList<>();

        while(chosen.size() != groupMembers.size() && choosers.size() != groupMembers.size()){
            int randNum1 = (int)(Math.random()*groupMembers.size());
            int randNum2 = (int)(Math.random()*groupMembers.size());
            if(randNum1 != randNum2) {
                if(!chosen.contains(randNum2) && !choosers.contains(randNum1)) {
                    choosers.add(Integer.valueOf(randNum1));
                    chosen.add(Integer.valueOf(randNum2));
                    secretGifts.add(groupMembers.get(randNum1) + "-Has->" + groupMembers.get(randNum2));
                }
            }
        }
        return secretGifts;
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
