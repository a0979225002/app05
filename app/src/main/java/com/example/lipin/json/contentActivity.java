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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
//                    URL url = new URL(strPic);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.connect();
                    //將圖片轉成BitMap

                    // Load CAs from an InputStream
                    // (could be from a resource or ByteArrayInputStream or ...)
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    // From https://www.washington.edu/itconnect/security/ca/load-der.crt
                    InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
                    Certificate ca;
                    try {
                        ca = cf.generateCertificate(caInput);
                        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                    } finally {
                        caInput.close();
                    }

                    // Create a KeyStore containing our trusted CAs
                    String keyStoreType = KeyStore.getDefaultType();
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("ca", ca);

                    // Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(keyStore);

                    // Create an SSLContext that uses our TrustManager
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);

                    // Tell the URLConnection to use a SocketFactory from our SSLContext
                    URL url = new URL(strPic);
                    HttpsURLConnection urlConnection =
                            (HttpsURLConnection)url.openConnection();
                    urlConnection.setSSLSocketFactory(context.getSocketFactory());
                    InputStream in = urlConnection.getInputStream();
                    copyInputStreamToOutputStream(in);





                } catch (Exception e) {
                    Log.v("brad",e.toString());
                }
            }

            private void copyInputStreamToOutputStream(InputStream in) {
                bitmap = BitmapFactory.decodeStream(in);
                uiHandler.sendEmptyMessage(0);

            }
        }.start();
    }
    private void fetchImage2(){
        RequestQueue queue = Volley.newRequestQueue(this);
        ImageRequest request = new ImageRequest(
                strPic,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        img.setImageBitmap(response);
                    }
                },
                0, 0,
                Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("brad", error.toString());
                    }
                }
        );
        request.setShouldRetryServerErrors(true);
        queue.add(request);
    }
    private class UIHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            img.setImageBitmap(bitmap);
        }
    }
}
