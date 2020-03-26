package com.example.lipin.json;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class contentActivity extends AppCompatActivity {

    private String strPic;
    private String strContent;
    private UIHandler uiHandler;
    private Bitmap bitmap;

    @BindView(R.id.content_img) ImageView img;
    @BindView(R.id.content_content) TextView content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        uiHandler = new UIHandler();

        //抓取上個畫面的兩個值
        strPic = getIntent().getStringExtra("pic");
        strContent = getIntent().getStringExtra("content");
        Log.v("brad",strPic);

        content.setText(strContent);
        fetchImage();
    }
    private void fetchImage(){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL(strPic);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    //將圖片轉成BitMap
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    uiHandler.sendEmptyMessage(0);

                } catch (Exception e) {

                }
            }
        }.start();
    }
    private class UIHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            img.setImageBitmap(bitmap);
        }
    }
}
