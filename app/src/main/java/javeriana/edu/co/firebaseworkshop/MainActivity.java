package javeriana.edu.co.firebaseworkshop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "Authentication";
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private EditText mUser;
  private EditText mPassword;
  private Button mLoginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mUser = findViewById(R.id.userEmailText);
    mPassword = findViewById(R.id.userPassword);
    mLoginButton = findViewById(R.id.loginButton);

    mLoginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (validateForm()) {
          String email = mUser.getText().toString();
          String password = mPassword.getText().toString();
          mAuth.signInWithEmailAndPassword(email, password)
              .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                  if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                    Toast.makeText(MainActivity.this, R.string.auth_failed,
                        Toast.LENGTH_SHORT).show();
                    mUser.setText("");
                    mPassword.setText("");
                  } else {
                    Intent intent = new Intent(MainActivity.this, HomeUserActivity.class);
                    startActivity(intent);
                  }
                }
              });
        }
      }
    });

    mAuth = FirebaseAuth.getInstance();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
          Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
          startActivity(new Intent(MainActivity.this, HomeUserActivity.class));
        } else {
          // User is signed out
          Log.d(TAG, "onAuthStateChanged:signed_out");
        }
      }
    };
  }

  public void fancyMethod(View view) {

  }

  private boolean validateForm() {

    boolean valid = true;
    String email = mUser.getText().toString();
    if (TextUtils.isEmpty(email) && !isEmailValid(email)) {
      mUser.setError("Required.");
      valid = false;
    } else if (!isEmailValid(email)) {
      mUser.setError("Email is not valid.");
      valid = false;
    } else {
      mUser.setError(null);
    }
    String password = mPassword.getText().toString();
    if (TextUtils.isEmpty(password)) {
      mPassword.setError("Required.");
      valid = false;
    } else {
      mPassword.setError(null);
    }

    return valid;
  }

  private boolean isEmailValid(String email) {
    boolean isValid = true;
    if (!email.contains("@") || !email.contains(".") || email.length() < 5) {
      isValid = false;
      Toast.makeText(MainActivity.this, R.string.bad_email_format,
          Toast.LENGTH_SHORT).show();
    }
    return isValid;
  }

  @Override
  protected void onStart() {
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

}
