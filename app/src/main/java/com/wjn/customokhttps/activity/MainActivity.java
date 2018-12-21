package com.wjn.customokhttps.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wjn.customokhttps.R;
import com.wjn.customokhttps.network.RxAndroidOkhttp;

import java.io.InputStream;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RxAndroidOkhttp mRxAndroidOkhttp=null;//RxAndroidOkhttp对象
    private Observable<String> mObservable=null;//get post 方式请求的Observable对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRxAndroidOkhttp=RxAndroidOkhttp.getInstance(MainApplication.getSslSocketFactory(),MainApplication.getX509TrustManager());
        getOkHttp("https://www.12306.cn/index/");
    }

    /**
     * 普通get请求
     * */

    private void getOkHttp(final String geturl){
        if(null!=mRxAndroidOkhttp){
            //observable定义被观察者
            mObservable=mRxAndroidOkhttp.get(geturl);
            if(null!=mObservable){
                //定义观察者
                Subscriber<String> mSubscriber=new Subscriber<String>(){
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TAG","e.getMessage()----:"+e.getMessage());
                        Log.d("TAG","e.getLocalizedMessage()----:"+e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("TAG","s----:"+s);
                    }
                };

                /**
                 * 订阅者关联被观察者
                 * Schedulers.io()说明是输入输出的计划任务
                 * AndroidSchedulers.mainThread()说明订阅者是中ui主线程中执行
                 * */

                mObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mSubscriber);
            }
        }
    }

}
