package com.aden.radq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Base64Custom;
import com.aden.radq.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.Objects;

public class MyAccountActivity extends AppCompatActivity {

    //Views
    private EditText etAccountEmail;
    private EditText etAccountPassword;
    private Button btAccountLogin;
    private LinearLayout llPassword;
    private LinearLayout llLoginFields;

    //Firebase
    private FirebaseAuth firebaseAuth;

    //Settings
    private Settings settings;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_account_activity);

        //Initialize views
        Button btCreateAccount = findViewById(R.id.btCreateAccount);
        etAccountEmail = findViewById(R.id.etCreateAccountEmail);
        etAccountPassword = findViewById(R.id.etAccountPassword);
        btAccountLogin = findViewById(R.id.btAccountLogin);
        llPassword = findViewById(R.id.llPassword);
        llLoginFields = findViewById(R.id.llLoginFields);

        //Initialize Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        if (firebaseAuth.getCurrentUser() != null){
            setViewsAsConnected();
        }

        //Load application settings
        settings = new Settings(MyAccountActivity.this);

        btAccountLogin.setOnClickListener(v -> {
            closeKeyboard();
            if (isUserConnected()) {
                validateLogout();
            } else {
                if (etAccountEmail.getText().toString().isEmpty()) {
                    showSnackbar(getString(R.string.snackbar_error_empty_email_field));
                } else if (etAccountPassword.getText().toString().isEmpty()) {
                    showSnackbar(getString(R.string.snackbar_error_empty_password_field));
                } else {
                    validateLogin();
                }
            }
        });

        btCreateAccount.setOnClickListener(v -> openCreateAccountActivity());
    }

    private boolean isUserConnected() {
        return firebaseAuth.getCurrentUser() != null;
    }

    private void setViewsAsConnected(){
        etAccountEmail.setText(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail());
        etAccountEmail.setEnabled(false);

        //Remove Password field and change button description
        ((ViewGroup) llPassword.getParent()).removeView(llPassword);
        btAccountLogin.setText(getText(R.string.button_logout));
    }

    private void validateLogout(){
        firebaseAuth.signOut();
        showSnackbar(getString(R.string.snackbar_logged_out));

        etAccountEmail.setEnabled(true);

        //No user is logged. Overrides the last id in the settings file
        settings.setIdentifierKey("");

        //Add Password field and change button description
        llLoginFields.addView(llPassword);
        btAccountLogin.setText(getText(R.string.button_login));
    }

    private void validateLogin() {
        firebaseAuth.signInWithEmailAndPassword(
                etAccountEmail.getText().toString(),
                etAccountPassword.getText().toString()
        ).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                setViewsAsConnected();

                String accountIdentifier = Base64Custom.encodeBase64(etAccountEmail.getText().toString());
                settings.setIdentifierKey(accountIdentifier);

                showSnackbar(getString(R.string.snackbar_logged_in));
            } else {
                String exceptionError = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                switch (exceptionError){
                    case "ERROR_INVALID_EMAIL":
                        alertDialogBox(getString(R.string.dialog_message_invalid_email));
                        break;
                    case "ERROR_WRONG_PASSWORD":
                        alertDialogBox(getString(R.string.dialog_message_wrong_password));
                        break;
                    case "ERROR_USER_NOT_FOUND":
                        alertDialogBox(getString(R.string.dialog_message_user_not_found));
                        break;
                    default:
                        alertDialogBox(getString(R.string.dialog_message_error_unknown_generic));
                        break;
                }
            }
        });
    }

    private void openCreateAccountActivity(){
        if(isUserConnected()){
            alertDialogBox(getString(R.string.dialog_message_disconnect_before_proceed));
        } else {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        }
    }

    private void closeKeyboard(){
        View view = getCurrentFocus();
        if(view != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clMyAccountActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }

    private void alertDialogBox(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.dialog_title_alert));
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}