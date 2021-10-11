package com.backdoor.qrcodescanner;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MakeQRCodeActivity extends AppCompatActivity {

    private EditText editText;
    private ImageView imageView;
    private Button generateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qrcode);

        init();

        generateBtn.setOnClickListener(v -> generateCode());


    }

    private void generateCode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(editText.getText().toString(), BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        editText = findViewById(R.id.generateEditTxt);
        imageView = findViewById(R.id.generateImageView);
        generateBtn = findViewById(R.id.generateBtn);
    }
}