package com.appsc.salmo23.santos;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.appsc.salmo23.figurinhas.EntryActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Santos extends AppCompatActivity{

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";
    SwipeRefreshLayout swipeRefreshLayout;
    List<GetDataAdapter_oracoes_santos> GetDataAdapter1;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    RecyclerViewAdapter_oracoes_santos recyclerViewadapter;
    String GET_JSON_DATA_HTTP_URL;
    String JSON_id           = "id";
    String JSON_identificador= "identificador";
    String JSON_nome         = "nome";
    String JSON_icone        = "icone";
    String JSON_image        = "image";
    String JSON_oracao       = "oracao";
    String JSON_sond         = "sond";
    String JSON_visualizacao = "visualizacao";
    String JSON_data         = "data";
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    private static String TAG   = EntryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.santos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (isEnglish()) {
            GET_JSON_DATA_HTTP_URL = "https://softcroy.com/app_santos_v2/api3/prayer/api_buscar_identificador_2_en.php?identificador=";
        } else {
            GET_JSON_DATA_HTTP_URL = "https://softcroy.com/app_santos_v2/api3/prayer/api_buscar_identificador_2.php?identificador=";
        }
        if (!Hawk.get("Assinatura").equals("Assinado")) {
            loadBanner();
        }        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(Santos.this.getIntent().getStringExtra("Identificador"));
            tv.setTextSize(23);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
            tv.setTypeface(tf);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv.setMarqueeRepeatLimit(-1); // -1 = infinito
            tv.setSelected(true); // Necessário para o marquee funcionar
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setCustomView(tv);
        }

        final RelativeLayout dd = findViewById(R.id.dd);
        String imageUrl = Santos.this.getIntent().getStringExtra("Image"); // Substitua isso pela URL real da imagem

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL); // Use a estratégia de cache apropriada para suas necessidades

        Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
                .into(new ViewTarget<RelativeLayout, Drawable>(dd) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        this.view.setBackground(resource);
                    }
                });
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        GetDataAdapter1 = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerViewlayoutManager = new LinearLayoutManager(Santos.this);
        recyclerView.setLayoutManager(recyclerViewlayoutManager);

        recyclerViewadapter = new RecyclerViewAdapter_oracoes_santos(GetDataAdapter1,Santos.this);
        recyclerView.setAdapter(recyclerViewadapter);

        JSON_DATA_WEB_CALL();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                JSON_DATA_WEB_CALL();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

    }
    private boolean isEnglish() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }
    public void JSON_DATA_WEB_CALL() {
        swipeRefreshLayout.setRefreshing(true);

        GetDataAdapter1.clear();
        jsonArrayRequest = new JsonArrayRequest(GET_JSON_DATA_HTTP_URL + Santos.this.getIntent().getStringExtra("Identificador"),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSON_PARSE_DATA_AFTER_WEBCALL(response);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        requestQueue = Volley.newRequestQueue(Santos.this);
        requestQueue.add(jsonArrayRequest);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            GetDataAdapter_oracoes_santos GetDataAdapter2 = new GetDataAdapter_oracoes_santos();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                GetDataAdapter2.setid           (json.getString(JSON_id));
                GetDataAdapter2.setidentificador(json.getString(JSON_identificador));
                GetDataAdapter2.setnome         (json.getString(JSON_nome));
                GetDataAdapter2.seticone        (json.getString(JSON_icone));
                GetDataAdapter2.setimage        (json.getString(JSON_image));
                GetDataAdapter2.setoracao       (json.getString(JSON_oracao));
                GetDataAdapter2.setsond         (json.getString(JSON_sond));
                GetDataAdapter2.setvisualizacao (json.getString(JSON_visualizacao));
                GetDataAdapter2.setdata         (json.getString(JSON_data));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            GetDataAdapter1.add(GetDataAdapter2);
            swipeRefreshLayout.setRefreshing(false);
        }
        recyclerViewadapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        esconderBotoesFisicos();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }



    @Override
    public boolean onSupportNavigateUp(){
        Principal_Oracao.getmInstanceActivity().OPENN();
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        Principal_Oracao.getmInstanceActivity().OPENN();
        finish();

    }

    private void loadBanner() {
    // 1. Verifica se o UMP já liberou a requisição de anúncios antes de prosseguir
    if (!com.google.android.ump.UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
        return;
    }

    // REMOVIDO: Bloco MobileAds.initialize (Gerenciado de forma centralizada na MyApplication)

    // Configura os IDs de dispositivos de teste
    MobileAds.setRequestConfiguration(
            new RequestConfiguration.Builder()
                    .setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                    .build()
    );

    // Encontra o container do ad view no seu layout
    adContainerView = findViewById(R.id.ad_view_container);

    // Cria um novo AdView e define o ID do bloco de anúncios
    adView = new AdView(this);
    adView.setAdUnitId(AD_BANNER_ID);

    // Remove qualquer visualização existente do container e adiciona a ad view
    adContainerView.removeAllViews();
    adContainerView.addView(adView);

    // Pega o AdSize apropriado para a ad view com base na largura do container
    AdSize adSize = getAdSize();
    adView.setAdSize(adSize);

    // Monta a requisição e carrega o anúncio com segurança
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);
}
    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
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
}
