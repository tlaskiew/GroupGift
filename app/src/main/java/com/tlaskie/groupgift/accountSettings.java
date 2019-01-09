package com.tlaskie.groupgift;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class accountSettings extends AppCompatActivity {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            String username = user.getDisplayName();
            TextView EMAIL = findViewById(R.id.textEmail);
            TextView USERNAME = findViewById(R.id.textUsername);
            EMAIL.setText(email);
            USERNAME.setText(username);

            Button logout = findViewById(R.id.buttonLogout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });
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
        finish();
        FirebaseAuth.getInstance().signOut();
        returnToMain();
    }

}
