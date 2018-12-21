package com.wjn.customokhttps.network;

import android.content.Context;

import com.wjn.customokhttps.constant.DataConstant;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by wjn on 2017/11/22.
 * OkHttp框架 封装方法
 */

public class RxAndroidOkhttp {

    private OkHttpClient mOkHttpClient=null;//OkHttpClient 对象
    private static RxAndroidOkhttp mRxAndroidOkhttp=null;//RxAndroidOkhttp 对象
    private Context mContext;

    /**
     * 构造方法私有化
     * */

    private RxAndroidOkhttp(SSLSocketFactory sslSocketFactory,X509TrustManager trustManager){
        //创建okhttp对象 以及连接,读,取超时时间
        if(null!=sslSocketFactory&&null!=trustManager){
            mOkHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(DataConstant.nettimeout, TimeUnit.SECONDS)//连接时间
                    .readTimeout(DataConstant.nettimeout,TimeUnit.SECONDS)//读时间
                    .writeTimeout(DataConstant.nettimeout,TimeUnit.SECONDS)//写时间
                    .sslSocketFactory(sslSocketFactory,trustManager)//Https证书
                    .build();
        }else{
            mOkHttpClient=new OkHttpClient.Builder()
                    .connectTimeout(DataConstant.nettimeout, TimeUnit.SECONDS)//连接时间
                    .readTimeout(DataConstant.nettimeout,TimeUnit.SECONDS)//读时间
                    .writeTimeout(DataConstant.nettimeout,TimeUnit.SECONDS)//写时间
                    .build();
        }
    }

    /**
     * 获取此单例类对象的方法
     * */

    public static RxAndroidOkhttp getInstance(SSLSocketFactory sslSocketFactory,X509TrustManager trustManager){
        if(null==mRxAndroidOkhttp){//单例对象为空
            synchronized (RxAndroidOkhttp.class){
                mRxAndroidOkhttp=new RxAndroidOkhttp(sslSocketFactory,trustManager);
            }
        }
        return mRxAndroidOkhttp;
    }

    /**
     * get请求方法
     * */

    public Observable<String> get(final String url){
        //创建被观察者
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>(){
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final Subscriber mSubscriber=subscriber;
                //没有取消订阅的时候
                if(!mSubscriber.isUnsubscribed()){
                    //get请求
                    Request request=new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    if(null!=mOkHttpClient){
                        mOkHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                //通知订阅者的错误信息
                                mSubscriber.onError(e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(null!=response){//response 不为空
                                    if(response.isSuccessful()){//response 请求成功
                                        //通知订阅者的成功信息
                                        mSubscriber.onNext(response.body().string());
                                    }else{//response 请求失败
                                        //通知订阅者的错误信息
                                        IOException IOExceptionx=new IOException();
                                        mSubscriber.onError(IOExceptionx);
                                    }
                                }else{//response 为空
                                    //通知订阅者的错误信息
                                    IOException IOExceptionx=new IOException();
                                    mSubscriber.onError(IOExceptionx);
                                }
                                //通知完毕
                                mSubscriber.onCompleted();
                            }
                        });
                    }
                }
            }
        });
        return observable;
    }

}
