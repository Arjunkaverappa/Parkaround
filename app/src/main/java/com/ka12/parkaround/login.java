package com.ka12.parkaround;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.chaos.view.PinView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/*
      https://github.com/firebase/snippets-android/blob/3f7aaf35b187b20cacc2de1df9cf17064389193a/auth/app/src/main/java/com/google/firebase/quickstart/auth/PhoneAuthActivity.java#L104-L111
 */
/*
   shared preferences to save all the data
   use the check network class
   use loading animations
   notify users before opening the browser
   create a helper class if required

   bugs:
   App crashes when there is a predefined number with a otp in the firebase since
   varificationID is only set when the onCodeSent callback is invoked.
 */

public class login extends AppCompatActivity {
    //the following are for the firebase authentication
    public FirebaseAuth mAuth;
    public String mVarificationId;
    public PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    public PhoneAuthProvider.ForceResendingToken mResendToken;
    public String TAG = "login_activtiy";
    LinearLayout linear_lay;
    CardView login_card, signup_card, get_otp_card, otp_card;
    Button submit_number, check_otp;
    PinView otp_number;
    TextInputEditText number, user_name;
    Boolean is_connected = true;
    //the following are for firebase realtime database
    DatabaseReference reference;
    FirebaseDatabase firebaseDatabase;
    //sharedpreference
    public static final String LOGIN = "com.ka12.parkaround.this_is_where_login_details_are_saved";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        linear_lay = findViewById(R.id.linear_lay);
        login_card = findViewById(R.id.login_card);
        signup_card = findViewById(R.id.signup_card);
        get_otp_card = findViewById(R.id.get_otp_card);
        submit_number = findViewById(R.id.submit_number);
        otp_card = findViewById(R.id.otp_card);
        otp_number = findViewById(R.id.otp_number);
        check_otp = findViewById(R.id.check_otp);
        number = findViewById(R.id.number);
        user_name = findViewById(R.id.user_name);

        //setting up action bar and status bar, hiding the otp cards
        set_up_action_and_status_bar();

        //checking the network connectivity
        check_network();

        Log.d(TAG, "onCreate: system initiated");

        //setting up onclick listeners for login and signup
        login_card.setOnClickListener(v -> {
            get_otp_card.setVisibility(View.VISIBLE);
            //hiding the login and signup card
            login_card.setVisibility(View.GONE);
            signup_card.setVisibility(View.GONE);
        });
        signup_card.setOnClickListener(v -> Toast.makeText(login.this, "signup!!!", Toast.LENGTH_SHORT).show());

        //onclick listenres for the login process
        submit_number.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: clicked submit_number button");
            if (Objects.requireNonNull(number.getText()).toString().equals("") || number.getText().toString().length() < 9) {
                Toast.makeText(login.this, "Please check the number", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCreate: Error by the user");
            } else {
                get_otp_card.setVisibility(View.GONE);
                otp_card.setVisibility(View.VISIBLE);
                //calling the otp varification function
                verify_phone_number_step_one(number.getText().toString().trim());
            }
        });

        //redirecting to next activity
        check_otp.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: check otp button clicked");
            String code = Objects.requireNonNull(otp_number.getText()).toString().trim();
            if (code.equals("") || code.length() < 6) {
                Toast.makeText(this, "please check the otp", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCreate: Error in entered otp");
            } else {
                verify_phone_with_code(mVarificationId, code);
            }
        });

        //initialising the firebase authentication
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d(TAG, "onCodeSent: initiated onCodeSent in callback");
                // The SMS verification code has been sent to the provided phone number
                mVarificationId = verificationId;
                mResendToken = forceResendingToken;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                AlertDialog.Builder alert = new AlertDialog.Builder(login.this);
                alert.setTitle("Timeout");
                alert.setMessage("We havent detected any OTP from your device. please check your entered phone numberand try again.");
                alert.setPositiveButton("Resend OTP", (dialog, which) -> {
                    //resending the verification code
                    resendVerificationCode(Objects.requireNonNull(number.getText()).toString(), mResendToken);
                }).setNegativeButton("Exit", (dialog, which) -> {
                    //the negative method goes here
                    System.exit(0);
                });
                alert.show();
                Log.d(TAG, "onCodeSent: initiated onCodeAutoRetrivalTimeOut in callback");
                Toast.makeText(login.this, "Timeout", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //completed
                Log.d(TAG, "onCodeSent: initiated onVerificationCompleted in callback");
                signInWithPhoneNumber_final(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //if the phone number is not valid or etc
                Log.d(TAG, "onCodeSent: initiated onVerification failed in callback");
                Log.d(TAG, "onCodeSent: Error : " + e.getMessage());
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(login.this, "Invalid request", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(login.this, "SMS quota exceeded", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseApiNotAvailableException) {
                    Toast.makeText(login.this, "API not available", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(login.this, "Invalid user exception", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //call back ends here
    }

    //the following method is used build an option with firebase to varify
    public void verify_phone_number_step_one(String phoneNumber) {
        Log.d(TAG, "verify_phone_number_step_one: initiated step one");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91" + phoneNumber)
                .setTimeout(90L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verify_phone_with_code(String verificationId, String code) {
        Log.d(TAG, "verify_phone_with_code: initated verify_phone_with_code");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneNumber_final(credential);
    }

    public void signInWithPhoneNumber_final(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneNumber_final: initialised final step");
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                //get the data inot realtime database
                Log.d(TAG, "signInWithPhoneNumber_final: SUCCESS");
                get_the_data_into_database();
                /*

                 */
                finish();
            } else {
                Log.d(TAG, "signInWithPhoneNumber_final: ERROR: " + task.getException());
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(login.this, "Invalid request", Toast.LENGTH_SHORT).show();
                } else if (task.getException() instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(login.this, "SMS quota exceeded", Toast.LENGTH_SHORT).show();
                } else if (task.getException() instanceof FirebaseApiNotAvailableException) {
                    Toast.makeText(login.this, "API not available", Toast.LENGTH_SHORT).show();
                } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(login.this, "Invalid user exception", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(90L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void get_the_data_into_database() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("USERS").child("name");
        String username = Objects.requireNonNull(user_name.getText()).toString().trim();
        String user_number = Objects.requireNonNull(number.getText()).toString().trim();
        reference.child(user_number).setValue(username).addOnCompleteListener(task -> {
            Intent go = new Intent(login.this, MainActivity.class);
            startActivity(go);
            Animatoo.animateZoom(login.this);
        }).addOnFailureListener(e -> {
            Log.d(TAG, "get_the_data_into_database: ERROR :" + e.getMessage());
            Toast.makeText(this, "Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
     /*
    public void progress_bar() {
        //  https://github.com/lopspower/CircularProgressBar

    }

      */

    public void set_up_action_and_status_bar() {
        //hiding the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        //changing status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //hiding the otp cards
        get_otp_card.setVisibility(View.GONE);
        otp_card.setVisibility(View.GONE);
    }

    public void check_network() {
        //TODO set up wherever required
        try {
            Log.d("zoom", "checking network");
            new Handler().postDelayed(() ->
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo data_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if ((wifi_conn != null && wifi_conn.isConnected()) || (data_conn != null && data_conn.isConnected())) {
                    is_connected = true;
                } else {
                    is_connected = false;
                    check_network();
                }
            }, 3500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}