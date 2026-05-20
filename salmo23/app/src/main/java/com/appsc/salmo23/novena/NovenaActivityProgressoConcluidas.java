package com.appsc.salmo23.novena;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.Principal_Oracao;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.appsc.salmo23.R;
import com.orhanobut.hawk.Hawk;

import java.util.Arrays;

public class NovenaActivityProgressoConcluidas extends AppCompatActivity {

    private ImageView imgHeader,iconAlarme;
    private TextView txtNome, txtFracao, txtDiaStat, txtLeituraStat, txtProgressoStat, txtCausa, txtHoraLembrete;
    private CircularProgressBar progressBar;
    private RecyclerView recyclerDias;

    private Novena novena;
    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novena_progresso); // Reutiliza o mesmo XML

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(getString(R.string.concluidas));
            tv.setTextSize(23);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
            tv.setTypeface(tf);
            getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setCustomView(tv);
        }

        // Recupera de forma segura evitando NullPointerException
        String assinaturaStatus = Hawk.get("Assinatura", "");
        if (!"Assinado".equals(assinaturaStatus)) {
            loadBanner();
        }

        // Recebe o objeto Novena já populado pelo ConcluidasFragment
        novena = (Novena) getIntent().getSerializableExtra("novena_selecionada");

        initViews();
        configurarInterfaceHistorica();

    }

    private void initViews() {
        imgHeader = findViewById(R.id.imgProgressoHeader);
        txtNome = findViewById(R.id.txtNomeNovenaAtiva);
        txtFracao = findViewById(R.id.txtProgressoFracao);
        txtDiaStat = findViewById(R.id.statDia);
        txtLeituraStat = findViewById(R.id.statLeitura);
        txtProgressoStat = findViewById(R.id.statPorcentagem);
        txtCausa = findViewById(R.id.txtHistoriaResumo);
        txtHoraLembrete = findViewById(R.id.txtHoraLembrete);
        iconAlarme = findViewById(R.id.iconAlarme);
        progressBar = findViewById(R.id.circularProgressBar);
        recyclerDias = findViewById(R.id.recyclerDiasAtivos);

        // Esconde o botão de editar permanentemente (modo leitura)
        View btnEditar = findViewById(R.id.btnEditarTudo);
        if (btnEditar != null) btnEditar.setVisibility(View.GONE);

        recyclerDias.setLayoutManager(new LinearLayoutManager(this));
        recyclerDias.setNestedScrollingEnabled(false);
    }

    private void configurarInterfaceHistorica() {
        if (novena != null) {
            txtNome.setText(novena.getNome());

            // Exibe a intenção salva no momento da conclusão
            txtCausa.setText("Intenção Alcançada: " + novena.getAuxCausa());

            // Usa o campo de lembrete para mostrar a data de término real vinda do PHP
            if (novena.getAuxDataTermino() != null && !novena.getAuxDataTermino().equals("---")) {
                txtHoraLembrete.setVisibility(View.VISIBLE);
                txtHoraLembrete.setText("Finalizadas em: " + novena.getAuxDataTermino());
                iconAlarme.setImageResource(R.drawable.ic_completed);
            } else {
                txtHoraLembrete.setVisibility(View.GONE);
            }

            Glide.with(this).load(novena.getImagem()).into(imgHeader);

            // Interface travada em 100%
            progressBar.setProgressMax(9f);
            progressBar.setProgress(9f);
            txtFracao.setText("9/9");
            txtProgressoStat.setText("100%");
            txtDiaStat.setText("9");
            txtLeituraStat.setText("9");

            // CORREÇÃO AQUI: Passando o 5º parâmetro (boolean) exigido pelo novo construtor do ProgressoAdapter
            boolean isAssinado = "Assinado".equals(Hawk.get("Assinatura", ""));
            ProgressoAdapter adapter = new ProgressoAdapter(this, novena, novena.getAuxDataInicio(), 9, isAssinado);
            recyclerDias.setAdapter(adapter);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        esconderBotoesFisicos();
        if (adView != null) {
            adView.resume();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        Principal_Oracao.getmInstanceActivity().OPENN();
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Principal_Oracao.getmInstanceActivity().OPENN();
        finish();
    }
    private void loadBanner() {
        if (!com.google.android.ump.UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
            return;
        }

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                        .build()
        );

        adContainerView = findViewById(R.id.ad_view_container);

        adView = new AdView(this);
        adView.setAdUnitId(AD_BANNER_ID);

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    private AdSize getAdSize() {
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;
        float adWidthPixels = adContainerView.getWidth();

        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
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
}