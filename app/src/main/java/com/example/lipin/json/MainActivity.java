package com.example.lipin.json;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private SimpleAdapter simpleAdapter;//串利調變器
    private String[] from ={"title","typr"};
    private  int[]to ={R.id.item_title,R.id.itme_type};
    private LinkedList<HashMap<String,String>>data = new LinkedList<>();

    @BindView(R.id.listview) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initListView();
        fetchRemoteData();
    }
    private void initListView(){
        //將資料放入調變器內
        simpleAdapter = new SimpleAdapter(this,data,R.layout.item,from,to);
        //將調變器資料給予listView
        listView.setAdapter(simpleAdapter);
        //點擊給予轉到下一頁功能
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //抓取你現在點擊的 是第幾個陣列
                GotoDetail(position);
            }
        });
    }
    private void GotoDetail(int which){
        Intent intent = new Intent(this,contentActivity.class);
        //將點擊的 該陣列內pic與content傳到contentActivity下個畫面去
        intent.putExtra("pic",data.get(which).get("pic"));
        intent.putExtra("content",data.get(which).get("content"));
        startActivity(intent);
    }
    //撈遠端JSON資料
    private void fetchRemoteData(){
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://data.coa.gov.tw/Service/OpenData/RuralTravelData.aspx");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuffer sb = new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    parseJSON(sb.toString());
                } catch (Exception e) {
                    Log.v("brad", e.toString());
                }
            }
        }.start();
    }
    //解析抓下來的JSON
    private void parseJSON(String JSON){
        try {
            JSONArray root = new JSONArray(JSON);
            for (int i=0;i<root.length();i++){
                JSONObject row = root.getJSONObject(i);

                HashMap<String,String> temp = new HashMap<>();
                temp.put(from[0],row.getString("Title"));
                //因為TravelType有\n\r所以需要先使用replace抓出該字元轉成空字元
                //然後將兩個空字元抓處來轉成String的"";
                temp.put(from[1],row.getString("TravelType")
                        //注意:\n不能使用String 只能使用char
                .replace('\n',' ')
                .replace('\r',' ')
                .replace("  ",""));
                temp.put("pic",row.getString("PhotoUrl"));
                temp.put("content",row.getString("Contents"));
                data.add(temp);
            }
                uIhandler.sendEmptyMessage(0);
        } catch (Exception e) {
            Log.v("brad",e.toString());
        }

    }
    private UIhandler uIhandler = new UIhandler();
    private class UIhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //跟調變器說資料已改變
            switch (msg.what){
                case 0:
                    simpleAdapter.notifyDataSetChanged();
                    break;
            }

        }
    }
}
