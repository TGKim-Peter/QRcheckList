package com.example.taegyunkim.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.renderscript.Sampler;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.taegyunkim.qrcode.Etc.Singleton;
import com.example.taegyunkim.qrcode.DetectQR.ClassifyMachine;
import com.example.taegyunkim.qrcode.Etc.Singleton;
import com.example.taegyunkim.qrcode.GenerateQR.GenerateQRcode;
import com.example.taegyunkim.qrcode.SQLite.ChangeColumn;
import com.example.taegyunkim.qrcode.SQLite.DBHelper;
import com.facebook.stetho.Stetho;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Value;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.taegyunkim.qrcode.GenerateQR.GenerateQRcode.WRITE_STORAGE;

public class MainActivity extends AppCompatActivity {
    private DBHelper helper;
    String dbName = "IngrediDBfile.db";
    //public SQLiteDatabase db;

    RelativeLayout rv;

    Intent classifyString;
    Button btnGenerateClick;
    Button btnChangeColumns;

    // 버튼 리스너 연결
    Button btnSaveData;
    String returnQRValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

         rv = (RelativeLayout) findViewById(R.id.relativeLayOut);

        btnGenerateClick = (Button) findViewById(R.id.btn_generateQR);
        btnGenerateClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GenerateQRcode.class);
                startActivity(intent); // GenerateQRcode 로 이동
            }
        });
        btnChangeColumns = (Button) findViewById(R.id.btn_changeColumn);
        btnChangeColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getColumnList();
                Intent intent = new Intent(MainActivity.this, ChangeColumn.class);
                startActivity(intent);
            }
        });
        btnSaveData = (Button) findViewById(R.id.btn_SaveData);
        btnSaveData.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                saveDB();
            }
        });

        // sharedPreference로 array[0]부터 저장할것
        String[] column = {"회화로_좌", "회화로_좌_explain", "회화로_우", "회화로_우_explain", "회화로_킬달용", "회화로_킬달용_explain", "Hotplate_회화로옆", "Hotplate_회화로옆_explain", "Hotplate_제당좌", "Hotplate_제당좌_explain", "Hotplate_제당우", "Hotplate_제당우_explain", "Hotplate_전분6구", "Hotplate_전분6구_explain", "Water_bath_청신", "Water_bath_청신_explain", "Water_bath_Advantec", "Water_bath_Advantec_explain", "Water_bath_가공전분", "Water_bath_가공전분_explain", "AAS", "AAS_explain", "Auto_Clave", "Auto_Clave_explain", "인화성물질보관", "인화성물질보관_explain"};
        saveArray(column, "columnName", getApplicationContext());


        helper = new DBHelper(this, dbName, null, 1);

        String getTempDate = checkTodayDate();
        helper.select(getTempDate);

        if(!Singleton.getInstance().getDateCheck()){
            Singleton.getInstance().setDate();
            helper.insert();
        }
        //helper.insert();

        helper.delete();
    }

    public String checkTodayDate(){
        Date dateTemp = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String getDate = sdf.format(dateTemp);

        return getDate;
    }

    public void detectClick(View v) {
        String getDate = checkTodayDate();
        helper.select(getDate); // PRIMARY KEY Exist check

        // Singleton Datecheck 'True'일 시 그냥 QRcode Recorder 실행
        if (Singleton.getInstance().getDateCheck()) {
            new IntentIntegrator(this).initiateScan();
        } else {
            helper.insert();
            new IntentIntegrator(this).initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            returnQRValue = result.getContents();
            boolean checkColumnValue = checkColumn(returnQRValue);

            if (result != null && resultCode == RESULT_OK && checkColumnValue) {
                try {
                    returnQRValue = result.getContents();
                    returnQRValue = URLDecoder.decode(returnQRValue, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Scanned: " + returnQRValue, Toast.LENGTH_LONG).show();

                classifyString = new Intent(getApplicationContext(), ClassifyMachine.class);
                classifyString.putExtra("result", returnQRValue);
                startActivity(classifyString);
            }
            else if(result != null && resultCode == RESULT_OK && !checkColumnValue){
                Snackbar.make(rv, "찾는 컬럼이 없습니다." ,Snackbar.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "취소", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void saveArray(String[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("columnName", 0);
        SharedPreferences.Editor editor = prefs.edit();

        if(!prefs.contains("columnName_size"))
        {
            editor.putInt(arrayName + "_size", array.length);
            for (int i = 0; i < array.length; i++)
                editor.putString(arrayName + "_" + i, array[i]);

            editor.commit();
        }
    }

    public boolean checkColumn(String columnName){
        boolean returnValue = false;

        SharedPreferences prefb = getSharedPreferences("columnName", MODE_PRIVATE);
        Collection<?> col =  prefb.getAll().values();
        Iterator<?> it = col.iterator();

        while(it.hasNext())
        {
            if(columnName.equals(it.next().toString())){
                returnValue = true;
            }
        }

        return returnValue;
    }

    public void getColumnList()
    {
        ArrayList<String> columnNameList = new ArrayList<String>();
        String tempColumnName = "";

        //키값없이 모든 저장값 가져오기
        SharedPreferences prefb = getSharedPreferences("columnName", MODE_PRIVATE);
        Collection<?> col =  prefb.getAll().values();
        Iterator<?> it = col.iterator();

        while(it.hasNext())
        {
            tempColumnName = it.next().toString();

            if(tempColumnName.equals("26")==false)
            {
                columnNameList.add(tempColumnName);
            }
        }
        Singleton.getInstance().setColumnNameList(columnNameList);
    }

    private void saveDB() {
        //TODO 근데 컬럼명 변경되면 다 변경될거아녀... 그전 값은 그대로고
        //TODO 어카지?
        int countRow = 0;
        int countCell = 0;

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ingredian");

        Row row;
        Cell cell;

        // sheet의 첫번째 Row 생성
        row = sheet.createRow(countRow);

        // ColumnNames 첫 줄에 작성
        String columnNames[] = helper.selectColumn();

        // 분류 항목들 작성
        for(int i=0; i<columnNames.length; i++){
            cell = row.createCell(countCell);
            cell.setCellValue(columnNames[i]);
            countCell++;
        }
        countRow = 0;
        countCell = 0;


        //TODO 셀들 다 가져오기
        helper.select();


        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "Ingredian.xls");
            try {
                FileOutputStream os = new FileOutputStream(file);
                workbook.write(os);

                Snackbar.make(rv,"Download 폴더에 저장되었습니다.",Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.e("확인불가능","확인불가능");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.e("Permission","Permission");
                        } else {
                            Log.e("Permission error","Permission error");
                        }
                    }
                }
                break;
        }
    }
}