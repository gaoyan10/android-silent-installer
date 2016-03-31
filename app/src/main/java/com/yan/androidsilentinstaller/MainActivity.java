package com.yan.androidsilentinstaller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvTitle;
    private Button btnRoot, btnAccess, btnSelectApk, btnOpenAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.title);
        btnRoot = (Button) findViewById(R.id.btn_root);
        btnAccess = (Button) findViewById(R.id.btn_access);
        btnSelectApk = (Button) findViewById(R.id.btn_selectapk);
        btnOpenAccess = (Button) findViewById(R.id.btn_openaccess);

        btnRoot.setOnClickListener(this);
        btnAccess.setOnClickListener(this);
        btnSelectApk.setOnClickListener(this);
        btnOpenAccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnRoot) {
            rootInstall();
        }
        if (v == btnAccess) {
            accessInstall();
        }
        if (v == btnSelectApk) {
            selectApk();
        }
        if (v == btnOpenAccess) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }
    private void accessInstall() {
        final String strPath = tvTitle.getText().toString();
        if (TextUtils.isEmpty(strPath)) {
            return;
        }
        Uri uri = Uri.fromFile(new File(strPath));
        Intent localIntent = new Intent(Intent.ACTION_VIEW);
        localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(localIntent);
    }
    private static final int FILE_SELECT = 0;
    private void rootInstall() {
        final String strPath = tvTitle.getText().toString();
        if (TextUtils.isEmpty(strPath)) {
            return;
        }
        boolean isRoot = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        if (!isRoot) {
            Toast.makeText(this, "手机没有Root", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(){
            @Override
            public void run() {
                RootInstaller installer = new RootInstaller();
                final boolean result = installer.install(strPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            Toast.makeText(MainActivity.this, "安装成功", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.start();
    }
    private void selectApk() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "选择一个APK"), FILE_SELECT);
        }catch (ActivityNotFoundException e) {
            Toast.makeText(this, "找不到文件浏览器", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == FILE_SELECT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                String path = getPath(this, uri);
                tvTitle.setText(path);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(this, "发生错误", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
