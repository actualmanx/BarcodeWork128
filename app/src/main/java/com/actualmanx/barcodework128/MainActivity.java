package com.actualmanx.barcodework128;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private Button generateButton;
    private ImageView barcodeImage;

    // Variables for history feature
    private ListView historyListView;
    private ArrayList<String> historyList;
    private ArrayAdapter<String> historyAdapter;
    // Set history limit back to 20
    private static final int HISTORY_LIMIT = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        inputText = findViewById(R.id.inputText);
        generateButton = findViewById(R.id.generateButton);
        barcodeImage = findViewById(R.id.barcodeImage);

        // Initialize history components
        historyListView = findViewById(R.id.historyListView);
        historyList = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        historyListView.setAdapter(historyAdapter);

        // Load history from SharedPreferences
        loadHistory();

        // Handle keyboard action button ("Done"/"Enter")
        inputText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                generateBarcodeIfNeeded();
                return true;
            }
            return false;
        });

        // Set the generate button click listener
        generateButton.setOnClickListener(v -> {
            hideKeyboard();
            generateBarcodeIfNeeded();
        });

        // Handle clicks on history items
        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = historyList.get(position);
            inputText.setText(selectedText);
            generateBarcode(selectedText);
            hideKeyboard();
        });
    }

    // Method to check input and generate barcode
    private void generateBarcodeIfNeeded() {
        String text = inputText.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter text to encode", Toast.LENGTH_SHORT).show();
        } else {
            generateBarcode(text);
        }
    }

    // Method to generate the barcode
    private void generateBarcode(String text) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            int width = Math.max(600, text.length() * 30); // Adjust width based on text length
            int height = 300;
            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.CODE_128, width, height);
            barcodeImage.setImageBitmap(bitmap);
            Toast.makeText(this, "Barcode generated successfully", Toast.LENGTH_SHORT).show();

            // Update history
            addToHistory(text);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating barcode", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to add text to history
    private void addToHistory(String text) {
        // Avoid duplicates and move existing item to top
        if (historyList.contains(text)) {
            historyList.remove(text);
        }
        // Add the new text to the top of the list
        historyList.add(0, text);

        // Ensure history does not exceed the limit
        while (historyList.size() > HISTORY_LIMIT) {
            historyList.remove(historyList.size() - 1);
        }

        // Notify the adapter of data change
        historyAdapter.notifyDataSetChanged();

        // Save history to SharedPreferences
        saveHistory();
    }

    // Method to hide the keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Method to save history using SharedPreferences
    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        editor.putString("historyList", json);
        editor.apply();
    }

    // Method to load history from SharedPreferences
    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("historyList", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> savedList = gson.fromJson(json, type);
        if (savedList != null) {
            historyList.clear();
            historyList.addAll(savedList);

            // Ensure history does not exceed the limit after loading
            while (historyList.size() > HISTORY_LIMIT) {
                historyList.remove(historyList.size() - 1);
            }

            historyAdapter.notifyDataSetChanged();
        }
    }

    // Save history when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        saveHistory();
    }
}
