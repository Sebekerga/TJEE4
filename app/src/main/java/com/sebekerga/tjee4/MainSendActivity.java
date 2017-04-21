package com.sebekerga.tjee4;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
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

    private int duration = 180; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate / 1000;
    private final double sample[] = new double[numSamples];
    private double freq0 = 5000;
    private double freq1 = 10000;

    AudioTrack audioTrack;

    EditText edit_text_data;
    EditText edit_text_fr_zero;
    EditText edit_text_fr_one;
    Button button_send;
    Button button_send_file;
    TextView tv_coded;

    int REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL = 0;
    private static final int READ_REQUEST_CODE = 42;

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
        button_send_file = (Button) findViewById(R.id.button_send_file);

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    Thread.sleep(1000); // Ввод в милисек
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }*/
                freq0 = Integer.valueOf(edit_text_fr_zero.getText().toString());
                freq1 = Integer.valueOf(edit_text_fr_one.getText().toString());
                //duration = Integer.valueOf(edit_text_data.getText().toString());

                String s = edit_text_data.getText().toString(), sn = "";
                String s0 = "";
                if (s.length() % 4 != 0) {
                    int n = 4 - (s.length() % 4);
                    for (int i = 0; i < n; i++) {
                        s0 += "0";
                    }
                    s = s0 + s;
                }
                for (int i = 0; i < s.length(); i += 4) {
                    sn += hammingGenerate(s.substring(i, i + 4));
                }
                tv_coded.setText(sn);
                boolean[] final_massage = genMessage(sn);

                byte[] sound_zero = genTone(freq0);
                byte[] sound_one = genTone(freq1);

                for (int i = 0; i < final_massage.length; i++) {
                    if (final_massage[i])
                        playSound(sound_one);
                    else
                        playSound(sound_zero);
                    try {
                        Thread.sleep(duration - 60); // Ввод в милисек
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    audioTrack.release();
                }
            }
        });
        button_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    audioTrack = null;
                }
                return false;
            }
        });

        button_send_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();

                try {
                    Thread.sleep(1000); // Ввод в милисек
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                freq0 = Integer.valueOf(edit_text_fr_zero.getText().toString());
                freq1 = Integer.valueOf(edit_text_fr_one.getText().toString());

                byte[] sound_zero = genTone(freq0);
                byte[] sound_one = genTone(freq1);

                for (String i : convertFileToBinary(new File(uri.getPath()))) {

                    String s0 = "", sn = "";
                    if (i.length() % 4 != 0) {
                        int n = 4 - (i.length() % 4);
                        for (int m = 0; m < n; m++) {
                            s0 += "0";
                        }
                        i = s0 + i;
                    }
                    for (int j = 0; j < i.length(); j += 4) {
                        sn += hammingGenerate(i.substring(j, j + 4));
                    }
                    boolean[] mes = genMessage(sn);

                    for (int j = 0; j < i.length(); j++) {
                        if (mes[j])
                            playSound(sound_one);
                        else
                            playSound(sound_zero);
                        try {
                            Thread.sleep(duration - 60); // Ввод в милисек
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        audioTrack.release();
                    }
                }


            }
        }
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
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
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
            if (message_array[i / 2] == '1') {
                final_message[i] = true;
                final_message[i + 1] = false;
            } else {
                final_message[i] = false;
                final_message[i + 1] = true;
            }
        }
        return final_message;
    }

    List<String> convertFileToBinary(File file) {
        List<String> final_bin = new LinkedList<>();
        byte[] file_array = file.toString().getBytes();

        for (byte i : file_array) {
            String binary = "";

            int val = i;
            for (int j = 0; j < 8; j++) {
                binary += ((val & 128) == 0 ? "0" : "1");
                val <<= 1;
            }

            final_bin.add(binary);
        }

        return final_bin;
    }
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
}