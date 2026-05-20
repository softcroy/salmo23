package com.appsc.salmo23.figurinhas;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.appsc.salmo23.identities.StickerPacksContainer;
import com.appsc.salmo23.utils.StickerPacksManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.hawk.Hawk;

import java.lang.ref.WeakReference;

public class StickerPackDetailsActivity extends AddStickerPackActivity {

    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View addButton;
    private View alreadyAddedText;
    private StickerPack stickerPack;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private ProgressBar downloadProgressBar; // ProgressBar para feedback de download

    private FrameLayout adContainerView;
    private AdView adView;
    TextView packSizeTextView;
    private TextView textViewCreatePack;
    private TextView textViewDownloads;

    private boolean jaVerifiqueiInicialmente = false;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_pack_details1);

        stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);

        // Inicializar Views
        downloadProgressBar = findViewById(R.id.entry_ponit_loader); // Use o ID do seu XML (ex: loader ou progressBar)
        textViewCreatePack = findViewById(R.id.text_view_create_pack);
        textViewDownloads = findViewById(R.id.item_pack_downloads);
        packSizeTextView = findViewById(R.id.text_view_size_pack);
        addButton = findViewById(R.id.add_to_whatsapp_button);
        alreadyAddedText = findViewById(R.id.already_added_text);
        recyclerView = findViewById(R.id.sticker_list);

        if (!Hawk.get("Assinatura", "").equals("Assinado")) {
            loadBanner();
        }

        if (stickerPack != null) {
            // Executa a validação completa (download dos stickers) em segundo plano ao abrir
            new ValidatePackTask(this).execute(stickerPack);

            // Setar textos básicos
            if (textViewCreatePack != null) textViewCreatePack.setText(stickerPack.data);
            if (textViewDownloads != null) textViewDownloads.setText(String.valueOf(stickerPack.download));

            if (packSizeTextView != null) {
                long totalBytes = stickerPack.getTotalSize();
                if (totalBytes <= 0 && stickerPack.getStickers() != null) {
                    totalBytes = stickerPack.getStickers().size() * 46080L;
                }
                packSizeTextView.setText(Formatter.formatShortFileSize(this, totalBytes));
            }

            setupRecyclerView();
            setupPackHeader();

            addButton.setOnClickListener(v -> {
                if (downloadProgressBar != null) downloadProgressBar.setVisibility(View.VISIBLE);
                v.setEnabled(false); // Evita cliques duplos
                addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
            });

            setupActionBar(showUpButton);
        }

        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void setupRecyclerView() {
        final int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPadding(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels);
        recyclerView.setClipToPadding(false);

        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(spacingInPixels, spacingInPixels, spacingInPixels, spacingInPixels);
            }
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int viewWidth = recyclerView.getWidth();
            int stickerSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size);
            int totalItemWidth = stickerSize + spacingInPixels;
            if (viewWidth > 0) {
                int columns = viewWidth / totalItemWidth;
                setNumColumns(Math.max(columns, 1));
            }
        });

        stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), spacingInPixels, stickerPack);
        recyclerView.setAdapter(stickerPreviewAdapter);
    }

    private void setupPackHeader() {
        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);

        packNameTextView.setText(stickerPack.name);
        packPublisherTextView.setText(R.string.app_name);

        int radius = (int) (25 * getResources().getDisplayMetrics().density);
        com.bumptech.glide.Glide.with(this)
                .load(StickerPackLoader.getStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile))
                .placeholder(R.drawable.sticker_error)
                .override(160, 160)
                .transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(), new com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
                .into(packTrayIcon);
    }

    // Task para validar e baixar as figurinhas assim que a tela abre
    static class ValidatePackTask extends AsyncTask<StickerPack, Void, Exception> {
        private final WeakReference<StickerPackDetailsActivity> activityRef;
        ValidatePackTask(StickerPackDetailsActivity activity) { this.activityRef = new WeakReference<>(activity); }

        @Override
        protected Exception doInBackground(StickerPack... packs) {
            try {
                StickerPackDetailsActivity activity = activityRef.get();
                if (activity != null) {
                    StickerPackValidator.verifyStickerPackValidity(activity, packs[0]);
                }
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            StickerPackDetailsActivity activity = activityRef.get();
            if (activity != null && e != null) {
                Toast.makeText(activity, "Erro ao carregar figurinhas: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Esconde o progress ao retornar do WhatsApp
        if (downloadProgressBar != null) downloadProgressBar.setVisibility(View.GONE);
        if (addButton != null) addButton.setEnabled(true);
    }

    // --- Mantenha os métodos incrementarDownloadWeb, setupActionBar, esconderBotoesFisicos etc abaixo ---
    // (O restante do seu código de AdMob e System UI permanece igual)

    private void setNumColumns(int columns) {
        if (this.numColumns != columns && columns > 0) {
            layoutManager.setSpanCount(columns);
            this.numColumns = columns;
            if (stickerPreviewAdapter != null) stickerPreviewAdapter.notifyDataSetChanged();
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        addButton.setVisibility(isWhitelisted ? View.GONE : View.VISIBLE);
        alreadyAddedText.setVisibility(isWhitelisted ? View.VISIBLE : View.GONE);
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
        private final WeakReference<StickerPackDetailsActivity> activityReference;
        WhiteListCheckAsyncTask(StickerPackDetailsActivity activity) { this.activityReference = new WeakReference<>(activity); }
        @Override
        protected Boolean doInBackground(StickerPack... packs) {
            StickerPackDetailsActivity activity = activityReference.get();
            if (activity == null || packs == null || packs.length == 0) return false;
            return WhitelistCheck.isWhitelisted(activity, packs[0].identifier);
        }
        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            StickerPackDetailsActivity activity = activityReference.get();
            if (activity != null) {
                if (!activity.jaVerifiqueiInicialmente) {
                    activity.jaVerifiqueiInicialmente = true;
                } else if (isWhitelisted && activity.addButton.getVisibility() == View.VISIBLE) {
                    if (activity.stickerPack != null) activity.incrementarDownloadWeb(activity.stickerPack.identifier);
                }
                activity.updateAddUI(isWhitelisted);
            }
        }
    }

    private void incrementarDownloadWeb(String identifier) {
        String url = "https://softcroy.com/app_santos_v2/api3/stickers/download.php?identifier=" + identifier;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {}, error -> {});
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void setupActionBar(boolean showUpButton) {
        if (getSupportActionBar() != null && stickerPack != null) {
            TextView tv = new TextView(this);
            tv.setText(stickerPack.name);
            tv.setTextSize(23);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.WHITE);
            try {
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
                tv.setTypeface(tf);
            } catch (Exception ignored) {}
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setCustomView(tv);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        esconderBotoesFisicos();
        if (adView != null) adView.resume();
        if (stickerPack != null) {
            whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
            whiteListCheckAsyncTask.execute(stickerPack);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) adView.pause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (Principal_Oracao.getmInstanceActivity() != null) Principal_Oracao.getmInstanceActivity().OPENN();
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (Principal_Oracao.getmInstanceActivity() != null) Principal_Oracao.getmInstanceActivity().OPENN();
        super.onBackPressed();
    }

    private void loadBanner() {
    // 1. Verifica se o UMP já liberou a requisição de anúncios antes de prosseguir
    if (!com.google.android.ump.UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
        return;
    }

    // REMOVIDO: MobileAds.initialize (isso agora é gerenciado na MyApplication)

    // Inicializa o container e a View do Banner
    adContainerView = findViewById(R.id.ad_view_container);
    adView = new AdView(this);
    adView.setAdUnitId(AD_BANNER_ID);

    // Limpa o container e adiciona a nova visualização de anúncio
    adContainerView.removeAllViews();
    adContainerView.addView(adView);

    // Define o tamanho responsivo
    adView.setAdSize(getAdSize());

    // Monta a requisição e carrega o anúncio com segurança
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);
}

    private AdSize getAdSize() {
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = outMetrics.density;
        float adWidthPixels = adContainerView.getWidth();
        if (adWidthPixels == 0) adWidthPixels = outMetrics.widthPixels;
        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void esconderBotoesFisicos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}