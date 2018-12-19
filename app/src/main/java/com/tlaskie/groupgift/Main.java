package com.tlaskie.groupgift;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Main extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        if (user != null) {
            signedIn();
        }
    }

    void login(View v){

        EditText EMAIL = findViewById(R.id.emailCreate);
        EditText PASS = findViewById(R.id.passwordCreate);
        String email = EMAIL.getText().toString();
        String password = PASS.getText().toString();

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please Fill In Everything!", Toast.LENGTH_LONG).show();
        }else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                user = mAuth.getCurrentUser();
                                Toast.makeText(getApplicationContext(), "Signed-In!", Toast.LENGTH_LONG).show();
                                signedIn();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    void createAccount(View v){
        Intent intent = new Intent(this, createAccount.class);
        startActivity(intent);
    }

    void signedIn(){
        finish();
        Intent intent = new Intent(this, Hub.class);
        startActivity(intent);
    }
}
