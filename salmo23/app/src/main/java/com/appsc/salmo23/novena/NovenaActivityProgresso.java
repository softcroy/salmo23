package com.appsc.salmo23.novena;

import static com.appsc.salmo23.figurinhas.StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NovenaActivityProgresso extends AppCompatActivity {

    private ImageView imgHeader;
    private TextView txtNome, txtFracao, txtDiaStat, txtLeituraStat, txtProgressoStat, txtCausa, txtHoraLembrete;
    private CircularProgressBar progressBar;
    private RecyclerView recyclerDias;
    private ImageButton btnEditar;

    private Novena novena;
    private String androidId;
    private String horaLembreteSalva = "";
    private boolean isRegistrando = false;

    // ======================
// VELA
// ======================

    private ImageView imgCandle;
    private ImageView imgWick;
    private ImageView imgFlame;

    private boolean possuiVela = false;
    private String corVela = "#E0E0E0";

    private final int[] candleHeights = {
            170, 150, 130, 110, 92, 75, 58, 40, 25
    };

    private final int[] flameOffsets = {
            122, 115, 107, 100, 93, 86, 79, 73, 67
    };
    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    private final String URL_DETALHES = "https://softcroy.com/app_santos_v2/novena/get_detalhes_progresso.php";
    private final String URL_UPDATE_INICIAR = "https://softcroy.com/app_santos_v2/novena/iniciar_novena.php";
    private final String URL_REGISTRAR_ACAO = "https://softcroy.com/app_santos_v2/novena/registrar_acao.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novena_progresso);

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(getString(R.string.progresso));
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

        novena = (Novena) getIntent().getSerializableExtra("novena_selecionada");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        initViews();

        if (novena != null && novena.getAuxDiasConcluidos() >= 9) {
            atualizarUI(
                    novena.getAuxDataInicio(),
                    9,
                    null
            );
            btnEditar.setVisibility(View.GONE);
        } else {
            carregarDadosCompletos();
        }

        btnEditar.setOnClickListener(v -> mostrarDialogoEdicao());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            carregarDadosCompletos();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        if (Hawk.get("Assinatura").equals("Assinado")) {
            FrameLayout adContainerView = findViewById(R.id.ad_view_container);
            if (adContainerView != null) {
                adContainerView.removeAllViews(); // Remove o AdView (o banner em si)
                adContainerView.setVisibility(View.GONE); // Esconde o espaço que ele ocupava
            }
        }
        esconderBotoesFisicos();
        if (novena != null) {
            carregarDadosCompletos();
        }
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
        btnEditar = findViewById(R.id.btnEditarTudo);
        progressBar = findViewById(R.id.circularProgressBar);
        recyclerDias = findViewById(R.id.recyclerDiasAtivos);

        imgCandle = findViewById(R.id.dialog_candle);
        imgWick = findViewById(R.id.dialog_wick);
        imgFlame = findViewById(R.id.dialog_flame);

        recyclerDias.setLayoutManager(new LinearLayoutManager(this));
        recyclerDias.setNestedScrollingEnabled(false);
    }

    private void carregarDadosCompletos() {
        String url = URL_DETALHES + "?androidid=" + androidId + "&id_novena=" + novena.getId();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean sucesso = response.optBoolean("success", true);
                        int concluidos = response.optInt("dias_concluidos", 0);

                        if (!sucesso && novena.getAuxDiasConcluidos() >= 8) {
                            concluidos = 9;
                        }

                        novena.setNome(response.optString("nome", novena.getNome()));
                        novena.setImagem(response.optString("imagem", novena.getImagem()));
                        novena.setAuxCausa(response.optString("causa", novena.getAuxCausa()));
                        horaLembreteSalva = response.optString("hora_lembrete", "");
                        String dataInicio = response.optString("data_inicio", novena.getAuxDataInicio());

                        // ======================
// VELA
// ======================

                        possuiVela =
                                response.optBoolean("vela", false);

                        corVela =
                                response.optString(
                                        "cor",
                                        "#E0E0E0"
                                );

                        if (concluidos >= 9 && !isRegistrando) {
                            registrarConclusaoGlobal();
                        }

                        JSONArray historico = response.optJSONArray("historico_detalhado");
                        if (historico != null) {
                            novena.getDatasConclusao().clear();
                            for (int i = 0; i < historico.length(); i++) {
                                JSONObject obj = historico.getJSONObject(i);
                                novena.addDataConclusao(obj.optInt("dia"), obj.optString("data"));
                            }
                        }

                        // Lógica de Processamento das Rezas com suporte a Orações Extras dinâmicas
                        JSONArray rezasArray = response.optJSONArray("rezas_dias");
                        if (rezasArray != null) {
                            List<String> listaRezasTexto = new ArrayList<>();
                            List<String> listaRezasCompletas = new ArrayList<>();

                            for (int i = 0; i < rezasArray.length(); i++) {
                                JSONObject diaObj = rezasArray.optJSONObject(i);
                                if (diaObj != null) {
                                    // Novo formato: Objeto JSON contendo 'texto' e o array 'oracoes_extras' populado pelo PHP
                                    listaRezasTexto.add(diaObj.optString("texto"));
                                    listaRezasCompletas.add(diaObj.toString());
                                } else {
                                    // Formato antigo: String simples (fallback de segurança)
                                    String textoPuro = rezasArray.getString(i);
                                    listaRezasTexto.add(textoPuro);
                                    try {
                                        listaRezasCompletas.add(new JSONObject().put("texto", textoPuro).toString());
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            novena.setListaRezas(listaRezasTexto);
                            novena.setListaRezasCompletas(listaRezasCompletas);
                        }

                        atualizarUI(
                                dataInicio,
                                concluidos,
                                historico
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (novena.getAuxDiasConcluidos() >= 8) atualizarUI(
                                novena.getAuxDataInicio(),
                                9,
                                null
                        );
                    }
                }, error -> {
            if (novena.getAuxDiasConcluidos() >= 8) atualizarUI(
                    novena.getAuxDataInicio(),
                    9,
                    null
            );
        });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void registrarConclusaoGlobal() {
        if (isRegistrando) return;
        isRegistrando = true;

        StringRequest request = new StringRequest(Request.Method.POST, URL_REGISTRAR_ACAO,
                response -> Log.d("CONCLUIDA", "Sucesso ao migrar tabela"),
                error -> {
                    isRegistrando = false;
                    Log.e("CONCLUIDA", "Erro no registro");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_novena", novena.getId());
                params.put("acao", "concluido");
                params.put("androidid", androidId);
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void atualizarUI(String dataInicio,
                             int concluidos,
                             JSONArray historico) {
        txtNome.setText(novena.getNome());
        txtCausa.setText("Sua Intenção: " + novena.getAuxCausa());

        if (horaLembreteSalva != null && !horaLembreteSalva.isEmpty()) {
            txtHoraLembrete.setVisibility(View.VISIBLE);
            txtHoraLembrete.setText("Lembrete diário às " + horaLembreteSalva + "h");
        } else {
            txtHoraLembrete.setVisibility(View.GONE);
        }

        Glide.with(this).load(novena.getImagem()).into(imgHeader);

        // ======================
// VELA
// ======================

        if (possuiVela) {

            imgCandle.setVisibility(View.VISIBLE);
            imgWick.setVisibility(View.VISIBLE);
            imgFlame.setVisibility(View.VISIBLE);

            tintCandle(
                    imgCandle,
                    corVela
            );

            startFlickerAnimation(imgFlame);

            int diasConcluidos = 0;

            if (historico != null) {
                diasConcluidos = historico.length();
            }

            updateCandleByProgress(
                    diasConcluidos
            );

        } else {

            imgCandle.setVisibility(View.GONE);
            imgWick.setVisibility(View.GONE);
            imgFlame.setVisibility(View.GONE);
        }

        progressBar.setProgressMax(9f);
        progressBar.setProgressWithAnimation((float) concluidos, 1200L);

        txtFracao.setText(concluidos + "/9");
        float percent = (concluidos / 9f) * 100f;
        txtProgressoStat.setText((int) percent + "%");

        if (concluidos >= 9) {
            btnEditar.setVisibility(View.GONE);
            txtDiaStat.setText("9");
            txtLeituraStat.setText("9");
        } else {
            txtDiaStat.setText(String.valueOf(concluidos + 1));
            txtLeituraStat.setText(String.valueOf(concluidos));
            btnEditar.setVisibility(View.VISIBLE);
        }

        boolean isAssinado = Hawk.get("Assinatura", "").equals("Assinado");
        ProgressoAdapter adapter = new ProgressoAdapter(this, novena, dataInicio, concluidos, isAssinado);
        recyclerDias.setAdapter(adapter);
    }

    private void mostrarDialogoEdicao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_editar_intencao, null);
        builder.setView(dialogView);

        EditText edtIntencao = dialogView.findViewById(R.id.edtIntencao);
        TimePicker timePicker = dialogView.findViewById(R.id.timePickerAlarme);
        Button btnAtualizar = dialogView.findViewById(R.id.btnConfirmarIntencao);
        ImageButton btnFechar = dialogView.findViewById(R.id.btnFecharDialog);
        HorizontalScrollView scrollCoresVela = dialogView.findViewById(R.id.scrollCoresVela);

        // Ocultar os Minutos do TimePicker (Modo Spinner)
        if (timePicker != null) {
            timePicker.setIs24HourView(true);
            int minutePickerId = Resources.getSystem().getIdentifier("minute", "id", "android");
            if (minutePickerId != 0) {
                View minutePicker = timePicker.findViewById(minutePickerId);
                if (minutePicker != null) {
                    minutePicker.setVisibility(View.GONE);
                }
            }
        }

        edtIntencao.setText(novena.getAuxCausa());

        // =====================================================
        // Lógica das Cores (Opacidade acionada APENAS no clique)
        // =====================================================
        if (possuiVela && scrollCoresVela != null) {
            scrollCoresVela.setVisibility(View.VISIBLE);

            String[] hexCores = {"#FFFFFF", "#FF0000", "#0000FF", "#008000", "#800080", "#FFFF00", "#FF1D8D", "#000000"};
            int[] idsViews = {
                    R.id.colorWhite, R.id.colorRed, R.id.colorBlue, R.id.colorGreen,
                    R.id.colorPurple, R.id.colorYellow, R.id.colorPink, R.id.colorBlack
            };

            final List<View> viewsCores = new ArrayList<>();
            for (int id : idsViews) {
                View v = dialogView.findViewById(id);
                if (v != null) {
                    // Garante que ao abrir o diálogo, todas comecem 100% visíveis
                    v.setAlpha(1.0f);
                    viewsCores.add(v);
                }
            }

            for (int i = 0; i < viewsCores.size(); i++) {
                final View viewAtual = viewsCores.get(i);
                final String corSelecionada = hexCores[i];

                viewAtual.setOnClickListener(v -> {
                    corVela = corSelecionada; // Atualiza a variável global

                    // No momento em que uma é escolhida, as outras tornam-se opacas
                    for (View vc : viewsCores) {
                        if (vc == viewAtual) {
                            vc.setAlpha(1.0f); // Mantém a selecionada totalmente visível
                        } else {
                            vc.setAlpha(0.3f); // Torna as demais opacas
                        }
                    }

                    Toast.makeText(this, "Cor selecionada!", Toast.LENGTH_SHORT).show();

                    // Ativa o botão de salvar alterações
                    btnAtualizar.setEnabled(true);
                    btnAtualizar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#35AEFF")));
                    btnAtualizar.setText("SALVAR ALTERAÇÕES");
                });
            }
        } else if (scrollCoresVela != null) {
            scrollCoresVela.setVisibility(View.GONE);
        }

        // Listener corrigido com o (int start)
        edtIntencao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean temTexto = s.toString().trim().length() > 0;
                btnAtualizar.setEnabled(temTexto);
                btnAtualizar.setBackgroundTintList(ColorStateList.valueOf(temTexto ? Color.parseColor("#35AEFF") : Color.parseColor("#CCCCCC")));
                btnAtualizar.setText(temTexto ? "SALVAR ALTERAÇÕES" : "DIGITE SUA INTENÇÃO");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (timePicker != null && horaLembreteSalva.contains(":")) {
            try {
                String[] p = horaLembreteSalva.split(":");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.setHour(Integer.parseInt(p[0]));
                    timePicker.setMinute(0);
                } else {
                    timePicker.setCurrentHour(Integer.parseInt(p[0]));
                    timePicker.setCurrentMinute(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnFechar.setOnClickListener(v -> dialog.dismiss());
        btnAtualizar.setOnClickListener(v -> {
            String novaIntencao = edtIntencao.getText().toString().trim();
            int h = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? timePicker.getHour() : timePicker.getCurrentHour();
            String novaHora = String.format(Locale.getDefault(), "%02d:00", h);

            if (!novaIntencao.isEmpty()) {
                salvarEdicao(novaIntencao, novaHora, corVela);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void salvarEdicao(String intencao, String hora, String cor) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_UPDATE_INICIAR,
                response -> {
                    Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show();
                    carregarDadosCompletos();
                },
                error -> Log.e("UPDATE", "Erro ao salvar")
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("androidid", androidId);
                params.put("id_novena", novena.getId());
                params.put("causa", intencao);
                params.put("hora_lembrete", hora);
                params.put("cor", cor); // Adicionado aqui o parâmetro enviado ao backend PHP
                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }
    // =====================================================
// VELA
// =====================================================

    private void updateCandleByProgress(int diasConcluidos) {

        if (diasConcluidos < 1) {
            diasConcluidos = 1;
        }

        if (diasConcluidos > 9) {
            diasConcluidos = 9;
        }

        int index = diasConcluidos - 1;

        int heightPx =
                dp(candleHeights[index]);

        int offsetPx =
                dp(flameOffsets[index]);

        imgCandle.getLayoutParams().height =
                heightPx;

        imgWick.getLayoutParams().height =
                heightPx;

        imgFlame.setTranslationY(
                -(heightPx - offsetPx)
        );

        imgCandle.requestLayout();
        imgWick.requestLayout();
        imgFlame.requestLayout();
    }

    private void tintCandle(ImageView candle,
                            String colorHex) {

        Drawable drawable =
                candle.getDrawable();

        if (drawable != null) {

            drawable = drawable.mutate();

            drawable.setColorFilter(
                    Color.parseColor(colorHex),
                    PorterDuff.Mode.MULTIPLY
            );

            candle.setImageDrawable(drawable);
        }
    }

    private void startFlickerAnimation(ImageView flame) {

        ObjectAnimator rotate =
                ObjectAnimator.ofFloat(
                        flame,
                        "rotation",
                        -1.5f,
                        1.5f
                );

        rotate.setDuration(140);

        rotate.setRepeatCount(
                ValueAnimator.INFINITE
        );

        rotate.setRepeatMode(
                ValueAnimator.REVERSE
        );

        rotate.setInterpolator(
                new LinearInterpolator()
        );

        rotate.start();

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(
                        flame,
                        "scaleX",
                        0.97f,
                        1.03f
                );

        scaleX.setDuration(110);

        scaleX.setRepeatCount(
                ValueAnimator.INFINITE
        );

        scaleX.setRepeatMode(
                ValueAnimator.REVERSE
        );

        scaleX.start();
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
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