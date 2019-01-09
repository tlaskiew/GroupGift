package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Hub extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<String> wishlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            TextView text = findViewById(R.id.username);
            text.setText(email);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("users").child(user.getUid()).child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    wishlist.clear();
                    wishlist.add("Wishlist QuickView:");
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String name = dsp.child("Name").getValue().toString();
                        String desc = dsp.child("Description").getValue().toString();
                        String price = dsp.child("Price").getValue().toString();
                        String location = dsp.child("Location").getValue().toString();

                        // Making sure no duplicates
                        if (!wishlist.contains(name)) {
                            // Adding and Displaying list to user
                            wishlist.add(name);

                        }
                    }
                    addToList(wishlist);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            database.getReference("users").child(user.getUid()).child("wishlist").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    wishlist.clear();
                    wishlist.add("Wishlist QuickView:");
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String name = dsp.child("Name").getValue().toString();
                        String desc = dsp.child("Description").getValue().toString();
                        String price = dsp.child("Price").getValue().toString();
                        String location = dsp.child("Location").getValue().toString();

                        // Making sure no duplicates
                        if (!wishlist.contains(name)) {
                            // Adding and Displaying list to user
                            wishlist.add(name);

                        }
                    }
                    addToList(wishlist);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Button buttonWishlist = findViewById(R.id.buttonWishlist);
            Button buttonFriends = findViewById(R.id.buttonFriends);
            Button buttonFriendsWishlist = findViewById(R.id.buttonFriendsWishlist);
            Button buttonSecretGifts = findViewById(R.id.buttonSecretGifts);
            Button buttonSettings = findViewById(R.id.buttonAccountSettings);

            buttonWishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToFriendsWishlist();
                }
            });
            buttonFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToFriends();
                }
            });
            buttonFriendsWishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToFriendsWishlist();
                }
            });
            buttonSecretGifts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToSecretGiftGroup();
                }
            });
            buttonSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToAccountSettings();
                }
            });
        }else{
            returnToMain();
        }
    }

    public void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            TextView text = findViewById(R.id.username);
            text.setText(email);
        }else{
            returnToMain();
        }
    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    void logout(){
        FirebaseAuth.getInstance().signOut();
        returnToMain();
    }

    void goToWishlist(){
        Intent intent = new Intent(this, wishlist.class);
        startActivity(intent);
    }

    void goToFriends(){
        Intent intent = new Intent(this, friends.class);
        startActivity(intent);
    }

    void goToAccountSettings(){
        Intent intent = new Intent(this, accountSettings.class);
        startActivity(intent);
    }

    void goToSecretGiftGroup(){
        Intent intent = new Intent(this, secretGiftGroup.class);
        startActivity(intent);
    }

    void goToFriendsWishlist(){
        Intent intent = new Intent(this, friendsWishlist.class);
        startActivity(intent);
    }

    void addToList(List<String> list){
        Spinner wish = findViewById(R.id.spinnerQuickList);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        wish.setAdapter(dataAdapter);
    }
}
