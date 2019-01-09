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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

        Button add = findViewById(R.id.buttonGoToCreate);
        Button remove = findViewById(R.id.removeSecretGiftGroup);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCreateGroup();
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup();
            }
        });

        Spinner wishlist = findViewById(R.id.spinnerGroups);
        wishlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final AdapterView<?> tempParent = parent;
                final int tempPosition = position;
                if(!tempParent.getItemAtPosition(position).toString().equals("Select Group To View")) {
                    showDetails(tempParent.getItemAtPosition(position).toString());
                }else{
                    showDetails("clear");
                    findViewById(R.id.textPersonTitle).setVisibility(View.INVISIBLE);
                    findViewById(R.id.textBudgetTitle).setVisibility(View.INVISIBLE);
                    TextView text = findViewById(R.id.textPerson);
                    TextView textAmount = findViewById(R.id.textBudgetAmount);
                    textAmount.setText("");
                    text.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getGroups();
    }

    public void onStart() {
        super.onStart();
        Spinner wishlist = findViewById(R.id.spinnerGroups);
        wishlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final AdapterView<?> tempParent = parent;
                final int tempPosition = position;
                if (!tempParent.getItemAtPosition(position).toString().equals("Select Group To View")) {
                    showDetails(tempParent.getItemAtPosition(position).toString());
                } else {
                    showDetails("clear");
                    findViewById(R.id.textPersonTitle).setVisibility(View.INVISIBLE);
                    findViewById(R.id.textBudgetTitle).setVisibility(View.INVISIBLE);
                    TextView text = findViewById(R.id.textPerson);
                    TextView textAmount = findViewById(R.id.textBudgetAmount);
                    textAmount.setText("");
                    text.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getGroups();
    }

    void getGroups(){
        //Get groups from database and split them with details
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!groupNames.contains("Select Group To View")) {
                    groupNames.add("Select Group To View");
                }
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        if(!groupNames.contains(dsp.child("Name").getValue().toString())) {
                            groupNames.add(dsp.child("Name").getValue().toString());
                        }
                    }
                }
                addToList(groupNames);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void showDetails(final String selected){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("groups").child(selected).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String personsUID = dataSnapshot.child("Your Secret Person").getValue().toString();
                    String budget = dataSnapshot.child("Budget").getValue().toString();
                    updateAdapter(personsUID, selected, budget);
                }else{
                    updateAdapter("none", "none", "none");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateAdapter(String personsUID, final String selected, final String budget){
        final TextView noItems = findViewById(R.id.textNoItems);
        if(personsUID.equals("none") && selected.equals("none")){
            recyclerView = findViewById(R.id.recyclerviewsecretgroup);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(null);
            noItems.setVisibility(View.INVISIBLE);
        }else {
            //Get and Populate the Adapter with user's wishlist
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("users").child(personsUID).child("wishlist").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        noItems.setVisibility(View.VISIBLE);
                    }else{
                        noItems.setVisibility(View.INVISIBLE);
                    }
                    findViewById(R.id.textPersonTitle).setVisibility(View.VISIBLE);
                    findViewById(R.id.textBudgetTitle).setVisibility(View.VISIBLE);
                    recyclerView = findViewById(R.id.recyclerviewsecretgroup);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(new AdapterWishlist(getApplicationContext(), details));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //Collect User's Name
            database.getReference("users").child(personsUID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("Username").getValue().toString();
                    TextView text = findViewById(R.id.textPerson);
                    TextView Budget = findViewById(R.id.textBudgetAmount);
                    Budget.setText(budget);
                    text.setText(name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    void addToList(List<String> list){
        Spinner groups = findViewById(R.id.spinnerGroups);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        groups.setAdapter(dataAdapter);
    }

    void goToCreateGroup(){
        Intent intent = new Intent(this, createGiftGroup.class);
        startActivity(intent);
    }

    void returnToMain(){
        finish();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    void deleteGroup(){
        //Delete Group For Everyone
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Spinner spin = findViewById(R.id.spinnerGroups);
        final String groupName = spin.getSelectedItem().toString();
        if(!groupName.equals("Select Group To View")) {
            database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        if (dsp.child("groups").child(groupName).exists()) {
                            dsp.child("groups").child(groupName).getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            groupNames.remove(groupName);
            addToList(groupNames);
            spin.setSelection(0);
            recyclerView = findViewById(R.id.recyclerviewsecretgroup);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(null);
        }else{
            Toast.makeText(getApplicationContext(), "Select A Group To Delete!", Toast.LENGTH_SHORT).show();
        }
    }
}
