package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class wishlist extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<String> wishlist = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //Check if logged in
        if (user == null) {
            returnToMain();
        }else {
            //Setup spinner
            wishlist.add("Your Wishlist:");
            addToList(wishlist);

            //Set up and add to list spinner
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("wishlist");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Loop through rewards in database and display current rewards
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String name = dsp.child("Name").getValue().toString();
                        String desc = dsp.child("Description").getValue().toString();
                        String price = dsp.child("Price").getValue().toString();
                        String location = dsp.child("Location").getValue().toString();

                        // Making sure no duplicates
                        if (!wishlist.contains(name)) {
                            // Adding and Displaying list to user
                            wishlist.add(name);
                            addToList(wishlist);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            //Update the wishlist item details below
            Spinner curWishList = findViewById(R.id.spinnerCurrentWishlist);
            curWishList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    final AdapterView<?> tempParent = parent;
                    final int tempPosition = position;
                    if (!tempParent.getItemAtPosition(position).toString().equals("Your Wishlist:")) {
                        TextView amount = findViewById(R.id.textAmountOfItems);
                        amount.setVisibility(View.INVISIBLE);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        database.getReference("users").child(user.getUid()).child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                    if (tempParent.getItemAtPosition(tempPosition).toString().equals(dsp.child("Name").getValue().toString())) {
                                        String name = dsp.child("Name").getValue().toString();
                                        String desc = dsp.child("Description").getValue().toString();
                                        String price = dsp.child("Price").getValue().toString();
                                        String location = dsp.child("Location").getValue().toString();

                                        List<String> item = new ArrayList<>();
                                        item.add(name + "~" + desc + "~" + price + "~" + location);
                                        recyclerView = findViewById(R.id.recyclerviewwishlist);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                        recyclerView.setAdapter(new AdapterItem(getApplicationContext(), item));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        TextView amount = findViewById(R.id.textAmountOfItems);
                        amount.setVisibility(View.VISIBLE);
                        recyclerView = findViewById(R.id.recyclerviewwishlist);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerView.setAdapter(null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            database.getReference("users").child(user.getUid()).child("wishlist").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    wishlist.clear();
                    wishlist.add("Your Wishlist:");
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
        }
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            returnToMain();
        }

    }

    void addToWishlist(View v){
        EditText name = findViewById(R.id.itemName);
        EditText desc = findViewById(R.id.itemDescription);
        EditText price = findViewById(R.id.itemPrice);
        EditText location = findViewById(R.id.itemLocation);

        if(check(name) && check(desc) && check(price) && check(location)) {
            if(name.length() > 32){
                Toast.makeText(getApplicationContext(), "Name Can't Be Longer Than 32!", Toast.LENGTH_SHORT).show();
            }else if(desc.length() > 32){
                Toast.makeText(getApplicationContext(), "Description Can't Be Longer Than 32!", Toast.LENGTH_SHORT).show();
            }else if(price.length() > 32){
                Toast.makeText(getApplicationContext(), "Price Can't Be Longer Than 32!", Toast.LENGTH_SHORT).show();
            }else if(location.length() > 32){
                Toast.makeText(getApplicationContext(), "Location Can't Be Longer Than 32!", Toast.LENGTH_SHORT).show();
            }else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users").child(user.getUid()).child("wishlist").child(name.getText().toString());
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("Name", name.getText().toString());
                userInfo.put("Description", desc.getText().toString());
                userInfo.put("Price", price.getText().toString());
                userInfo.put("Location", location.getText().toString());
                myRef.setValue(userInfo);
                name.setText("");
                desc.setText("");
                price.setText("");
                location.setText("");
                wishlist.add(name.getText().toString());
                addToList(wishlist);
            }
        }else{
            Toast.makeText(getApplicationContext(), "Please Fill In Everything!", Toast.LENGTH_SHORT).show();
        }
    }

    void remove(View v){
        View viewAdd = findViewById(R.id.layoutAddView);
        View viewRemove = findViewById(R.id.layoutRemoveView);
        viewRemove.setVisibility(View.VISIBLE);
        viewAdd.setVisibility(View.INVISIBLE);
    }

    void removeCur(View v){
        Spinner current = findViewById(R.id.spinnerCurrentWishlist);
        String curItem = current.getSelectedItem().toString();

        if(!curItem.equals("Your Wishlist:")) {
            wishlist.remove(curItem);
            Spinner spin = findViewById(R.id.spinnerCurrentWishlist);
            spin.setSelection(0);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("users").child(user.getUid()).child("wishlist").child(curItem).removeValue();
        }
    }

    void add(View v){
        View viewAdd = findViewById(R.id.layoutAddView);
        View viewRemove = findViewById(R.id.layoutRemoveView);
        viewRemove.setVisibility(View.INVISIBLE);
        viewAdd.setVisibility(View.VISIBLE);
    }

    boolean check(EditText text){
        return !text.getText().toString().isEmpty();
    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    void addToList(List<String> list){
        Spinner wish = findViewById(R.id.spinnerCurrentWishlist);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        wish.setAdapter(dataAdapter);

        TextView amount = findViewById(R.id.textAmountOfItems);
        if(amount.getVisibility() == View.VISIBLE) {
            int count = list.size() - 1;
            if (count == 0) {
                amount.setText("You Have No \nItems In Your Wishlist!");
            } else if (count == 1) {
                amount.setText("You Have " + count + "\nItem In Your Wishlist!");
            } else {
                amount.setText("You Have " + count + "\nItems In Your Wishlist!");
            }
        }
    }

}
