package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class createAccount extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user != null) {
            signedIn();
        }
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        if (user != null) {
            signedIn();
        }
    }

    void create(View v) {
        EditText EMAIL = findViewById(R.id.emailCreate);
        EditText PASS = findViewById(R.id.passwordCreate);
        EditText PASSCONFIRM = findViewById(R.id.passwordCreate2);
        EditText USERNAME = findViewById(R.id.usernameCreate);
        final String email = EMAIL.getText().toString();
        final String password = PASS.getText().toString();
        final String username = USERNAME.getText().toString();
        final String passwordConfirm = PASSCONFIRM.getText().toString();

        if(email.isEmpty() || password.isEmpty() || username.isEmpty() || passwordConfirm.isEmpty()){
            Toast.makeText(getApplicationContext(), "Please Fill In Everything!", Toast.LENGTH_LONG).show();
        }else if(username.contains("~")){
            Toast.makeText(getApplicationContext(), "Username Cannot Contain '~'.", Toast.LENGTH_LONG).show();
        }else if(!isValidPassword(password)){
            Toast toast = Toast.makeText(getApplicationContext(), "Password Must Contain 3 Numbers.", Toast.LENGTH_LONG);
            TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
            if(view != null) {
                view.setGravity(Gravity.CENTER);
            }
            toast.show();
        }else if(username.length() >= 10) {
            Toast.makeText(getApplicationContext(), "Username Must Be 10 Or Less Characters.", Toast.LENGTH_LONG).show();
        }else if(password.length() <= 8) {
            Toast.makeText(getApplicationContext(), "Password Must Be 8 Characters Or Longer.", Toast.LENGTH_LONG).show();
        }else if(!password.equals(passwordConfirm)){
                Toast.makeText(getApplicationContext(), "Passwords Don't Match!", Toast.LENGTH_LONG).show();
        }else{
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.child(username).exists()) {
                        createNewAccount(password, email, username);
                    }else{
                        Toast.makeText(getApplicationContext(), "Username Exists!", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    boolean isValidPassword(String item){
        int countDigits = 0;
        for(int i = 0; i < item.length(); i++){
            if(Character.isDigit(item.charAt(i))){
                countDigits++;
            }
        }

        if(countDigits >= 3){
            return true;
        }else {
            return false;
        }
    }

    void signedIn(){
        finish();
        Intent intent = new Intent(this, Hub.class);
        startActivity(intent);
    }

    void createNewAccount(final String password, final String email, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users").child(user.getUid());
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("Username", username);
                            myRef.setValue(userInfo);
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                            user.updateProfile(profileUpdate);
                            //Add to list of current usernames in use
                            myRef = database.getReference("usernames").child(username);
                            myRef.setValue(user.getUid());
                            signedIn();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast toast = Toast.makeText(getApplicationContext(), "Account Creation Failed:\nCheck Account Credentials!", Toast.LENGTH_LONG);
                            TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                            if(view != null) {
                                view.setGravity(Gravity.CENTER);
                            }
                            toast.show();
                        }
                    }
                });
    }
}
