package com.guoliuya.sharePreferencesCache;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements OkHttpRequestHelper.HttpCallBack {
    private OkHttpRequestHelper mOk;
    private String mUrl = "your url";//请求的url换成你自己的ur，我已经用正常的url测试过了，TestBean的内容换成你自己的
    private ImageView mIv;
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIv = (ImageView) findViewById(R.id.iv);
        mTv = (TextView) findViewById(R.id.tv);
        mOk = new OkHttpRequestHelper(this,this);
        loadData();
    }

    private void loadData() {
        mOk.getAsString(mUrl);
    }

    @Override
    public void onSuccess(String info) {
        TestBean testBean = new Gson().fromJson(info, TestBean.class);
        Picasso.with(this).load(testBean.getData().getNormalLiveList().get(0).getChannelImg()).into(mIv);
        mTv.setText(testBean.getData().getNormalLiveList().get(0).getTitle());
    }

    @Override
    public void onFailure(String info) {
        TestBean testBean = new Gson().fromJson(info, TestBean.class);
        Picasso.with(this).load(testBean.getData().getNormalLiveList().get(0).getChannelImg()).into(mIv);
        mTv.setText(testBean.getData().getNormalLiveList().get(0).getTitle());
    }
}
