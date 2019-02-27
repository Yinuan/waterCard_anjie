package com.klcxkj.watercard_anjie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.klcxkj.reshui.ACT_CardCenter;
import com.klcxkj.reshui.RechagePayBak;
import com.klcxkj.reshui.activity.ACT_ApplyCard_Rechge;
import com.klcxkj.reshui.activity.ACT_Rechage;

public class MainActivity extends AppCompatActivity implements RechagePayBak{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate");
        ACT_Rechage.setPayBak(this);
        ACT_ApplyCard_Rechge.setPayBak(this);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 登录/注册
                Intent intent =new Intent(MainActivity.this, ACT_CardCenter.class);
                intent.putExtra("tellPhoneNum","17728031092");//手机号码
                intent.putExtra("prjRecId","2");//项目ID
                startActivity(intent);
            }
        });
    }

    /**
     *
     * @param info  订单信息
     * @param type  冲值类型（普通冲钱/卡工费）
     * @param monney  冲值大小
     */
    @Override
    public void payBack(String info, String type, String monney) {
        Toast.makeText(this, info + type + monney, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("MainActivity", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
    }
}
