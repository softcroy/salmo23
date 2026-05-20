package com.appsc.salmo23.novos;


import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Play extends AppCompatActivity {

    public MediaPlayer mPlayer = null; // Áudio da oração
    private MediaPlayer bgMediaPlayer;  // Áudio de fundo

    ToggleButton play1;
    Button sha;
    TextView oracao;
    ImageView img;

    // Variáveis da Música de Fundo
    private float musicVolume = 0.5f;
    private List<String> musicUrls = new ArrayList<>();
    private List<String> musicNames = new ArrayList<>();
    private ToggleButton tgMusic;
    private LottieAnimationView lottieMusic;
    private View bgCircle;
    private ImageView imgMusicalNote;

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";
    private static String TAG = Play.class.getSimpleName();
    private String VIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);

        if (!Hawk.isBuilt()) Hawk.init(this).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        VIEW = isEnglish() ? "https://softcroy.com/app_santos_v2/api3/prayer/visualizacao_oracao_en.php?id="
                : "https://softcroy.com/app_santos_v2/api3/prayer/visualizacao_oracao_br.php?id=";

        // Inicializar Views
        play1 = findViewById(R.id.play1);
        sha = findViewById(R.id.nao);
        oracao = findViewById(R.id.oracao);
        img = findViewById(R.id.img); // Agora pegando a ImageView correta dentro do RelativeLayout dd

        configurarMusicaFundo();

        if (!Hawk.get("Assinatura").equals("Assinado")) {
            loadBanner();
        }

        CountVisualizacao();

        // Configurar Toolbar/Título
        if (getSupportActionBar() != null) {
            TextView tv = new TextView(getApplicationContext());
            tv.setText(getIntent().getStringExtra("NomeOracao"));
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setCustomView(tv);
        }

        // Glide corrigido para ImageView
        String imageUrl = getIntent().getStringExtra("Image");
        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(this).load(imageUrl).apply(requestOptions).into(img);

        oracao.setText(getIntent().getStringExtra("Oracao"));
        oracao.setTextSize(26);
        oracao.setTextColor(Color.parseColor("#737373"));

        sha.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra("Oracao") + "  " + getString(R.string.app_name) + " " + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Compartilhar"));
        });

        play1.setOnClickListener(v -> {
            if (play1.isChecked()) {
                playy1();
            } else {
                if (mPlayer != null) {
                    mPlayer.pause();
                    mPlayer = null;
                }
                // Volta o volume da música de fundo ao normal
                if (bgMediaPlayer != null) bgMediaPlayer.setVolume(musicVolume, musicVolume);
            }
        });
    }

    private void configurarMusicaFundo() {
        bgCircle = findViewById(R.id.bgCircle);
        lottieMusic = findViewById(R.id.lottieMusic);
        imgMusicalNote = findViewById(R.id.imgMusicalNote);
        tgMusic = findViewById(R.id.tgMusic);

        boolean isEnabled = Hawk.get("bg_music_enabled", true);
        tgMusic.setChecked(isEnabled);
        musicVolume = Hawk.get("bg_music_vol", 50) / 100f;

        if (isEnabled) {
            bgCircle.setVisibility(View.GONE);
            lottieMusic.setVisibility(View.VISIBLE);
            imgMusicalNote.setVisibility(View.VISIBLE);
            lottieMusic.playAnimation();
        }

        fetchMusicList(() -> {
            if (tgMusic.isChecked() && !musicUrls.isEmpty()) {
                startBackgroundMusic(Hawk.get("bg_music_pos", 0));
            }
        });

        tgMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            Hawk.put("bg_music_enabled", isChecked);
            if (isChecked) {
                bgCircle.setVisibility(View.GONE);
                lottieMusic.setVisibility(View.VISIBLE);
                imgMusicalNote.setVisibility(View.VISIBLE);
                lottieMusic.playAnimation();
                if (!musicUrls.isEmpty()) startBackgroundMusic(Hawk.get("bg_music_pos", 0));
                abrirDialogoConfigMusica();
            } else {
                lottieMusic.cancelAnimation();
                lottieMusic.setVisibility(View.GONE);
                imgMusicalNote.setVisibility(View.GONE);
                bgCircle.setVisibility(View.VISIBLE);
                stopBackgroundMusic();
            }
        });
    }

    private void fetchMusicList(Runnable callback) {
        String url = "https://softcroy.com/app_santos_v2/biblia/musicas/api_musicas.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                musicNames.clear(); musicUrls.clear();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    musicNames.add(isEnglish() ? obj.getString("nome_en") : obj.getString("nome_pt"));
                    musicUrls.add(obj.getString("link"));
                }
                if (callback != null) callback.run();
            } catch (Exception e) { e.printStackTrace(); }
        }, null);
        Volley.newRequestQueue(this).add(request);
    }

    private void startBackgroundMusic(int pos) {
        if (pos < 0 || pos >= musicUrls.size()) return;
        try {
            if (bgMediaPlayer != null) bgMediaPlayer.release();
            bgMediaPlayer = new MediaPlayer();
            bgMediaPlayer.setDataSource(musicUrls.get(pos));
            bgMediaPlayer.setLooping(true);
            bgMediaPlayer.setVolume(musicVolume, musicVolume);
            bgMediaPlayer.prepareAsync();
            bgMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void stopBackgroundMusic() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }

    private void abrirDialogoConfigMusica() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_config_musica, null);
        builder.setView(view);

        Spinner spMusic = view.findViewById(R.id.spMusic);
        SeekBar sbVolume = view.findViewById(R.id.sbVolume);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, musicNames);
        spMusic.setAdapter(adapter);
        spMusic.setSelection(Hawk.get("bg_music_pos", 0));
        sbVolume.setProgress(Hawk.get("bg_music_vol", 50));

        spMusic.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Hawk.put("bg_music_pos", position);
                if (tgMusic.isChecked()) startBackgroundMusic(position);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                musicVolume = p / 100f;
                if (bgMediaPlayer != null) bgMediaPlayer.setVolume(musicVolume, musicVolume);
                Hawk.put("bg_music_vol", p);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        builder.create().show();
    }

    private void playy1() {
        if (mPlayer != null) mPlayer.release();
        mPlayer = new MediaPlayer();
        play1.setText(R.string.aguarde);

        try {
            mPlayer.setDataSource(getIntent().getStringExtra("Sond"));
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(mp -> {
                play1.setText(R.string.parar);
                // Abaixa volume da música de fundo para ouvir a oração
                if (bgMediaPlayer != null) bgMediaPlayer.setVolume(0.1f, 0.1f);
                mPlayer.start();
            });
            mPlayer.setOnCompletionListener(mp -> {
                play1.setChecked(false);
                if (bgMediaPlayer != null) bgMediaPlayer.setVolume(musicVolume, musicVolume);
                mPlayer = null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bgMediaPlayer != null && !bgMediaPlayer.isPlaying() && Hawk.get("bg_music_enabled", true)) {
            bgMediaPlayer.start();
        }
        if (adView != null) adView.resume();
        esconderBotoesFisicos();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
            bgMediaPlayer.pause();
        }
        if (adView != null) adView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBackgroundMusic();
        if (adView != null) adView.destroy();
    }

    // --- MANTENDO SEUS MÉTODOS ORIGINAIS ---

    @Override
    public boolean onSupportNavigateUp() {
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
    // 1. Verifica se o UMP já liberou a requisição de anúncios antes de gastar processamento
    if (!com.google.android.ump.UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
        return;
    }

    // REMOVIDO: MobileAds.initialize (isso agora é feito na MyApplication)

    // Configura os IDs de dispositivos de teste
    MobileAds.setRequestConfiguration(
            new RequestConfiguration.Builder()
                    .setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                    .build()
    );

    // Inicializa o container e a View do Banner
    adContainerView = findViewById(R.id.ad_view_container);
    adView = new AdView(this);
    adView.setAdUnitId(AD_BANNER_ID);

    // Limpa o container e adiciona o novo anúncio
    adContainerView.removeAllViews();
    adContainerView.addView(adView);

    // Define o tamanho e carrega o anúncio com segurança
    adView.setAdSize(getAdSize());
    adView.loadAd(new AdRequest.Builder().build());
}

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = outMetrics.density;
        float adWidthPixels = adContainerView.getWidth();
        if (adWidthPixels == 0) adWidthPixels = outMetrics.widthPixels;
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, (int) (adWidthPixels / density));
    }

    private boolean isEnglish() {
        return Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage());
    }

    private void CountVisualizacao() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                VIEW + getIntent().getStringExtra("Id_oracao"), null, response -> {},
                error -> VolleyLog.d(TAG, "Error: " + error.getMessage()));
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(30000, 1, 1f));
        mRequestQueue.add(jsonObjReq);
    }

    private void esconderBotoesFisicos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }
}