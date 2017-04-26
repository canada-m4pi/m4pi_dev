package com.m4Pi;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CreateActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecord";
    private static int MILLIS_IN_MINUTE = 60*1000;
    private int bpm=80, currentBeat=1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    FileOutputStream outputStream;

    private Button mRecordButton=null;
    private MediaRecorder mRecorder = null;
    Boolean mStartRecording=true;

    private Button mPlayButton=null;
    private MediaPlayer   mPlayer = null;
    Boolean mStartPlaying=true;

    private SeekBar bpmSeek=null;
    private TextView textView=null, bpm_text=null,displayBeat=null;
    private Button sendBeat=null;

    private Timer mainTimer=null;
    private MyTimerTask mainTimerTask=null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private String serverResponse="", Title="",email="";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_create);

        // Record to the external cache directory for visibility
        //mFileName = getExternalCacheDir().getAbsolutePath();
        //mFileName += "/audiorecordtest.3gp";

        mRecordButton=(Button)findViewById(R.id.record);
        mPlayButton=(Button)findViewById(R.id.playback);
        sendBeat=(Button)findViewById(R.id.send_song);
        textView=(TextView)findViewById(R.id.textView2);
        bpm_text=(TextView)findViewById(R.id.text_bpm);
        displayBeat=(TextView)findViewById(R.id.displayBeat);


        sendBeat.setEnabled(false);
        mFileName=getFilesDir()+"/test.m4a";
        Log.d("[ALERT]",mFileName);
        if(-1== ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }


        bpmSeek=(SeekBar)findViewById(R.id.seekBar);
        bpmSeek.incrementProgressBy(10);
        bpmSeek.setMax(120);
        SharedPreferences prefs = getSharedPreferences("LOGIN", MODE_PRIVATE);
        email=prefs.getString("email","-");
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        String currentDateandTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //String split[]=email.split("@");
        Title=email.replaceAll("[-+.^:,@]","")+currentDateandTime;
        Log.d("[EMAIL]",Title);
        setButtons();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(mRecorder!=null) {
                mRecorder.stop();
            }
            if(mPlayer!=null) {
                mPlayer.stop();
            }
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void serverMsg(String mssg, int code) {

        Toast.makeText(getBaseContext(), "Server Says: "+mssg, Toast.LENGTH_LONG).show();
        if(code==0){
            if(mRecorder!=null) {
                mRecorder.stop();
            }
            if(mPlayer!=null) {
                mPlayer.stop();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(CreateActivity.this, ListenActivity.class);
                    intent.putExtra("title", Title);
                    startActivity(intent);
                    CreateActivity.this.finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }, 3500);

        }else{
            sendBeat.setEnabled(true);
        }
           // sendBeat.setEnabled(true);
    }

    private void setButtons(){


        mRecordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                play(mStartRecording);
                if(mStartRecording){
                    textView.setText("Recording...                                                    ");
                }else{
                    textView.setText("Done Recording...                                               ");
                    sendBeat.setEnabled(true);
                }
                mStartRecording=!mStartRecording;

            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onPlay(mStartPlaying);
                if(mStartPlaying){
                    textView.setText("Playing...                                                      ");
                    textView.setSelected(true);
                }else{
                    textView.setText("Done Playing...                                                 ");
                    textView.setSelected(true);
                }
                mStartPlaying=!mStartPlaying;

            }
        });

        sendBeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    sendBeat.setEnabled(false);
                    new SendfeedbackJob().execute();
            }
        });

        bpmSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bpmSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 80;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = 80+ progress;
                bpm_text.setText(""+bpm+" BPM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    textView.setText("Done Playing...                                               ");
                    mStartPlaying=!mStartPlaying;
                }
            });
            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }



    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void play(Boolean playing) {
        //MyTimerTask subTimerTask = new MyTimerTask();
        bpmSeek.setEnabled(!playing);
        if(playing) {
            mainTimerTask = new MyTimerTask();
            mainTimer= new Timer();
            mainTimer.schedule(mainTimerTask, 0, MILLIS_IN_MINUTE / bpm);

        }else{
            mainTimerTask.cancel();
            mainTimer.cancel();
            mainTimer.purge();
            mainTimer=null;
        }

    }

    private void  playSound() {
        int maxVolume=100;
        int currVolume=15;
        float log1=(float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.click2);
        mp.setVolume(1-log1,1-log1);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            };
        });
    }

    private void displayBeatNum(){
        displayBeat.setText("       BEAT: "+currentBeat+"!");
        if(currentBeat<4){
            currentBeat++;
        }else{
            currentBeat=1;
        }
    }

    private void doFileUpload() {

        HttpURLConnection conn=null;
        DataOutputStream dos;
        DataInputStream inStream = null;
        String existingFileName =getFilesDir()+"/test.m4a";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String urlString = "http://m4pi.duckdns.org/upload";

        try {

            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            Log.d("[HTTP]",url.toString());
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP connection to the URL
            Log.d("[HTTP]",conn.toString());
            conn.setInstanceFollowRedirects(true);
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a cached copy.
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"title\""+ lineEnd);
            dos.writeBytes(lineEnd);

            dos.writeBytes(lineEnd);
            dos.writeBytes(Title);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"bpm\""+ lineEnd);
            dos.writeBytes(lineEnd);

            dos.writeBytes(lineEnd);
            dos.writeBytes(""+bpm);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);


            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            Log.e("Debug", "File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }

        //------------------ read the SERVER RESPONSE
        try {

            inStream = new DataInputStream(conn.getInputStream());
            String tmp;

            while ((tmp = inStream.readLine()) != null) {

                Log.e("Debug", "Server Response " + tmp);
                serverResponse=tmp.toString();

            }

            inStream.close();

        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
    }

    private class SendfeedbackJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
                doFileUpload();
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
            if(serverResponse.contentEquals("OKAY")) {
                serverMsg("Your Song was Uploaded!",0);
            }else{
                serverMsg("There was an error uploading your song.",-1);
            }
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            playSound();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayBeatNum();
                }
            });

        }
    }
    


}