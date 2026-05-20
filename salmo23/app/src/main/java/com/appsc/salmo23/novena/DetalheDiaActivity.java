package com.appsc.salmo23.novena;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.bumptech.glide.Glide;
import com.appsc.salmo23.R;
import com.bumptech.glide.request.target.CustomTarget;
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
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetalheDiaActivity extends AppCompatActivity {

    private String idNovena;
    private int diaIndex;
    private boolean jaConcluido;
    private boolean salvouNestaSessao = false;
    private Button btnConcluir;
    private LinearLayout containerExtras;

    // Variáveis para Áudio
    private MediaPlayer mediaPlayer;
    private ToggleButton currentActiveButton; // Para desmarcar o botão anterior se trocar de áudio

    private final String URL_SALVAR = "https://softcroy.com/app_santos_v2/novena/concluir_dia.php";

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_dia);

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(getIntent().getStringExtra("titulo_dia"));
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
        if (!Hawk.get("Assinatura").equals("Assinado")) {
            loadBanner();
        }

        String imagemTopo = getIntent().getStringExtra("imagem_topo");

        ImageView imgTopo = findViewById(R.id.imgTopoDetalhe);

        imgTopo.setScaleType(ImageView.ScaleType.MATRIX);

        if (imagemTopo != null && !imagemTopo.isEmpty()) {

            Glide.with(this)
                    .load(imagemTopo)
                    .placeholder(R.drawable.img_maria)
                    .into(new CustomTarget<Drawable>() {

                        @Override
                        public void onResourceReady(
                                @NonNull Drawable resource,
                                @Nullable Transition<? super Drawable> transition) {

                            imgTopo.setImageDrawable(resource);

                            imgTopo.post(() -> {

                                Matrix matrix = new Matrix();

                                float viewWidth = imgTopo.getWidth();
                                float viewHeight = imgTopo.getHeight();

                                float drawableWidth = resource.getIntrinsicWidth();
                                float drawableHeight = resource.getIntrinsicHeight();

                                float scale = Math.max(
                                        viewWidth / drawableWidth,
                                        viewHeight / drawableHeight
                                );

                                matrix.setScale(scale, scale);

                                // centraliza horizontalmente
                                float dx = (viewWidth - drawableWidth * scale) / 2f;

                                // topo fixo
                                float dy = 0f;

                                matrix.postTranslate(dx, dy);

                                imgTopo.setImageMatrix(matrix);

                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

        }

        String titulo = getIntent().getStringExtra("titulo_dia");
        String jsonCompleto = getIntent().getStringExtra("json_dia_completo");
        idNovena = getIntent().getStringExtra("id_novena");
        diaIndex = getIntent().getIntExtra("dia_index", 0);
        jaConcluido = getIntent().getBooleanExtra("ja_concluido", false);

        TextView txtTitulo = findViewById(R.id.txtTituloDiaDetalhe);
        TextView txtTexto = findViewById(R.id.txtOracaoDiaDetalhe);
        containerExtras = findViewById(R.id.containerOracoesExtras);
        btnConcluir = findViewById(R.id.btnConcluirDia);

        txtTitulo.setText(titulo);

        try {

            if (jsonCompleto != null) {

                JSONObject diaObj = new JSONObject(jsonCompleto);

                String texto = diaObj.optString("texto");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    txtTexto.setText(
                            Html.fromHtml(texto, Html.FROM_HTML_MODE_LEGACY)
                    );
                } else {
                    txtTexto.setText(
                            Html.fromHtml(texto)
                    );
                }

                JSONArray extras = diaObj.optJSONArray("oracoes_extras");

                if (extras != null && extras.length() > 0) {

                    for (int i = 0; i < extras.length(); i++) {
                        adicionarCardExtra(extras.getJSONObject(i));
                    }

                }

            } else {

                String texto = getIntent().getStringExtra("oracao_texto");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    txtTexto.setText(
                            Html.fromHtml(texto, Html.FROM_HTML_MODE_LEGACY)
                    );
                } else {
                    txtTexto.setText(
                            Html.fromHtml(texto)
                    );
                }

            }

        } catch (Exception e) {

            Log.e("JSON_ERROR", "Erro ao processar orações extras", e);

        }

        if (jaConcluido) {
            btnConcluir.setEnabled(false);
            btnConcluir.setText("CONCLUÍDO");
            btnConcluir.setAlpha(0.7f);
        }

        btnConcluir.setOnClickListener(v -> concluirDia(btnConcluir));
    }

    private void adicionarCardExtra(JSONObject extra) {
        View card = getLayoutInflater().inflate(R.layout.item_card_oracao_extra, containerExtras, false);

        TextView txtNome = card.findViewById(R.id.txtNomeExtra);
        TextView txtCorpo = card.findViewById(R.id.txtTextoExtra);
        ToggleButton btnToggleAudio = card.findViewById(R.id.btnToggleAudioExtra);

        txtNome.setText(extra.optString("nome"));
        txtCorpo.setText(extra.optString("oracao"));

        String urlAudio = extra.optString("sond");

        btnToggleAudio.setOnClickListener(v -> {
            boolean isChecked = btnToggleAudio.isChecked();

            if (isChecked) {
                // Se já havia um áudio tocando em outro card, para ele e desmarca o botão anterior
                if (currentActiveButton != null && currentActiveButton != btnToggleAudio) {
                    currentActiveButton.setChecked(false);
                }
                currentActiveButton = btnToggleAudio;
                playAudio(urlAudio, btnToggleAudio);
            } else {
                stopAudio();
            }
        });

        containerExtras.addView(card);
    }

    private void playAudio(String url, ToggleButton btn) {
        try {
            stopAudio(); // Garante que não sobreponha áudios

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // Prepare assíncrono para não travar a UI

            Toast.makeText(this, "Carregando áudio...", Toast.LENGTH_SHORT).show();

            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                btn.setChecked(false); // Volta o ícone para "Play" quando acabar
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Erro ao reproduzir áudio.", Toast.LENGTH_SHORT).show();
                btn.setChecked(false);
                return false;
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro na URL do áudio.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAudio(); // Para o som se o usuário sair da tela
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (jaConcluido || salvouNestaSessao) {
            Principal_Oracao.getmInstanceActivity().OPENN();
            super.onBackPressed();
        } else {
            mostrarDialogoConfirmacao();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (jaConcluido || salvouNestaSessao) {
            Principal_Oracao.getmInstanceActivity().OPENN();
            super.onBackPressed();
        } else {
            mostrarDialogoConfirmacao();
        }
    }

    private void mostrarDialogoConfirmacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = getLayoutInflater().inflate(R.layout.dialog_confirmar_conclusao, null);
        builder.setView(layout);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        layout.findViewById(R.id.btnDialogConcluido).setOnClickListener(v -> {
            dialog.dismiss();
            concluirDia(btnConcluir);
        });

        layout.findViewById(R.id.btnDialogMaisTarde).setOnClickListener(v -> {
            dialog.dismiss();
            Principal_Oracao.getmInstanceActivity().OPENN();
            finish();
        });

        dialog.show();
    }

    private void concluirDia(Button btn) {
        btn.setEnabled(false);
        btn.setText("Salvando...");

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        int diaAtual = diaIndex + 1;

        SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'às' HH:mm", new Locale("pt", "BR"));
        String dataFormatada = sdf.format(new Date());

        StringRequest request = new StringRequest(Request.Method.POST, URL_SALVAR,
                response -> {
                    salvouNestaSessao = true;
                    btn.setText("CONCLUÍDO");
                    btn.setEnabled(false);
                    setResult(RESULT_OK);

                    if (diaAtual >= 9) {
                        // Abre o dialog customizado com o Lottie. O finish() será disparado ao fechar o dialog.
                        mostrarDialogoFelicitacao();
                    } else {
                        Toast.makeText(this, "Dia " + diaAtual + " concluído!", Toast.LENGTH_SHORT).show();
                        btn.postDelayed(this::finish, 1200);
                        Principal_Oracao.getmInstanceActivity().OPENN();
                    }
                },
                error -> {
                    btn.setEnabled(true);
                    btn.setText("Concluir Dia");
                    Toast.makeText(this, "Erro ao salvar no servidor.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("androidid", androidId);
                params.put("id_novena", idNovena);
                params.put("dia", String.valueOf(diaAtual));
                params.put("data_conclusao", dataFormatada);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void mostrarDialogoFelicitacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout = getLayoutInflater().inflate(R.layout.dialog_novena_concluida, null);
        builder.setView(layout);

        // Impede que o usuário feche clicando fora, garantindo que o fluxo do finish() aconteça no botão
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Ao clicar no botão "Amém", descarta o dialog e fecha a Activity
        layout.findViewById(R.id.btnFecharFelicitacao).setOnClickListener(v -> {
            dialog.dismiss();
            Principal_Oracao.getmInstanceActivity().OPENN();
            finish();
        });

        dialog.show();
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