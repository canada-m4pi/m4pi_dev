package com.m4Pi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;



import butterknife.ButterKnife;
import butterknife.Bind;
import com.m4Pi.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private final String ADDR="http://m4pi.duckdns.org/proc_mobile_signup";
    private Boolean isSelected=false;
    private String name="";
    private String email="";
    private String password="";
    private String reEnterPassword="";
    private String serverResponse="";
    private JSONObject jsonServer;


    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;
    private String gender="";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed("You did not fill out the form.");
            return;
        }

        _signupButton.setEnabled(false);



        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();

        // TODO: Implement your own signup logic here.
        new Login().execute();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed(String msg) {
        Toast.makeText(getBaseContext(), "Login failed: "+msg, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.male:
                if (checked)
                    gender="male";
                    isSelected=true;
                    break;
            case R.id.female:
                if (checked)
                    gender="female";
                    isSelected=true;
                    break;
            case R.id.other:
                if(checked)
                    gender="other";
                    isSelected=true;
                    break;
                    //

        }
    }

    public boolean validate() {
        boolean valid = true;

        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }
        if(!isSelected){
            valid=false;
        } else {

        }

        return valid;
    }

    private class Login extends AsyncTask<String, String, String> {

        ProgressDialog progDailog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(SignupActivity.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        /**
         *
         * @param downloadUrl
         * @return
         */

        @Override
        protected String doInBackground(String... downloadUrl) {
            BufferedReader reader = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(ADDR);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(6 * 10 * 1000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("email", email);
                jsonObject.put("password", password);
                jsonObject.put("gender",gender);
                jsonObject.put("name",name);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                bw.write(jsonObject.toString());
                bw.flush();
                bw.close();
                InputStream inputStream = conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }else {
                    serverResponse = buffer.toString();
                }




            } catch (Exception e) {
                Log.e("Error  ", e.toString());
                serverResponse="Error.";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }

        /**
         * onPostExecute follows the AsyncTask...
         * This will
         * @param unused
         */

        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            progDailog.dismiss();
            _signupButton.setEnabled(true);

            try {
                jsonServer= new JSONObject(serverResponse);
                if(jsonServer.getString("INFO").contentEquals("Success")){
                    onSignupSuccess();
                }
                else if(jsonServer.getString("INFO").contentEquals("User Exists")){
                    onSignupFailed(jsonServer.getString("INFO"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }
}