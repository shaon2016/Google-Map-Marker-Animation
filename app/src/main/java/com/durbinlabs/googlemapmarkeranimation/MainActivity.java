package com.durbinlabs.googlemapmarkeranimation;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity {
    Button btnDraggableMarkerActivityDialog, btnDraggableMarkerDialog, btnAnimCar, btnAnimCar2, btnAnimMarker, btnDragMarker, btnDragMarkerAndMoveTheMap, btnPlacePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAnimCar =  findViewById(R.id.btnAnimCar);
        btnAnimCar2 = (Button) findViewById(R.id.btnAnimCar2);
        btnAnimMarker = (Button) findViewById(R.id.btnAnimMarker);
        btnDragMarker = (Button) findViewById(R.id.btnDragMarker);
        btnDraggableMarkerActivityDialog = (Button) findViewById(R.id.btnDraggableMarkerActivityDialog);
        btnDragMarkerAndMoveTheMap = (Button) findViewById(R.id.btnDragMarkerAndMoveTheMap);
        btnPlacePicker =  findViewById(R.id.btnPlacePicker);
        btnDraggableMarkerDialog =  findViewById(R.id.btnDraggableMarkerDialog);

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
                startActivity(new Intent(MainActivity.this, DraggableMarkerActivity.class));
            }
        });

        btnDragMarkerAndMoveTheMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UpdatePositionMovingTheMap.class));
            }
        });

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePicker();
            }
        });
        btnDraggableMarkerDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDraggableMarkerDialog();
            }
        });
        btnDraggableMarkerActivityDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DaggerMarekerActivityDialog.class));
            }
        });
    }

    private void showDraggableMarkerDialog() {
        DraggableMarkerDialogFragment dialog = new DraggableMarkerDialogFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        dialog.show(ft, "draggable marker");
    }

    private int PLACE_PICKER_REQUEST = 1;
    private void showPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
