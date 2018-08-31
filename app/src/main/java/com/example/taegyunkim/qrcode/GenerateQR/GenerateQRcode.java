package com.example.taegyunkim.qrcode.GenerateQR;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.example.taegyunkim.qrcode.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


// QR코드 생성을 위한 클래스
public class GenerateQRcode extends AppCompatActivity {
    ImageView imageView;
    Bitmap bitmap;
    EditText edit;
    String content; // 입력 된 문자열 읽기 위한 변수

    public static final int WRITE_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
            }
        }

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.relativeLayOut);
        imageView = (ImageView) findViewById(R.id.iv_generated_qrcode);
        edit = (EditText)findViewById(R.id.edit_QRtext);


        findViewById(R.id.btn_generatorQR).setOnClickListener(new View.OnClickListener() { // generate 버튼 클릭 시
            @Override
            public void onClick(View v) {
                content = ((EditText) findViewById(R.id.edit_QRtext)).getText().toString();
                // UTF-8 변환
                try {
                    content = URLEncoder.encode(content, "UTF-8");
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                if (content.isEmpty()) {
                    Toast.makeText(GenerateQRcode.this, "문자를 입력해주세요", Toast.LENGTH_LONG).show();
                } else {
                    generateQRcode(content);
                }

                // Generate 하면 자판 내려가게끔 설정
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit.getWindowToken(),0);

            }
        });
        rl.setOnClickListener(new View.OnClickListener() { // 레이아웃 클릭 시 자판 내려가기
            @Override
            public void onClick(View view) {
                //TODO Auto-generated method stub

                // 화면 어디서든 클릭하면 자판 내려가게 설정
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit.getWindowToken(),0);
            }
        });

        Button btnSaveQR = (Button)findViewById(R.id.btn_saveQR); // save 버튼 클릭 시
        btnSaveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (bitmap != null) {
                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, content.toString()+".jpg", content.toString() + ".jpg");
                        Snackbar.make(view, "갤러리에 이미지가 저장되었습니다.", Snackbar.LENGTH_LONG).show();
                        // 이후 setAction 으로 이동버튼 누를시 갤러리로 바로 이동하게끔 하면 좋을 듯.
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Not save BMP",Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "file error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generateQRcode(String contents) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            bitmap = toBitmap(qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, 500, 500));
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
