package com.sebekerga.tjee4;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainSendActivity extends AppCompatActivity {

    private  int duration = 240; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate / 1000;
    private final double sample[] = new double[numSamples];
    private double freq0 = 5000;
    private double freq1 = 10000;

    AudioTrack audioTrack;

    EditText edit_text_data;
    EditText edit_text_fr_zero;
    EditText edit_text_fr_one;
    Button button_send;
    TextView tv_coded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_send);
        tv_coded = (TextView) findViewById(R.id.coded_message);
        tv_coded.setMovementMethod(new ScrollingMovementMethod());
        edit_text_data = (EditText) findViewById(R.id.edit_text_data);
        edit_text_fr_zero = (EditText) findViewById(R.id.edit_text_fr_zero);
        edit_text_fr_one = (EditText) findViewById(R.id.edit_text_fr_one);
        button_send = (Button) findViewById(R.id.button_send);

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(genTone(15000));
                try {
                    Thread.sleep(1000); // Ввод в милисек
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                freq0 = Integer.valueOf(edit_text_fr_zero.getText().toString());
                freq1 = Integer.valueOf(edit_text_fr_one.getText().toString());
                //duration = Integer.valueOf(edit_text_data.getText().toString());
                boolean[] final_massage = genMessage(edit_text_data.getText().toString());

                byte[] sound_zero = genTone(freq0);
                byte[] sound_one = genTone(freq1);

                for(int i = 0; i < final_massage.length; i++){
                    if (final_massage[i])
                        playSound(sound_one);
                    else
                        playSound(sound_zero);
                    try {
                        Thread.sleep(duration - 60); // Ввод в милисек
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                playSound(genTone(15000));
            }
        });
        button_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    audioTrack = null;
                }
                return false;
            }
        });
    }


    byte[] genTone(double freqOfTone) {
        final byte generatedSnd[] = new byte[2 * numSamples];
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>>8);
        }
        return generatedSnd;
    }

    void playSound(byte[] sound) {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sound.length / 2,
                AudioTrack.MODE_STATIC);
        audioTrack.write(sound, 0, sound.length);
        audioTrack.play();
    }

    boolean[] genMessage(String message) {
        char[] message_array = message.toCharArray();
        boolean[] final_message = new boolean[message_array.length * 2];

        for (int i = 0; i < message_array.length * 2; i += 2) {
            if (message_array[i/2] == '1') {
                final_message[i] = true;
                final_message[i + 1] = false;
            } else {
                final_message[i] = false;
                final_message[i + 1] = true;
            }
        }
        return final_message;
    }
}
