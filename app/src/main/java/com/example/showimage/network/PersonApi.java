package com.example.showimage.network;

import android.util.Log;
import android.widget.ProgressBar;

import com.example.showimage.Person;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author ST
 * Step 1：通过HttpUrlConnection发起Get请求，然后获得后台返回的数据，此时是流形式的
 * Step 2：我们需要写一个流转成字节数组的方法
 * Step 3：将字节数组转成字符串后，得到的就是后台的给我们返回的数据了，接着要做的就 是写一个解析这一大串Json的方法了，我们需要获取Json里我们需要的数据，丢到Bean里
 * Step 4：返回处理后的集合数据
 */
public class PersonApi {

    private static final String TAG = "NetWork";
    private static final String BASE_URL = "https://gank.io/api/v2/data/category/Girl/type/Girl";

    /**
     * 查询人物信息
     */
    public ArrayList<Person> fetchPerson(int page, int count) {
        String fetchUrl = BASE_URL + "/page/" + page + "/count/" + count;
        ArrayList<Person> persons = new ArrayList<>();
        try {
            URL url = new URL(fetchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                byte[] data = readFromStream(in);
                String result = new String(data, "UTF-8");
                persons = parsePerson(result);
            } else {
                Log.e(TAG, "请求失败: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return persons;
    }

    /**
     * 读取流中数据的方法
     */
    public byte[] readFromStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = -1;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }

    /**
     * 解析返回Json数据的方法
     */
    public ArrayList<Person> parsePerson(String content) throws Exception {
        ArrayList<Person> persons = new ArrayList<>();
        JSONObject object = new JSONObject(content);
        JSONArray array = object.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            JSONObject results = (JSONObject) array.get(i);
            Person person = new Person();
            person.set_id(results.getString("_id"));
            person.setAuthor(results.getString("author"));
            person.setCategory(results.getString("category"));
            person.setCreatedAt(results.getString("createdAt"));
            person.setDesc(results.getString("desc"));
            person.setLikeCounts(results.getInt("likeCounts"));
            person.setPublishedAt(results.getString("publishedAt"));
            person.setStars(results.getInt("stars"));
            person.setTitle(results.getString("title"));
            person.setType(results.getString("type"));
            person.setUrl("https" + results.getString("url").split("http")[1]);
            person.setViews(results.getString("views"));
            persons.add(person);
        }
        return persons;
    }
}
