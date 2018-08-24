package com.ldlywt.androidadvancedemo.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ldlywt.androidadvancedemo.R;
import com.ldlywt.base.fragment.BaseFragment;
import com.ldlywt.easyhttp.Call;
import com.ldlywt.easyhttp.Callback;
import com.ldlywt.easyhttp.HttpClient;
import com.ldlywt.easyhttp.Request;
import com.ldlywt.easyhttp.RequestBody;
import com.ldlywt.easyhttp.Response;
import com.ldlywt.ioc.annomation.event.OnClick;
import com.ldlywt.ioc.annomation.resouces.ViewById;
import com.ldlywt.ioc.manager.InjectManager;

import java.io.IOException;

/**
 * <pre>
 *     author : lex
 *     e-mail : ldlywt@163.com
 *     time   : 2018/08/24
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class HttpFragment extends BaseFragment {

    public static final String TAG = "HttpFragment";

    private HttpClient mHttpClient;

    @ViewById(R.id.tv_show)
    private TextView mTvShow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_http, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InjectManager.inject(this);
        mHttpClient = new HttpClient();
    }

    @OnClick({R.id.bt_get, R.id.bt_post})
    public void open(View v) {
        switch (v.getId()) {
            case R.id.bt_get:
//                Toast.makeText(this, "xxxx", Toast.LENGTH_SHORT).show();
                get();
                break;
            case R.id.bt_post:
                post();
                break;
        }
    }

    private void get() {
        new Thread(() -> {
            Request request = new Request
                    .Builder()
                    .url("http://www.wanandroid.com/banner/json")
                    .build();
            Call call = mHttpClient.newCall(request);
            try {
                final Response response = call.execute();
                Log.i(TAG, "get onResponse: " + response.getBody());
                getActivity().runOnUiThread(() -> mTvShow.setText(response.getBody()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void post() {
        RequestBody body = new RequestBody()
                .add("key", "064a7778b8389441e30f91b8a60c9b23")
                .add("city", "深圳");
        final Request request = new Request
                .Builder()
                .url("http://restapi.amap.com/v3/weather/weatherInfo")
                .post(body)
                .build();
        mHttpClient
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: " + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        Log.i(TAG, "post onResponse: " + response.getBody());
                        getActivity().runOnUiThread(() -> mTvShow.setText(response.getBody()));

                    }
                });
    }
}
