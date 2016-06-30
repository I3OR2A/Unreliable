package com.example.i3or2a.unreliable.network.multicast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.i3or2a.unreliable.R;

import java.net.DatagramPacket;

public class MulticastActivity extends AppCompatActivity {

    private MulticastService multicastService;

    private TextView textView;

    private Button buttonSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multicast);

        textView = (TextView) findViewById(R.id.textView);

        multicastService = new MulticastService(getApplicationContext());
        multicastService.setOnReceivedListener(new OnReceivedListener() {
            public void onReceived(MulticastService multicastService, DatagramPacket datagramPacket) {
                textView.setText(textView.getText() + "\n" + new String(datagramPacket.getData()));
            }
        });

        buttonSend = (Button) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                multicastService.send();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        multicastService.start();
    }

    protected void onDestroy() {
        multicastService.release();
        super.onDestroy();
    }
}
