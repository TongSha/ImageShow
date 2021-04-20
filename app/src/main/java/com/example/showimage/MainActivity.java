 package com.example.showimage;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.showimage.network.PersonApi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author ST
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private ImageView showImg;
    private Button showBtn;
    private Button refreshBtn;

    private PersonApi personApi;
    private byte[] picByte;
    private int curPos = 0;
    private int page = 1;
    private ArrayList<Person> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init data
        initData();

        // init controls
        initUI();
    }

    private void initData() {
        personApi = new PersonApi();
        data = new ArrayList<>();
        new PersonTask(page).execute();
    }

    private void initUI() {
        showImg = findViewById(R.id.show_img);
        showBtn = findViewById(R.id.show_btn);
        refreshBtn = findViewById(R.id.refresh_btn);

        showBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_btn:
                if (data != null && !data.isEmpty()) {
                    if (curPos > data.size() - 1) {
                        curPos = 0;
                    }
                    new ImageLoadTask(data.get(curPos).getUrl()).execute();
                    curPos++;
                }
                break;
            case R.id.refresh_btn:
                page++;
                new PersonTask(page).execute();
                curPos = 0;
                break;
            default:
                break;
        }
    }

    public byte[] get(String imageUrl) {
        try {
            // instantiate a URL object
            URL url = new URL(imageUrl);
            // get HttpURLConnection instantiate
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // set and request association attributes
            // request mode
            conn.setRequestMethod("GET");
            // request time
            conn.setConnectTimeout(6000);
            // get response code
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "get: " + conn.getResponseCode());
                // get inputstream
                InputStream in = conn.getInputStream();
                ByteArrayOutputStream baso = new ByteArrayOutputStream();
                // loop read
                byte[] b = new byte[1024];
                int length = -1;
                while ((length = in.read(b)) != -1) {
                    Log.d(TAG, "get: " + length);
                    baso.write(b, 0, length);
                }
                picByte = baso.toByteArray();
                in.close();
                baso.close();
                return picByte;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class PersonTask extends AsyncTask<Void, Void, ArrayList<Person>> {

        private int page;

        public PersonTask(int page) {
            this.page = page;
        }

        @Override
        protected ArrayList<Person> doInBackground(Void... params) {
            return personApi.fetchPerson(page, 10);
        }

        @Override
        protected void onPostExecute(ArrayList<Person> persons) {
            super.onPostExecute(persons);
            data.clear();
            data.addAll(persons);
        }
    }

    private class ImageLoadTask extends AsyncTask<Void, Void, byte[]> {

        private String imageUrl;

        public ImageLoadTask(String imageUrl) {
            this.imageUrl = imageUrl;
        }


        @Override
        protected byte[] doInBackground(Void... voids) {
            return MainActivity.this.get(imageUrl);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            showImg.setImageBitmap(bitmap);
        }
    }
}