package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.helper.AccountHelper;
import com.aden.radq.helper.FirebaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "ContactLoginActivity";

    private EditText contactEmail;
    private EditText contactPassword;
    private Button buttonLoginLogout;

    private AccountHelper accountHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        contactEmail = findViewById(R.id.etContactEmail);
        contactPassword = findViewById(R.id.contactPasswordEditText);
        buttonLoginLogout = findViewById(R.id.contactLoginButton);
        Button buttonCreateAccount = findViewById(R.id.createAccountButton);

        isUserConnected();

        buttonLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isUserConnected()){
                    accountHelper = new AccountHelper();
                    if(contactEmail.getText().toString().isEmpty()){
                        Log.d(TAG,"Email Vazio");
                        Snackbar.make(findViewById(R.id.LoginActivity), "Email vazio", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    } else if(contactPassword.getText().toString().isEmpty()) {
                        Log.d(TAG,"Senha Vazia");
                        Snackbar.make(findViewById(R.id.LoginActivity), "Senha vazia", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    } else {
                        accountHelper.setEmail(contactEmail.getText().toString());
                        accountHelper.setPassword(contactPassword.getText().toString());

                        validateLogin();
                    }
                } else {
                    validateLogout();
                }
            }
        });

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private boolean isUserConnected() {
        FirebaseAuth firebaseAuth = FirebaseHelper.getFirebaseAuth();
        if(firebaseAuth.getCurrentUser() != null){
            contactEmail.setText(firebaseAuth.getCurrentUser().getEmail());
            contactEmail.setEnabled(false);
            contactPassword.setEnabled(false);
            buttonLoginLogout.setText(getText(R.string.logout_button));
            return true;
        }
        return false;
    }

    private void validateLogout(){
        Log.d(TAG,"ValidateLogout()");
        FirebaseAuth firebaseAuth = FirebaseHelper.getFirebaseAuth();
        firebaseAuth.signOut();
        Snackbar.make(findViewById(R.id.LoginActivity), "Logged out", Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
        contactEmail.setEnabled(true);
        contactPassword.setEnabled(true);
        buttonLoginLogout.setText(getText(R.string.login_button));
    }

    private void validateLogin() {
        Log.d(TAG, "ValidateLogin()");
        FirebaseAuth firebaseAuth = FirebaseHelper.getFirebaseAuth();
        firebaseAuth.signInWithEmailAndPassword(
                accountHelper.getEmail(),
                accountHelper.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Snackbar.make(findViewById(R.id.LoginActivity), "Sucesso", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    contactEmail.setEnabled(false);
                    contactPassword.setEnabled(false);
                    buttonLoginLogout.setText(getText(R.string.logout_button));
                } else {
                    Snackbar.make(findViewById(R.id.LoginActivity), "Erro", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });
    }

    private void createAccount(){
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }
}
