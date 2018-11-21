package com.chat.chatsdkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chat.host.GameHost;
import com.chatsdk.MiddleManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.openChatView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开聊天

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initChatView();
    }

    private void initChatView(){
        //设置host
        MiddleManager.initHost(this, new GameHost());

        //初始化中间层
        MiddleManager.init();
    }

}
