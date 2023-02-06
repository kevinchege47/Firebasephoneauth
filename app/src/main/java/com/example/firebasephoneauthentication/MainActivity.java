package com.example.firebasephoneauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.firebasephoneauthentication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    //viewbinding
    private ActivityMainBinding binding;
    //if code send failed,will be used to resend the OTP code.
     private PhoneAuthProvider.ForceResendingToken forceResendingToken;
     private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
     private String mVerificationId; //wild hold OTP verification code
    private static final String TAG = "MAIN_TAG";
    private FirebaseAuth mAuth;
    //Progress Dialog
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.phonelayout.setVisibility(View.VISIBLE);
        binding.codelayout.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        //initialise progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please Wait");

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
// The callback will be invoked in two situations
//                1. Instant verification - phone number can be verified without the
//                need for sending and receiving verification code.
//                2.Auto retrieval, on some devices, Google play services can automatically detect incoming
//                SMS and verify it without user action
                SignInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
//                Invoked when phone number is not valid
            progressDialog.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token               ) {
                super.onCodeSent(verificationId, forceResendingToken);
                //code has been sent to user and we now need to ask user to input the code
//                and construct the code with a verification ID
                Log.d(TAG,"OnCodeSent"+verificationId);
                mVerificationId = verificationId;
                forceResendingToken = token;
                progressDialog.dismiss();
                binding.phonelayout.setVisibility(View.GONE);
                binding.codelayout.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();
                binding.Tvcodesentdescription.setText("Enter the verification code sent to" + binding.Etphone.getText().toString().trim());

            }
        };
        binding.Btnphonecontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.Etphone.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
                }
                else{
                    startPhoneNumberVerification(phone);
                }

            }
        });
        binding.BtnCodesubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.Etcode.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(MainActivity.this, "Please Enter Verification Code", Toast.LENGTH_SHORT).show();
                }
                else{
                    verifyPhoneNumberwithCode(mVerificationId,code);
                }
            }
        });
        binding.Tvresendcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.Etphone.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
                }
                else{
                    resendVerificationCode(phone,forceResendingToken);
                }
            }
        });

    }
    
    private void resendVerificationCode(String phone,PhoneAuthProvider.ForceResendingToken token) {
        progressDialog.setTitle("Resending Code");
        progressDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void startPhoneNumberVerification(String phone) {
        progressDialog.setTitle("Verifying Phone Number");
        progressDialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void verifyPhoneNumberwithCode(String mVerificationId, String code) {
        progressDialog.setTitle("Verifying Code");
        progressDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,code);
        SignInWithPhoneAuthCredential(credential);
    }

    private void SignInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog.setMessage("Logging You In");
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        String phone = mAuth.getCurrentUser().getPhoneNumber();
                        Toast.makeText(MainActivity.this, ""+phone, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }
}