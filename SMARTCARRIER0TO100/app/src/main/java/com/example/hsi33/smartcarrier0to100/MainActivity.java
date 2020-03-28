package com.example.hsi33.smartcarrier0to100;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageButton buttonOnOff, buttonNFC, buttonGps, buttoncontrol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        buttonOnOff = (ImageButton) findViewById(R.id.on_off);
        buttonNFC = (ImageButton) findViewById(R.id.nfc);
        buttonGps = (ImageButton) findViewById(R.id.trace);
        buttoncontrol = (ImageButton) findViewById(R.id.control);

        buttonNFC.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Boolean bool = false;
                if(bool == false) {
                    bool=true;
                    Toast.makeText(MainActivity.this, "NFC ON", Toast.LENGTH_SHORT).show();
                }
                if(bool == true) {
                    bool=false;
                    Toast.makeText(MainActivity.this, "NFC OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonOnOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), OnOffActivity.class);
                startActivity(intent);
            }
        });
        buttonGps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), GpsLocation.class);
                startActivity(intent);
            }
        });

        buttoncontrol.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), control.class);
                startActivity(intent);
            }
        });
    }

}