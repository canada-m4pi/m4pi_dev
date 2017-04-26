package com.m4Pi;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.Bind;
import com.m4Pi.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String ADDRESS="http://m4pi.duckdns.org/auth_user_mobile";
    private static final int REQUEST_SIGNUP = 0;
    private String serverResponse="";
    private JSONObject jsonServer;
    private String email;
    private String password;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed("You did not meet the criteria. Try Again.");
            return;
        }

        _loginButton.setEnabled(false);
        email=_emailText.getText().toString();
        password=_passwordText.getText().toString();
        new Login().execute();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        SharedPreferences.Editor editor = getSharedPreferences("LOGIN", MODE_PRIVATE).edit();
        editor.putInt("loggedin", 1);
        editor.putString("email",email);
        editor.putString("pass",password);
        editor.commit();

        _loginButton.setEnabled(true);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed(String errMsg) {
        Toast.makeText(getBaseContext(), "Login failed: "+errMsg, Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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

        return valid;
    }

    private class Login extends AsyncTask<String, String, String> {

        ProgressDialog progDailog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(LoginActivity.this);
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
                URL url = new URL(ADDRESS);
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


            _loginButton.setEnabled(true);

            try {
                jsonServer= new JSONObject(serverResponse);
                if(jsonServer.getString("INFO").contentEquals("Success")){
                    onLoginSuccess();
                }
                else if(jsonServer.getString("INFO").contentEquals("Wrong Password")){
                    onLoginFailed(jsonServer.getString("INFO"));
                }
                else if(jsonServer.getString("INFO").contentEquals("Wrong e-mail")){
                    onLoginFailed(jsonServer.getString("INFO"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //onLoginSuccess();

        }

    }




}
