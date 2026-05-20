package com.appsc.salmo23;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.orhanobut.hawk.Hawk;

import java.util.List;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks, PurchasesUpdatedListener {

    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_OPEN_ID = "ca-app-pub-3882038212063780/1907507525";

    private AppOpenAd appOpenAd = null;
    private static boolean isShowingAd = false;
    private boolean isAdLoaded = false;
    private String statusCarregamento = "não";
    private Activity currentActivity;
    private final MyApplication myApplication;

    // Billing variables
    private BillingClient billingClient;
    private boolean isSubscribed = false;

    public AppOpenManager(MyApplication myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // Inicializa o Billing direto no construtor
        initBilling();
    }

    private void initBilling() {
    billingClient = BillingClient.newBuilder(myApplication)
            .setListener(this)
            .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts()
                            .build()
            )
            .build();

    billingClient.startConnection(new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                checkSubscriptionStatus();
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
        }
    });
}

    private void checkSubscriptionStatus() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (result, purchases) -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        boolean active = false;
                        String targetSku = Hawk.get("skuItem", "appsc.salmo23.1mes");

                        for (Purchase p : purchases) {
                            if (p.getProducts().contains(targetSku) && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                active = true;
                                break;
                            }
                        }
                        isSubscribed = active;
                        // Sincroniza com o Hawk para manter o resto do app atualizado
                        Hawk.put("Assinatura", active ? "Assinado" : "Assinar");
                        Log.i(LOG_TAG, "Status Assinatura Billing: " + isSubscribed);
                    }
                }
        );
    }

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        // Verifica novamente ao abrir para garantir
        if (billingClient != null && billingClient.isReady()) {
            checkSubscriptionStatus();
        }

        if (!isSubscribed) {
            if (statusCarregamento.equals("não")) {
                fetchAd();
                statusCarregamento = "sim";
            }
        }
    }

    public void fetchAd() {
        if (isSubscribed || isShowingAd || isAdLoaded) return;

        // --- ADICIONE ESTA VALIDAÇÃO DO UMP ---
        com.google.android.ump.ConsentInformation consentInformation =
                com.google.android.ump.UserMessagingPlatform.getConsentInformation(myApplication);
        if (!consentInformation.canRequestAds()) {
            Log.d(LOG_TAG, " fetchAd abortado: Usuário ainda não deu consentimento UMP.");
            return;
        }
        // --------------------------------------

        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                appOpenAd = ad;
                isAdLoaded = true;
                if (currentActivity instanceof Principal_Oracao) {
                    showAd();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                statusCarregamento = "não";
            }
        };

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(myApplication, AD_OPEN_ID, request, loadCallback);
    }

    private void showAd() {
        if (isSubscribed || appOpenAd == null || isShowingAd) return;

        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                isAdLoaded = false;
                statusCarregamento = "não";
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });

        appOpenAd.show(currentActivity);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        // Se houver uma compra em tempo real, atualizamos o status
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            checkSubscriptionStatus();
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
        if (!isSubscribed && activity instanceof Principal_Oracao && isAdLoaded && !isShowingAd) {
            showAd();
        }
    }

    // Métodos obrigatórios do Lifecycle (Vazios ou mantendo lógica anterior)
    @Override public void onActivityResumed(@NonNull Activity activity) { currentActivity = activity; }
    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) { if (currentActivity == activity) currentActivity = null; }
}