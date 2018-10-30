package javeriana.edu.co.firebaseworkshop;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

  private EditText mEmail, mPassword, mName, mLastName;
  private Button signUpButton;

  private static final String TAG = "Signup";

  FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);

    mAuth = FirebaseAuth.getInstance();


    mEmail = findViewById(R.id.txtEmail);
    mPassword = findViewById(R.id.txtPassword);
    mName = findViewById(R.id.txtName);
    mLastName = findViewById(R.id.txtLastName);

    signUpButton = findViewById(R.id.signupinform);

    signUpButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if(validateForm()){
          String email = mEmail.getText().toString();
          String password = mPassword.getText().toString();

          mAuth.createUserWithEmailAndPassword(email, password)
              .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  if(task.isSuccessful()){
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user!=null){ //Update user Info
                      UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                      upcrb.setDisplayName(mName.getText().toString()+" "+mLastName.getText().toString());
                      //upcrb.setPhotoUri(Uri.parse("res/to/pic"));//fake uri, use Firebase Storage
                      user.updateProfile(upcrb.build());
                      startActivity(new Intent(SignupActivity.this, HomeUserActivity.class)); //o en el listener
                    }
                  }
                  if (!task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, R.string.auth_failed+ task.getException().toString(),
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, task.getException().getMessage());
                  }
                }
              });
        }
      }
    });
  }

  public boolean validateForm(){
    boolean valid = true;
    String email = mEmail.getText().toString();
    if (TextUtils.isEmpty(email)) {
      mEmail.setError("Required.");
      valid = false;
    } else if (!isEmailValid(email)) {
      mEmail.setError("Email is not valid.");
      valid = false;
    } else {
      mEmail.setError(null);
    }

    String password = mPassword.getText().toString();
    if (TextUtils.isEmpty(password)) {
      mPassword.setError("Required.");
      valid = false;
    } else {
      mPassword.setError(null);
    }

    String name = mName.getText().toString();
    if (TextUtils.isEmpty(name)) {
      mName.setError("Required.");
      valid = false;
    } else {
      mName.setError(null);
    }

    String lastName = mLastName.getText().toString();
    if (TextUtils.isEmpty(lastName)) {
      mLastName.setError("Required.");
      valid = false;
    } else {
      mLastName.setError(null);
    }

    return valid;
  }

  private boolean isEmailValid(String email) {
    boolean isValid = true;
    if (!email.contains("@") || !email.contains(".") || email.length() < 5) {
      isValid = false;
      Toast.makeText(SignupActivity.this, R.string.bad_email_format,
          Toast.LENGTH_SHORT).show();
    }
    return isValid;
  }
}
