package cloud.techstar.jisho.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cloud.techstar.jisho.R;
import cloud.techstar.jisho.Utils.ConnectionDetector;
import cloud.techstar.jisho.Utils.JishoConstant;
import cloud.techstar.jisho.database.WordTable;
import cloud.techstar.jisho.models.Words;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private WordTable wordTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        wordTable = new WordTable();

        mHandler = new Handler(Looper.getMainLooper());
        Handler handler = new Handler(Looper.getMainLooper());

        //
        if (!ConnectionDetector.isNetworkAvailable(SplashActivity.this)){
            Toast.makeText(SplashActivity.this, "No internet connections", Toast.LENGTH_LONG).show();
            startActivity(new Intent(SplashActivity.this, MainActivity.class));

        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectServer();
                }
            }, 100);
        }
    }

    public void connectServer() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(JishoConstant.WEB_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("Server connection failed : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                Logger.json(res);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            JSONObject ob = new JSONObject(res);
                            JSONArray memorize = ob.getJSONArray("memorize");

                            Logger.json(memorize.toString());

                            if (memorize.length() > 0) {
                                WordTable wordTable = new WordTable();

                                wordTable.deleteAll();

                                for (int i = 0; i < memorize.length(); i++) {
                                    Words words = new Words();
                                    words.setCharacter(memorize.getJSONObject(i).getString("character"));
                                    words.setMeaning(memorize.getJSONObject(i).getString("meanings"));
                                    words.setKanji(memorize.getJSONObject(i).getString("kanji"));
                                    words.setIsMemorize("false");
                                    words.setLevel(memorize.getJSONObject(i).getString("level"));
                                    wordTable.insert(words);
                                }
                            } else {
                                Toast.makeText(SplashActivity.this, "Words not found", Toast.LENGTH_LONG)
                                        .show();
                            }
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}