package com.sebekerga.tjee4;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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

    /*String toHammingsCode(String message) {
        int[] coded_message;
        char[] message_array = message.toCharArray();
        String debug = "";
        // We find the number of parity bits required:
        int i = 0, parity_count = 3, j = 0, k = 0;

        while (i < message_array.length) {
            // 2^(parity bits) must equal the current position
            // Current position is (number of bits traversed + number of parity bits + 1).
            // +1 is needed since array indices start from 0 whereas we need to start from 1.
            if (Math.pow(2, parity_count) == i + parity_count + 1) {
                parity_count++;
            } else {
                i++;
            }
        }

        // Length of 'b' is length of original data (a) + number of parity bits.
        coded_message = new int[message_array.length + parity_count];

        // Initialize this array with '2' to indicate an 'unset' value in parity bit locations:
        for (i = 1; i <= coded_message.length; i++) {
            if (Math.pow(2, j) == i) {
                // Found a parity bit location.
                // Adjusting with (-1) to account for array indices starting from 0 instead of 1.
                coded_message[i - 1] = 2;
                j++;
            } else {
                coded_message[k + j] = message_array[k++];
            }
        }
        for (i = 0; i < parity_count; i++) {
            // Setting even parity bits at parity bit locations:
            coded_message[((int) Math.pow(2, i)) - 1] = getParity(coded_message, i);
        }
        debug += "";
        for (int m = 0; m < coded_message.length; m++) {
            switch (coded_message[m]) {
                case 48:
                    debug += "0";
                    break;
                case 49:
                    debug += "1";
                    break;
                case 0:
                    debug += "0";
                    break;
            }

        }
        Log.i("hamming", debug);
        return debug;
    }*/

    String hammingGenerate(String number) {
        String result = "";

        //pa =p1, pb =p2, pc = p3 in Hamming (7,4) Code.
        char pa = '0', pb = '0', pc = '0';

        if (!evenOneChecker(String.valueOf(number.charAt(0))
                + String.valueOf(number.charAt(1))
                + String.valueOf(number.charAt(3)))) {
            pa = '1';
        }
        if (!evenOneChecker(String.valueOf(number.charAt(0))
                + String.valueOf(number.charAt(2))
                + String.valueOf(number.charAt(3)))) {
            pb = '1';
        }
        if (!evenOneChecker(String.valueOf(number.charAt(1))
                + String.valueOf(number.charAt(2))
                + String.valueOf(number.charAt(3)))) {
            pc = '1';
        }
        result +=pa;
        result +=pb;
        result += number.charAt(0);
        result +=pc;
        result += number.charAt(1);
        result += number.charAt(2);
        result += number.charAt(3);
        return result;
    }

    boolean evenOneChecker(String binaryNumber) {

        int oneNumber = 0;
        for (int i = 0; i < binaryNumber.length(); i++) {
            if (binaryNumber.charAt(i) == '1') {
                oneNumber++;
            }
        }
        if (oneNumber % 2 == 0) {
            return true;
        }
        return false;
    }

    static int getParity(int b[], int power) {
        int parity = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] != 2) {
                // If 'i' doesn't contain an unset value,
                // We will save that index value in k, increase it by 1,
                // Then we convert it into binary:

                int k = i + 1;
                String s = Integer.toBinaryString(k);

                // Now if the bit at the 2^(power) location of the binary value of index is 1,
                // Then we need to check the value stored at that location.
                // Checking if that value is 1 or 0, we will calculate the parity value.

                int x = ((Integer.parseInt(s)) / ((int) Math.pow(10, power))) % 10;
                if (x == 1) {
                    if (b[i] == 1) {
                        parity = (parity + 1) % 2;
                    }
                }
            }
        }
        return parity;
    }

    List<String> convertFileToBinary(File file){
        List<String> final_bin = new LinkedList<>();
        byte[] file_array = file.toString().getBytes();

        for(byte i : file_array){
            String binary = "";

            int val = i;
            for (int j = 0; j < 8; i++) {
                binary += ((val & 128) == 0 ? false : true);
                val <<= 1;
            }

            final_bin.add(binary);
        }

        return final_bin;
    }
}
