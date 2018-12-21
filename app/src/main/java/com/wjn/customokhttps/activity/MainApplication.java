package com.wjn.customokhttps.activity;

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MainApplication extends Application {

    private static SSLSocketFactory sslSocketFactory;
    private static X509TrustManager x509TrustManager;

    @Override
    public void onCreate() {
        super.onCreate();
        initx509TrustManagerAndsslSocketFactory();
    }

    /**
     * 初始化
     * */

    private void initx509TrustManagerAndsslSocketFactory(){
        InputStream inputStream = null;
        try {
            inputStream = getApplicationContext().getAssets().open("srca.cer"); //得到证书的输入流
            try {
                if(null!=inputStream){
                    x509TrustManager = trustManagerForCertificates(inputStream);//以流的方式读入证书
                    if(null!=x509TrustManager){
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
                        sslSocketFactory = sslContext.getSocketFactory();
                    }
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        MainApplication.setSslSocketFactory(sslSocketFactory);
        MainApplication.setX509TrustManager(x509TrustManager);
    }

    /**
     * 以流的方式添加信任证书
     */

    private X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    /**
     * 添加password
     * @param password
     * @return
     * @throws GeneralSecurityException
     */

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // 这里添加自定义的密码，默认
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public static void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        MainApplication.sslSocketFactory = sslSocketFactory;
    }

    public static X509TrustManager getX509TrustManager() {
        return x509TrustManager;
    }

    public static void setX509TrustManager(X509TrustManager x509TrustManager) {
        MainApplication.x509TrustManager = x509TrustManager;
    }
}
