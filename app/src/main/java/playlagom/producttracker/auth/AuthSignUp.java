package playlagom.producttracker.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import playlagom.producttracker.MapsActivity;
import playlagom.producttracker.R;

public class AuthSignUp extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    Button btnSignUp;
    EditText etSignUpEmail, etSignUpPassword, etName, etPhone;
    Button btnLogin;

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    public static android.widget.RelativeLayout RelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_auth_sign_up);

        RelativeLayout =  findViewById(R.id.relativeLayout1);

        // check internet connection
        if (!isInternetOn()) {
            showSnackbar();
            Toast.makeText(this, "Please ON your internet", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "----onCreate: " + isInternetOn());

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MapsActivity.class));
            finish();
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(getString(R.string.producttracker));
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        etName = findViewById(R.id.etName);
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etPhone = findViewById(R.id.etPhone);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);

        btnSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignUp) {
            if(!isInternetOn()){
                showSnackbar();
            }else{
                registerUser();
            }
        }
        if (v == btnLogin) {
            startActivity(new Intent(AuthSignUp.this, AuthLogin.class));
            finish();
        }
    }

    public void showSnackbar() {

        final Snackbar snackbar = Snackbar
                .make(RelativeLayout, "No internet connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar snackbar1 = Snackbar.make(RelativeLayout,"Retry Login Using Your Connection Again",Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    // Paste this on activity from where you need to check internet status
    public boolean isInternetOn() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;
        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    String newUserName;
    private void registerUser() {
        // Register User
        final String name = etName.getText().toString().trim();
        newUserName = name;
        final String email = etSignUpEmail.getText().toString().trim();
        final String password = etSignUpPassword.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();

        // START: form validation
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show();
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show();
            etSignUpEmail.setError("Email is required");
            etSignUpEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSignUpEmail.setError("Please enter a valid Email");
            etSignUpEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show();
            etSignUpPassword.setError("Password is required");
            etSignUpPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etSignUpPassword.setError("Minimum length of pass should be 6");
            etSignUpPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your Phone number", Toast.LENGTH_LONG).show();
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        } // END: form validation

        progressDialog.setMessage("Registering user...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //notifyAllToBeFriend();

                    // Registration successful and move to profile page
                    Toast.makeText(AuthSignUp.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    databaseReference.child(currentUser.getUid()).child("name").setValue(name);
                    databaseReference.child(currentUser.getUid()).child("email").setValue(email);
                    databaseReference.child(currentUser.getUid()).child("password").setValue(password);
                    databaseReference.child(currentUser.getUid()).child("phone").setValue(phone);
                    databaseReference.child(currentUser.getUid()).child("danger").setValue("0");
                    databaseReference.child(currentUser.getUid()).child("online").setValue("0");

                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(AuthSignUp.this, MapsActivity.class));
                } else {
                    progressDialog.dismiss();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(AuthSignUp.this, "You are already registered!", Toast.LENGTH_LONG).show();
                        // TODO: tvSignIn.setText("Forget password, click to recover.");
                    } else {
                        Toast.makeText(AuthSignUp.this, "Could not register. Try again later", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });
    }

    public static DataSnapshot tempDataSnapshot;
    /*private void notifyAllToBeFriend() {
        // SEND push notification to all about this new user
        databaseReference
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            int counter = 1;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                tempDataSnapshot = snapshot;
                                // [ OK ] data reached
                                Log.d(TAG, "[ OK ] ==== onDataChange: " +
                                        "" +counter++ +
                                        ", " + snapshot.toString());

                                // retrieve friend deviceToken
                                databaseReference
                                        .child(snapshot.getKey())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // [ OK ] data reached
                                                if (dataSnapshot != null) {
                                                    if (dataSnapshot.hasChild(getString(R.string.deviceToken))) {
                                                        // [ OK ] data reached
                                                        Log.d(TAG, "[ OK ] =====token: " + newUserName +
                                                                ", friendKey: " + tempDataSnapshot.getKey() + "" +
                                                                ", " +
                                                                "deviceToken: " + dataSnapshot.child(getString(R.string.deviceToken)).getValue().toString());

                                                        String deviceToken = dataSnapshot.child(getString(R.string.deviceToken)).getValue().toString();
                                                        notifyAllPushNotification(deviceToken);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                            counter = 0;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "[ ERROR ] -------- notifyFriendsOnline.onCancelled: " +
                                "" + databaseError.getMessage());
                    }
                });
    }*/

    public static final String SERVER_KEY = "AAAAzSZNbUY:APA91bE-g_vgALMF4u9mqC2rVbPVi_FkiVtXFi3SiK7ya802mWMLUkIxeatHaxTcZfBnQPacCwJUYQoRXSqA6fBF2vJ_zEsfKruxXdxnTYyuKDgB6uVteHJOumJm5-NLYUqRuyZXq4R7";
    // src: https://stackoverflow.com/questions/39068722/post-ing-json-request-to-fcm-server-isnt-working
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization","key=" + SERVER_KEY)
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    private void notifyAllPushNotification(String deviceToken) {
        Log.d(TAG, "sendOnlinePushNotification: data: " + deviceToken);

        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject param = new JSONObject();
            param.put("body", newUserName + " (NEW USER)");
            param.put("title", "Find " + newUserName + " at map");
            param.put("sound", "default");
            jsonObject.put("notification", param);
            jsonObject.put("to", deviceToken);
            post("https://fcm.googleapis.com/fcm/send", jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                    //Something went wrong
                    Log.d("test", "onFailure: FAILED........");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        Log.d("Response", responseStr);
                        // Do what you want to do with the response.
                    } else {
                        // Request not successful
                    }
                }
                    }
            );
        } catch (JSONException ex) {
            Log.d("Exception", "JSON exception", ex);
        }
    }
}