package com.example.dowloadwebapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Handler h;
    private TextView dowloadedWeb;
    private ImageView dowloadedImage;
    private Button dowloadWebButton;
    private Button dowloadImageButton;
    final String webUrl = "https://www.marca.com/";
    final String imageUrl = "https://s.inyourpocket.com/gallery/113383.jpg";

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dowloadedWeb = findViewById(R.id.dowloadedWeb);
        dowloadedWeb.setMovementMethod(new ScrollingMovementMethod());
        dowloadedImage = findViewById(R.id.dowloadedImage);

        //Check network state
        checkNetwork();

        //Obtain messages from threads
        this.h = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:
                        dowloadedWeb.append(msg.obj.toString());
                        break;
                    case 1:
                        dowloadedImage.setImageBitmap((Bitmap) msg.obj);
                        break;
                }
                super.handleMessage(msg);
            }
        };

        dowloadWebButton = findViewById(R.id.dowloadWebButton);
        dowloadImageButton = findViewById(R.id.dowloadImageButton);

        dowloadWebButton.setOnClickListener(this);
        dowloadImageButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
    }

    public void checkNetwork() {
        //Check network
        ConnectivityManager connectivityManager =  (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                if (networkInfo.getType() == connectivityManager.TYPE_WIFI) {
                    Toast.makeText(this, "Wifi connected!", Toast.LENGTH_SHORT).show();
                }
                if (networkInfo.getType() == connectivityManager.TYPE_MOBILE) {
                    Toast.makeText(this, "Mobile connected!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "No network connected!!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "No network operating!!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {

        if (v == dowloadWebButton) {
            final Thread tr = new Thread() {

                @Override
                public void run() {
                    try {
                        URL url = new URL(webUrl);
                        URLConnection conn = url.openConnection();

                        //Get the response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            Message lmsg;
                            lmsg = new Message();
                            lmsg.obj = line;
                            lmsg.what = 0;
                            MainActivity.this.h.sendMessage(lmsg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            tr.start();
        }

        if (v == dowloadImageButton) {
            final Thread tr = new Thread() {

                @Override
                public void run() {
                    try{
                        URL url = new URL(imageUrl);
                        URLConnection conn = url.openConnection();

                        //Get the response
                        InputStream input = conn.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);

                        //Send bitmap
                        Message lmsg;
                        lmsg = new Message();
                        lmsg.obj = bitmap;
                        lmsg.what = 1;
                        MainActivity.this.h.sendMessage(lmsg);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            tr.start();
        }
    }
}
