package com.neodreams.secucheck;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class ConfigActivity extends Activity
{
    public static String SERVERIP = "192.168.100.181";
    public static int SERVERPORT = 19801;
    public static String HTTPPORT = "8880";

    EditText etIP;
    EditText etPort;
    EditText etWebPort;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_config);

        etIP = findViewById(R.id.txtIP);
        etPort = findViewById(R.id.txtPort);
        etWebPort = findViewById(R.id.txtWebPort);

        etIP.setText(ConfigActivity.SERVERIP);
        etPort.setText(String.valueOf(ConfigActivity.SERVERPORT));
        etWebPort.setText(ConfigActivity.HTTPPORT);
    }

    public void onButtonCClicked(View v)
    {
        finish();
    }

    public void onButtonSClicked(View v)
    {
        ConfigActivity.SERVERIP = etIP.getText().toString().trim();
        String tmp = etPort.getText().toString().trim();
        ConfigActivity.SERVERPORT = Integer.parseInt(tmp);
        ConfigActivity.HTTPPORT = etWebPort.getText().toString().trim();

        SharedPreferences SP = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor ed = SP.edit();

        ed.putString("SERVER_IP", ConfigActivity.SERVERIP.trim());
        ed.putInt("SERVER_PORT", ConfigActivity.SERVERPORT);
        ed.putString("HTTPPORT", ConfigActivity.HTTPPORT);

        Log.e("commit", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>1234");
        ed.commit();

        finish();
    }
}
