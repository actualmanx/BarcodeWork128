package com.dilligaf.barcodework128;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText editTextInput;
    private Button buttonGenerate;
    private ImageView imageViewBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        editTextInput = findViewById(R.id.editTextInput);
        buttonGenerate = findViewById(R.id.buttonGenerate);
        imageViewBarcode = findViewById(R.id.imageViewBarcode);

        // Set onClick listener for the generate button
        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateBarcode();
            }
        });
    }

    private void generateBarcode() {
        String text = editTextInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter text", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.CODE_128, 600, 300);
            imageViewBarcode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error generating barcode", Toast.LENGTH_SHORT).show();
        }
    }
}
