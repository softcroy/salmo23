package com.appsc.salmo23;

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyApplication extends Application {
    private static final String TAG = "MyApplicationUMP";
    private static AppOpenManager appOpenManager;
    private ConsentInformation consentInformation;
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        FirebaseApp.initializeApp(this);
        Hawk.init(this).setEncryption(new NoEncryption()).build();
        Hawk.put("skuItem", "appsc.salmo23.1mes");

        consentInformation = UserMessagingPlatform.getConsentInformation(this);

        // Se o consentimento já existia de sessões anteriores, inicializa o AdMob imediatamente
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }

    public void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Inicializa o SDK de Anúncios
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {
            Log.i(TAG, "AdMob Inicializado com sucesso.");
        });

        // Inicia o gerenciador de abertura
        if (appOpenManager == null) {
            appOpenManager = new AppOpenManager(this);
        }
    }
}