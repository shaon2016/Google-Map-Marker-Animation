package com.durbinlabs.googlemapmarkeranimation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnAnimCar, btnAnimCar2, btnAnimMarker, btnDragMarker, btnDragMarkerAndMoveTheMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAnimCar = (Button) findViewById(R.id.btnAnimCar);
        btnAnimCar2 = (Button) findViewById(R.id.btnAnimCar2);
        btnAnimMarker = (Button) findViewById(R.id.btnAnimMarker);
        btnDragMarker = (Button) findViewById(R.id.btnDragMarker);
        btnDragMarkerAndMoveTheMap = (Button) findViewById(R.id.btnDragMarkerAndMoveTheMap);

        btnAnimCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });


        btnAnimMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity2.class));
            }
        });

        btnAnimCar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity3.class));
            }
        });
        btnDragMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DraggableMarker.class));
            }
        });

        btnDragMarkerAndMoveTheMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UpdatePositionMovingTheMap.class));
            }
        });
    }
}
