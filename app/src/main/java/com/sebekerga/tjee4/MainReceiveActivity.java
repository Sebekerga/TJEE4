package com.sebekerga.tjee4;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainReceiveActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int ZERO_UP = 6050;
    private static final int ZERO_DOWN = 5980;
    private static final int ONE_UP = 4100;
    private static final int ONE_DOWN = 3980;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    TextView tv_message, tv_converted_message;
    Button button_reset, button_convert;
    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recieve);

        button_reset = (Button) findViewById(R.id.button_reset);
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message += "\n";
            }
        });

        button_convert = (Button) findViewById(R.id.button_convert);
        button_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_converted_message.setText(convertMessage(message));
            }
        });

        /*int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);*/
        tv_message = (TextView) findViewById(R.id.recieved_message);
        tv_converted_message = (TextView) findViewById(R.id.converted_message);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                scanForBits();

            }
        }, "AudioRecorder Thread");
        recordingThread.start();


    }

    int BufferElements2Rec = 256; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void scanForBits() {

        short sData[] = new short[BufferElements2Rec];
        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
           // System.out.println("Short wirting to file" + sData.toString());
            int FR = calculate(RECORDER_SAMPLERATE * 2, sData);
            if (FR < ZERO_UP && FR > ZERO_DOWN) {
                message += "0";
            } else if (FR < ONE_UP && FR > ONE_DOWN) {
                message += "1";
            }
            setText();
            Log.i("FR", Integer.toString(FR));
            Log.i("message", message);

        }

    }

    public static int calculate(int sampleRate, short[] audioData) {

        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples - 1; p++) {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0)) {
                numCrossing++;
            }
        }
        float numSecondsRecorded = (float) numSamples / (float) sampleRate;
        float numCycles = numCrossing / 2;
        float frequency = numCycles / numSecondsRecorded;

        return (int) frequency;
    }
    private void setText(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(message);
            }
        });
    }

    String convertMessage(String message){
        String converted_message = "";
        char [] message_array = converted_message.toCharArray();

        for(int i = 0; i < message_array.length - 1; i++){
            if(message_array[i] == '0' && message_array[i + 1] == '1')
                converted_message += "0";
            else if(message_array[i] == '1' && message_array[i + 1] == '0')
                converted_message += "1";
        }

        return converted_message;
    }
}