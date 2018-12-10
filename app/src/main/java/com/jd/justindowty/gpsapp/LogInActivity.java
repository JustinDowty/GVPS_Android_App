package com.jd.justindowty.gpsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * The initial log in activity as an entry point for the application.
 * This app uses the default Firebase User Auth log in API.
 * Facebook sign in methods commented out, apparent bug with firebase connectivity.
 */
public class LogInActivity extends AppCompatActivity {

    private Button signInButton;
    private CallbackManager mCallbackManager;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        if (ActivityCompat.checkSelfPermission(LogInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LogInActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LogInActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        mAuth = FirebaseAuth.getInstance();
        LoginManager.getInstance().logOut();
        signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener((View v) -> createSignInIntent());
        mCallbackManager = CallbackManager.Factory.create();
        /*LoginButton loginButton = findViewById(R.id.buttonFacebookLogin);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LogInActivity.this, "FB Success.",
                        Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LogInActivity.this, "FB Error",
                        Toast.LENGTH_SHORT).show();
                // ...
            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /*private void handleFacebookAccessToken(AccessToken token) {
        Toast.makeText(LogInActivity.this, "" + token.getUserId(),
                Toast.LENGTH_SHORT).show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LogInActivity.this, "Sign in succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LogInActivity.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(LogInActivity.this, "handleFB Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }*/

    /**
     * Checks that location permitted is granted first, if it is not then permission is requested, if it is then app continues
     */
    public void createSignInIntent() {
        if (ActivityCompat.checkSelfPermission(LogInActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LogInActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LogInActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(true)
                            .setLogo(R.drawable.gvps_logo_slim)
                            .setTheme(R.style.FirebaseTheme)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    // Get back user if log in successful
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Intent i = new Intent(LogInActivity.this, MainMenuActivity.class);
                startActivity(i);

            } else {
                // TOAST if log in fails
                Toast.makeText(LogInActivity.this, "Sign in failed",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /* UNUSED */
    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    public void delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_delete]
    }
}
