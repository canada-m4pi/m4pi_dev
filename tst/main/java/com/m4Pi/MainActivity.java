package com.m4Pi;

import java.util.ArrayList;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView txtSpeechInput;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private TextView selected;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        selected=(TextView)findViewById(R.id.Phrase);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);


        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        SharedPreferences prefs = getSharedPreferences("LOGIN", MODE_PRIVATE);
        int isLogged=prefs.getInt("loggedin", 0);
        if (isLogged==0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }


    }

    private void nextSteps(Context context,String msg){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(msg);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        selected.setVisibility(View.INVISIBLE);
        txtSpeechInput.setText("");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String temp=result.get(0);
                    temp.toLowerCase();
                    selected.setVisibility(View.VISIBLE);
                    txtSpeechInput.setText(temp);

                    if(temp.contains("create")) {
                        //Go to Create Music
                        //nextSteps(this,"Starting Up the Creation Process");
                        Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                        startActivityForResult(intent, 0);
                        //finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                    }else if(temp.contains("sign out")||temp.contains("signout")){
                        //Go to Signout
                        //nextSteps(this,"Signing Out!");
                        SharedPreferences.Editor editor = getSharedPreferences("LOGIN", MODE_PRIVATE).edit();
                        editor.putInt("loggedin", 0);
                        editor.putString("email",null);
                        editor.putString("pass",null);
                        editor.commit();
                        Intent intent = new Intent(this, LoginActivity.class);
                        this.startActivity ( intent );
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        finish();
                    }

                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



}