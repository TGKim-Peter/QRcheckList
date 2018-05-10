package com.example.taegyunkim.qrcode;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.taegyunkim.qrcode.DetectQR.ClassifyMachine;
import com.example.taegyunkim.qrcode.GenerateQR.GenerateQRcode;
import com.example.taegyunkim.qrcode.SQLite.DBHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private DBHelper helper;
    String dbName = "IngreDBfile.db";
    public SQLiteDatabase db; // 삭제요망

    Intent classifyString;
    Button btnGenerateClick;
    String temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGenerateClick = (Button)findViewById(R.id.btn_generateQR);
        btnGenerateClick.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), GenerateQRcode.class);
                startActivity(intent); // GenerateQRcode 로 이동
            }
        });

        helper = new DBHelper(this, dbName,null,1);
        try {
            db = helper.getWritableDatabase();
            // PRIMARY KEY 인 날짜 받아오기
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String getDate = sdf.format(date);
            // insert 날짜

            //helper.insert(getDate);
            //helper.addAlter("회화로좌");
            helper.select();
        }catch (SQLiteException e){
            e.printStackTrace();
            finish();
        }
    }
    public void detectClick(View v){
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && resultCode == RESULT_OK){
                try{
                    temp = result.getContents();
                    temp = URLDecoder.decode(temp,"UTF-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                Toast.makeText(this, "Scanned: " + temp, Toast.LENGTH_LONG).show();

                btnGenerateClick.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(v.getContext(), GenerateQRcode.class);
                        startActivity(intent); // GenerateQRcode 로 이동
                    }
                });
                classifyString = new Intent(getApplicationContext(), ClassifyMachine.class);
                classifyString.putExtra("result",result.getContents());
                startActivity(classifyString);
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
