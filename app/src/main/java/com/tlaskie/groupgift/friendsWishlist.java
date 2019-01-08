package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class friendsWishlist extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<String> friends = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_wishlist);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Spinner spinnerFriends = findViewById(R.id.spinnerFriends);
            spinnerFriends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    final AdapterView<?> tempParent = parent;
                    final int tempPosition = position;
                    if(!tempParent.getItemAtPosition(position).toString().equals("Your Friends: ") && !tempParent.getItemAtPosition(position).toString().equals("No Friends Found")) {
                        TextView nowishlist = findViewById(R.id.textNoWishlist);
                        nowishlist.setText("");
                        showList(tempParent.getItemAtPosition(position).toString());
                    }else{
                        TextView nowishlist = findViewById(R.id.textNoWishlist);
                        nowishlist.setText("");
                        showList("clear~");
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            showFriends();
        }else{
            returnToMain();
        }
    }

    public void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            TextView text = findViewById(R.id.textEmail);
            text.setText(email);
        }else{
            returnToMain();
        }
    }

    void showList(String friend){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(friend.equals("clear~")){
            //Clear RecyclerView
            recyclerView = findViewById(R.id.recyclerviewFriendsWishlist);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(null);
        }else{
            //Display Friend's Wishlist
            database.getReference("usernames").child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        String UID = dataSnapshot.getValue().toString();
                        updateRecycler(UID);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    void updateRecycler(String UID){
        //Fill Recycler With UID's Wishlist
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(UID).child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> details = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String name = dsp.child("Name").getValue().toString();
                        String desc = dsp.child("Description").getValue().toString();
                        String price = dsp.child("Price").getValue().toString();
                        String location = dsp.child("Location").getValue().toString();
                        details.add(name + "~" + desc + "~" + price + "~" + location);
                    }
                }
                if(details.isEmpty()){
                    TextView nowishlist = findViewById(R.id.textNoWishlist);
                    nowishlist.setText("No Items On Their Wishlist!");
                    recyclerView = findViewById(R.id.recyclerviewFriendsWishlist);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(new AdapterWishlist(getApplicationContext(), details));
                }else {
                    TextView nowishlist = findViewById(R.id.textNoWishlist);
                    nowishlist.setText("");
                    recyclerView = findViewById(R.id.recyclerviewFriendsWishlist);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(new AdapterWishlist(getApplicationContext(), details));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void showFriends(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String Friends = dataSnapshot.getValue().toString();
                    if(Friends.equals("")){
                        //No Friends In Database
                        friends.add("No Friends Found");
                    }else{
                        friends.add("Your Friends: ");
                    }
                    String arrayOfFriends[] = Friends.split("~");
                    friends.addAll(Arrays.asList(arrayOfFriends));
                    updateSpinner(friends);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateSpinner(List<String> list){
        Spinner friendsList = findViewById(R.id.spinnerFriends);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_friend, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item_friend);
        friendsList.setAdapter(dataAdapter);
    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
}
