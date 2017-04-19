package com.sebekerga.tjee4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        Button button_send = (Button) findViewById(R.id.button_send);
        Button button_receive = (Button) findViewById(R.id.button_receive);

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseRoleActivity.this, MainSendActivity.class));
            }
        });

        button_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseRoleActivity.this, MainReceiveActivity.class));
            }
        });
    }
}
