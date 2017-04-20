package com.sebekerga.tjee4;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainReceiveActivity extends AppCompatActivity {
    private static final boolean DEBUG = true;
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int ZERO_UP = 6050;
    private static final int ZERO_DOWN = 5500;
    private static final int ONE_UP = 4100;
    private static final int ONE_DOWN = 3600;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    TextView tv_message, tv_converted_message, tv_decoded_message;
    Button button_newline, button_convert, button_clear, button_decode;
    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_recieve);

        tv_decoded_message = (TextView) findViewById(R.id.decoded_message);
        tv_message = (TextView) findViewById(R.id.scaned_freq);
        tv_converted_message = (TextView) findViewById(R.id.converted_message);
        tv_message.setMovementMethod(new ScrollingMovementMethod());
        tv_converted_message.setMovementMethod(new ScrollingMovementMethod());
        tv_decoded_message.setMovementMethod(new ScrollingMovementMethod());
        button_newline = (Button) findViewById(R.id.newline_button);
        button_newline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message += "\n";
            }
        });
        button_decode = (Button) findViewById(R.id.button_decode);
        button_decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = String.valueOf(tv_converted_message.getText()), sn = "";
                String s0 = "";
                if (s.length() % 7 != 0) {
                    s = s.substring(0, s.length() - s.length() % 7);
                }
                for (int i = 0; i < s.length(); i += 7) {
                    sn += hammingCheck(s.substring(i, i + 7));
                }
                tv_decoded_message.setText(sn);
            }
        });
        button_clear = (Button) findViewById(R.id.clear_button);
        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = "";
                tv_message.setText("");
                tv_decoded_message.setText("");
                tv_converted_message.setText("");
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

    int BufferElements2Rec = 128; // want to play 2048 (2K) since 2 bytes we use only 1024
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

    private void setText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(message);
            }
        });
    }

    String hammingCheck(String number) {
        String result = "";
        char ca = '0', cb = '0', cc = '0';
        if (!evenOneChecker(String.valueOf(number.charAt(0)) + String.valueOf(number.charAt(2))
                + String.valueOf(number.charAt(4)) + String.valueOf(number.charAt(6)))) {
            ca = '1';
        }
        if (!evenOneChecker(String.valueOf(number.charAt(1)) + String.valueOf(number.charAt(2))
                + String.valueOf(number.charAt(5)) + String.valueOf(number.charAt(6)))) {
            cb = '1';
        }
        if (!evenOneChecker(String.valueOf(number.charAt(3)) + String.valueOf(number.charAt(4))
                + String.valueOf(number.charAt(5)) + String.valueOf(number.charAt(6)))) {
            cc = '1';
        }
        String[] positionOfError = {"000", "100", "010", "110", "001", "101", "011", "111"};
        //String[] encode = {"000", "P1", "P2", "D1", "P3", "D2", "D3", "D4"};
        int index = 0;
        for (int i = 0; i < positionOfError.length; i++) {
            String sm = "";
            sm += ca;
            sm += cb;
            sm += cc;
            if (positionOfError[i].equals(sm)) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            result += String.valueOf(number.charAt(2))
                    + String.valueOf(number.charAt(4))
                    + String.valueOf(number.charAt(5))
                    + String.valueOf(number.charAt(6));
        } else {
            if (number.charAt(index - 1) == '0') {
                number = number.substring(0, index - 1) + '1' + number.substring(index);
            } else {
                number = number.substring(0, index - 1) + '0' + number.substring(index);
            }
            result += String.valueOf(number.charAt(2))
                    + String.valueOf(number.charAt(4))
                    + String.valueOf(number.charAt(5))
                    + String.valueOf(number.charAt(6));
        }
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

    String convertMessage(String message) {
        String converted_message = "";
        char[] message_array = message.toCharArray();
        List<Boolean> bits_list = new LinkedList<>();
        List<List<Boolean>> message_list = new LinkedList<>();

        for (int i = 0; i < message_array.length - 1; i++) {
            bits_list.add(message_array[i] == '1' ? true : false);
            if (message_array[i] != message_array[i + 1]) {
                message_list.add(bits_list);
                bits_list = new LinkedList<>();
            }
        }

        float sum = 0;
        for (List<Boolean> i : message_list) {
            sum += i.size();
        }
        sum /= message_list.size();

        bits_list = new LinkedList<>();
        for (List<Boolean> i : message_list) {
            boolean bit = i.get(0);
            bits_list.add(bit);
            if (i.size() / sum > 1.3) {
                bits_list.add(bit);
            }
        }

        for (int i = 0; i < bits_list.size(); i += 2) {
            if (bits_list.get(i) == true)
                converted_message += '1';
            else
                converted_message += '0';
        }

        return converted_message;
    }

    String decode_and_repair(int recieved_mes[], int parity_count) {
        // This is the receiver code. It receives a Hamming code in array 'a'.
        // We also require the number of parity bits added to the original data.
        // Now it must detect the error and correct it, if any.
        String repaired = "";
        int power;
        // We shall use the value stored in 'power' to find the correct bits to check for parity.

        int parity[] = new int[parity_count];
        // 'parity' array will store the values of the parity checks.

        String syndrome = new String();
        // 'syndrome' string will be used to store the integer value of error location.

        for (power = 0; power < parity_count; power++) {
            // We need to check the parities, the same number of times as the number of parity bits added.

            for (int i = 0; i < recieved_mes.length; i++) {
                // Extracting the bit from 2^(power):

                int k = i + 1;
                String s = Integer.toBinaryString(k);
                int bit = ((Integer.parseInt(s)) / ((int) Math.pow(10, power))) % 10;
                if (bit == 1) {
                    if (recieved_mes[i] == 1) {
                        parity[power] = (parity[power] + 1) % 2;
                    }
                }
            }
            syndrome = parity[power] + syndrome;
        }
        // This gives us the parity check equation values.
        // Using these values, we will now check if there is a single bit error and then correct it.
        String debug = "";
        int error_location = Integer.parseInt(syndrome, 2);
        if (error_location != 0) {
            debug += "\nError is at location " + error_location + ".";
            recieved_mes[error_location - 1] = (recieved_mes[error_location - 1] + 1) % 2;
            debug += "\nCorrected code is:";
            for (int i = 0; i < recieved_mes.length; i++) {
                repaired += recieved_mes[recieved_mes.length - i - 1];
                debug += recieved_mes[recieved_mes.length - i - 1];
            }

            debug += "\n";
        } else {
            debug += "\nThere is no error in the received data.";
        }

        // Finally, we shall extract the original data from the received (and corrected) code:
        debug += "\nOriginal data sent was:";
        power = parity_count - 1;
        for (int i = recieved_mes.length; i > 0; i--) {
            if (Math.pow(2, power) != i) {
                repaired += recieved_mes[i - 1];
                debug += recieved_mes[i - 1];
            } else {
                power--;
            }
        }
        debug += "\n";
        Log.i("hamming", debug);
        return repaired;
    }

}