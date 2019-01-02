package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class createGiftGroup extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView recyclerViewExclude;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final Random random = new Random();
    private List<String> people = new ArrayList<>();
    private List<String> excludedPeople = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gift_group);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            returnToMain();
        }

        recyclerView = findViewById(R.id.recyclerviewgroup);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        showList();
    }

    public void onStart() {
        super.onStart();

        recyclerView = findViewById(R.id.recyclerviewgroup);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
    }

    void createGroup(View v){
        final List<String> groupMembers = new ArrayList<>();
        final EditText groupName = findViewById(R.id.textGroupName);
        final EditText BUDGET = findViewById(R.id.textBudget);
        final String budget = BUDGET.getText().toString();

        if(budget.isEmpty()){
            Toast.makeText(getApplicationContext(), "Create A Budget!", Toast.LENGTH_SHORT).show();
        }else if(!groupName.getText().toString().isEmpty()) {
            String members = "";
            final FirebaseDatabase database = FirebaseDatabase.getInstance();

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

                //Create Assignments
                List<Integer> assignments = generateAssignments(groupMembers.size(), excludedPeople , groupMembers);
                List<String> secretGroup = new ArrayList<>();
                for (int i = 0; i < assignments.size(); i++) {
                    secretGroup.add(groupMembers.get(i) + "-Has->" + groupMembers.get(assignments.get(i)));
                }

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
                            groupInfo.put("Budget", budget);
                            database.getReference("users").child(UID1).child("groups").child(groupName.getText().toString()).setValue(groupInfo);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                Toast.makeText(getApplicationContext(), "Group Created!", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Group Creation Failed!", Toast.LENGTH_LONG).show();
                reset();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Create A Group Name!", Toast.LENGTH_SHORT).show();
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
                    if(!Friends.equals("")) {
                        String arrayOfFriends[] = Friends.split("~");
                        friends.addAll(Arrays.asList(arrayOfFriends));
                        recyclerView.setAdapter(new AdapterGroup(getApplicationContext(), friends));
                    }else{
                        TextView none = findViewById(R.id.textViewNone);
                        none.setText("No Friends Found!");
                    }
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

    //Return exclusions location in list
    List<String> getPositionOfExclusions(List<String> members, final List<String> Exclusion){
        List<String> excluded = new ArrayList<>();

        for(int i = 0; i < Exclusion.size(); i++) {
            String Current = Exclusion.get(i);
            String[] temp = Current.split("~>");
            excluded.add(members.indexOf(temp[0]) + "~>" + members.indexOf(temp[1]));
        }

        return excluded;
    }

    //Generate Assignments by their int position and shuffle + check until assignment is valid
    List<Integer> generateAssignments(final int size, final List<String> exclusions, final List<String> members) {
        final List<Integer> assignments = generateShuffledList(size);

        while (!areValidAssignments(assignments, exclusions, members)) {
            Collections.shuffle(assignments, random);
        }

        return assignments;
    }

    //Shuffle list
    List<Integer> generateShuffledList(final int size) {
        final List<Integer> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            list.add(i);
        }

        Collections.shuffle(list, random);

        return list;
    }

    //Check to make sure assignments are valid
    boolean areValidAssignments(final List<Integer> assignments, final List<String> exclusions, final List<String> members) {
        List<String> excluded = getPositionOfExclusions(members, exclusions);

        for (int i = 0; i < assignments.size(); i++) {
            //Insert Exclude Checker
            for (int k = 0; k < excluded.size(); k++) {
                String current = excluded.get(k);
                String[] temp = current.split("~>");
                //temp 0 cant be assigned to 1!
                if (i == assignments.get(i)) {
                    return false;
                }else if(i == Integer.parseInt(temp[0])){
                    //Check for exclusions
                    if(assignments.get(i) == Integer.parseInt(temp[1])){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Setup UI
    void exclude(View v){
        String members = "";
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkboxadd));
            String friend = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textfriend)).getText().toString();
            if (current.isChecked()) {
                members = members + friend;
            }
        }

        if(members.equals("")){
            Toast.makeText(getApplicationContext(), "Select Some Friends!", Toast.LENGTH_SHORT).show();
        }else {
            //Friends Selected
            Button button = findViewById(R.id.buttonExclude);
            Button next = findViewById(R.id.buttonNext);
            Button reset = findViewById(R.id.buttonReset);
            reset.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            TextView title = findViewById(R.id.textView8);
            title.setText("Select Gift Rules");
            RecyclerView recyclerview = findViewById(R.id.recyclerviewgroup);
            recyclerViewExclude = findViewById(R.id.recyclerviewExclude);
            recyclerview.setVisibility(View.INVISIBLE);
            recyclerViewExclude.setVisibility(View.VISIBLE);

            List<String> exclude = gatherMembers();
            copy(exclude);

            TextView cur = findViewById(R.id.textNextName);
            cur.setText(people.get(0) + " Can Gift: ");
            exclude.remove(people.get(0));
            people.remove(0);


            //Populate Exclude recyclerView
            populateExclude(exclude);
        }
    }

    //Copy List into people list
    void copy(List<String> list){
        for(int i = 0; i < list.size(); i++){
            people.add(list.get(i));
        }
        people.add(user.getDisplayName());
    }

    //Next person's rules
    void next(View v){
        recyclerViewExclude = findViewById(R.id.recyclerviewExclude);
        TextView textName = findViewById(R.id.textNextName);
        if(people.size() == 0){
            //No More People Left. Display Confirm
            Button next = findViewById(R.id.buttonNext);
            Button confirm = findViewById(R.id.buttonCreateGroup);
            confirm.setVisibility(View.VISIBLE);
            next.setVisibility(View.INVISIBLE);
            textName.setText("");
            TextView title = findViewById(R.id.textView8);
            title.setText("Click To Confirm Group");
            recyclerViewExclude.setVisibility(View.INVISIBLE);
        }else {
            //Collect
            for (int i = 0; i < recyclerViewExclude.getChildCount(); i++) {
                Switch current = (recyclerViewExclude.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.switchExclude));
                String person = ((TextView) recyclerViewExclude.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textFriend)).getText().toString();
                if (!current.isChecked()) {
                    String fix = textName.getText().toString().replace(" Can Gift: ", "");
                    excludedPeople.add(fix + "~>" + person);
                }
                current.setChecked(true);
            }
            //Next Person
            textName.setText(people.get(0) + " Can Gift: ");
            List<String> members = gatherMembers();
            members.remove(people.get(0));
            people.remove(0);
            if(people.size() != 0) {
                //Populate
                populateExclude(members);
            }else{
                //No More People Left. Display Confirm
                Button next = findViewById(R.id.buttonNext);
                Button confirm = findViewById(R.id.buttonCreateGroup);
                confirm.setVisibility(View.VISIBLE);
                next.setVisibility(View.INVISIBLE);
                textName.setText("");
                TextView title = findViewById(R.id.textView8);
                title.setText("Click To Confirm Group");
                recyclerViewExclude.setVisibility(View.INVISIBLE);
            }
        }
    }

    //Fill recycler with list
    void populateExclude(List<String> list){
        recyclerViewExclude = findViewById(R.id.recyclerviewExclude);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerViewExclude.setLayoutManager(manager);
        recyclerViewExclude.setHasFixedSize(true);
        recyclerViewExclude.setAdapter(new AdapterExclude(getApplicationContext(), list));
    }

    //Get all members of a group including user
    List<String> gatherMembers(){
        List<String> members = new ArrayList<>();
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            CheckBox current = (recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.checkboxadd));
            String person = ((TextView) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textfriend)).getText().toString();
            if (current.isChecked()) {
                members.add(person);
            }
        }
        members.add(user.getDisplayName());

        return members;
    }

    void resetActivity(View v){
        reset();
    }

    //Clear entire activity of information
    void reset(){
        EditText GN = findViewById(R.id.textGroupName);
        EditText BUDGET = findViewById(R.id.textBudget);
        Button createButton = findViewById(R.id.buttonCreateGroup);
        Button resetButton = findViewById(R.id.buttonReset);
        Button GRButton = findViewById(R.id.buttonExclude);
        Button next = findViewById(R.id.buttonNext);
        TextView title = findViewById(R.id.textView8);
        TextView canGift = findViewById(R.id.textNextName);
        canGift.setText("");
        GN.setText("");
        BUDGET.setText("");
        title.setText("Select Group Members");
        GRButton.setVisibility(View.VISIBLE);
        createButton.setVisibility(View.INVISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
        people.clear();
        excludedPeople.clear();
        recyclerViewExclude.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        showList();
    }
}
