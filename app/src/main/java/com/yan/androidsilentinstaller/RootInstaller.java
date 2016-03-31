package com.yan.androidsilentinstaller;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by Yan Gao on 3/31/16.
 */
public class RootInstaller {
    public boolean install(String strApkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorReader = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            String command = "pm install -r " + strApkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null){
                sb.append(line);
            }
            String output = sb.toString();
            Log.d("Installer", "install msg is " + output);
            if (!output.contains("Failure")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorReader != null) {
                    errorReader.close();
                }
            }catch(IOException e) {

            }
        }
        return false;
    }

}
