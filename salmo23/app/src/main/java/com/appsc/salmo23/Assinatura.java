package com.appsc.salmo23;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.billingclient.api.*;
import com.orhanobut.hawk.Hawk;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Assinatura extends AppCompatActivity implements PurchasesUpdatedListener {
    private BillingClient billingClient;
    private ProductDetails productDetails;
    private String offerToken;

    private TextView tvPrice;
    private Button btnSubscribe;
    private ImageView close;

    private final int[] imagensAmem = {R.drawable.a1, R.drawable.a2, R.drawable.b1, R.drawable.b2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assinatura);

        // Bind UI
        tvPrice       = findViewById(R.id.tvPrice);
        btnSubscribe  = findViewById(R.id.btnSubscribe);
        close         = findViewById(R.id.close);

        close.setOnClickListener(view -> finish());

        btnSubscribe.setEnabled(false);

        initBilling();

        btnSubscribe.setOnClickListener(v -> launchPurchaseFlow());
    }

    private void initBilling() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .build()
                )
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                    checkSubscriptionStatus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.i("Comprar", "Faturamento desconectado");
            }
        });
    }

    private void queryProductDetails() {
        QueryProductDetailsParams.Product prod = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Hawk.get("skuItem"))
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                        .setProductList(Collections.singletonList(prod))
                        .build(),
                (billingResult, queryProductDetailsResult) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && queryProductDetailsResult != null
                            && !queryProductDetailsResult.getProductDetailsList().isEmpty()) {

                        productDetails = queryProductDetailsResult.getProductDetailsList().get(0);
                        List<ProductDetails.SubscriptionOfferDetails> offers = productDetails.getSubscriptionOfferDetails();

                        if (offers != null && !offers.isEmpty()) {
                            offerToken = offers.get(0).getOfferToken();
                            runOnUiThread(() -> {
                                ProductDetails.PricingPhase phase = offers.get(0).getPricingPhases().getPricingPhaseList().get(0);
                                tvPrice.setText(phase.getFormattedPrice());

                                // Ativa o botão e inicia o efeito chamativo
                                btnSubscribe.setEnabled(true);
                                iniciarEfeitoBrilho(btnSubscribe);
                            });
                        }
                    }
                }
        );
    }

    private void launchPurchaseFlow() {
        if (productDetails == null || offerToken == null) return;

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                ))
                .build();

        billingClient.launchBillingFlow(this, flowParams);
    }

    private void checkSubscriptionStatus() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (result, purchases) -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase p : purchases) {
                            if (p.getProducts().contains(Hawk.get("skuItem"))) {
                                runOnUiThread(() -> {
                                    btnSubscribe.setText("Assinatura Ativa");
                                    btnSubscribe.setEnabled(false);
                                });
                                handlePurchase(p);
                                break;
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            runOnUiThread(() -> {
                // Ativa a animação de "Amém/Anjinhos" ao confirmar a compra
                animarAmem(btnSubscribe);

                btnSubscribe.setText("Assinatura Ativa");
                btnSubscribe.setEnabled(false);
            });
            for (Purchase p : purchases) {
                handlePurchase(p);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                && !purchase.isAcknowledged()) {
            AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(ackParams, ackResult -> {
                Log.i("Comprar", "Compra reconhecida");
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        esconderBotoesFisicos();
    }
    public void animarAmem(View viewMae) {
        if (viewMae == null) return;

        int quantidadeIcones = 10;
        Random random = new Random();
        ViewGroup root = (ViewGroup) viewMae.getRootView();

        int[] localizacao = new int[2];
        viewMae.getLocationInWindow(localizacao);

        for (int i = 0; i < quantidadeIcones; i++) {
            final ImageView imgAnim = new ImageView(this);
            imgAnim.setImageResource(imagensAmem[random.nextInt(imagensAmem.length)]);

            int sizeDp = 50 + random.nextInt(40);
            int sizePx = (int) (sizeDp * this.getResources().getDisplayMetrics().density);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
            imgAnim.setLayoutParams(params);

            imgAnim.setX(localizacao[0] + (viewMae.getWidth() / 2f) - (sizePx / 2f));
            imgAnim.setY(localizacao[1]);

            root.addView(imgAnim);

            float finalX = (random.nextFloat() * 600) - 300;
            float finalY = -(600 + random.nextInt(400));

            imgAnim.animate()
                    .translationYBy(finalY)
                    .translationXBy(finalX)
                    .alpha(0f)
                    .rotation(random.nextInt(90) - 45)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(3000 + random.nextInt(2000))
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> root.removeView(imgAnim))
                    .start();
        }
    }
    private void esconderBotoesFisicos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                // REMOVIDO: statusBars() -> Assim a hora continua aparecendo
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    // REMOVIDO: SYSTEM_UI_FLAG_FULLSCREEN e LAYOUT_FULLSCREEN
            );
        }
    }
    private void iniciarEfeitoBrilho(View view) {
        // Animação para aumentar e diminuir a escala (pulsação)
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(800)
                            .withEndAction(() -> iniciarEfeitoBrilho(view))
                            .start();
                }).start();
    }
}