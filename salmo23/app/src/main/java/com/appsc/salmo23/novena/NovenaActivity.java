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
import android.text.Html;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.bumptech.glide.Glide;
import com.appsc.salmo23.R;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NovenaActivity extends AppCompatActivity {

    private ImageView imgHeader;
    private TextView txtNome, txtPadroeiro, txtHistoria, txtLeitura, txtConcluido, txtCompartilhado;
    private TextView txtDataTradicional, txtDiaFesta;
    private Button btnIniciar;
    private ImageView btnShare;

    private String androidId;
    private String idNovenaAtual = "";
    private String dataInicioUser = "";
    private int diasConcluidosUser = 0;
    private Novena novenaSelecionada;
    // ======================
// VELA
// ======================

    private String selectedCandleColor = "#E0E0E0";

    private final int[] candleHeights = {
            170, 150, 130, 110, 92, 75, 58, 40, 25
    };

    private final int[] flameOffsets = {
            122, 115, 107, 100, 93, 86, 79, 73, 67
    };

    private int currentDay = 1;
    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    private final String URL_GET_PROGRESSO = "https://softcroy.com/app_santos_v2/novena/get_novenas.php?androidid=";
    private final String URL_UPDATE_INICIAR = "https://softcroy.com/app_santos_v2/novena/iniciar_novena.php";
    private final String URL_REGISTRAR_ACAO = "https://softcroy.com/app_santos_v2/novena/registrar_acao.php";

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 1001;
    private String intencaoPendente = "";
    private String horaPendente = "";
    private AlertDialog dialogIntencaoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novena);

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(getString(R.string.descricao));
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

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        inicializarComponentes();

        if (getIntent().hasExtra("novena_selecionada")) {
            novenaSelecionada = (Novena) getIntent().getSerializableExtra("novena_selecionada");
            if (novenaSelecionada != null) {
                idNovenaAtual = novenaSelecionada.getId();
                buscarProgressoEContadores();
            }
        }


        btnShare.setOnClickListener(v -> {
            registrarAcaoGlobal("compartilhado");
            compartilharNovena();
        });
    }

    private void inicializarComponentes() {
        imgHeader = findViewById(R.id.imgNovenaHeader);
        txtNome = findViewById(R.id.txtNomeNovena);
        txtPadroeiro = findViewById(R.id.txtPadroeiroDetalhe);
        txtHistoria = findViewById(R.id.txtHistoria);
        txtDataTradicional = findViewById(R.id.txtDataTradicional);
        txtDiaFesta = findViewById(R.id.txtDiaFesta);
        txtLeitura = findViewById(R.id.countLeitura);
        txtConcluido = findViewById(R.id.countConcluido);
        txtCompartilhado = findViewById(R.id.countCompartilhado);
        btnIniciar = findViewById(R.id.btnIniciarNovena);
        btnShare = findViewById(R.id.btnShareNovena);
    }

    private void buscarProgressoEContadores() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_GET_PROGRESSO + androidId, null,
                response -> {
                    try {
                        JSONArray lista = response.getJSONArray("lista_novenas");
                        for (int i = 0; i < lista.length(); i++) {
                            JSONObject nObj = lista.getJSONObject(i);
                            if (nObj.getString("id").equals(idNovenaAtual)) {
                                txtLeitura.setText(nObj.optString("leitura_count", "0"));
                                txtConcluido.setText(nObj.optString("concluido_count", "0"));
                                txtCompartilhado.setText(nObj.optString("compartilhado_count", "0"));

                                // Sincroniza o Padroeiro se ele estiver vazio no objeto local
                                if (novenaSelecionada.getPadroeiro() == null || novenaSelecionada.getPadroeiro().isEmpty()) {
                                    novenaSelecionada.setPadroeiro(nObj.optString("padroeiro", ""));
                                }

                                if (novenaSelecionada.getDataTradicional() == null || novenaSelecionada.getDataTradicional().isEmpty()) {
                                    novenaSelecionada.setDataTradicional(nObj.optString("data_tradicional", "---"));
                                    novenaSelecionada.setDiaFesta(nObj.optString("dia_festa", "---"));
                                }
                                break;
                            }
                        }

                        JSONArray progresso = response.getJSONArray("progresso_usuario");
                        dataInicioUser = "";
                        diasConcluidosUser = 0;
                        for (int i = 0; i < progresso.length(); i++) {
                            JSONObject p = progresso.getJSONObject(i);
                            if (p.getString("id da novena").equals(idNovenaAtual)) {
                                dataInicioUser = p.optString("data de início", "");
                                if(dataInicioUser.isEmpty()) dataInicioUser = p.optString("data de inu00edcio", "");

                                diasConcluidosUser = p.optInt("dias concluídos", 0);
                                if(diasConcluidosUser == 0) diasConcluidosUser = p.optInt("dias concluu00eddos", 0);
                                break;
                            }
                        }
                        configurarUI();
                    } catch (JSONException e) {
                        Log.e("NovenaActivity", "Erro no JSON: " + e.getMessage());
                        configurarUI();
                    }
                }, error -> configurarUI());
        Volley.newRequestQueue(this).add(request);
    }

    private void configurarUI() {
        if (novenaSelecionada == null) return;

        txtNome.setText(novenaSelecionada.getNome());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtHistoria.setText(Html.fromHtml(novenaSelecionada.getHistoria(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            txtHistoria.setText(Html.fromHtml(novenaSelecionada.getHistoria()));
        }

        // Exibe o padroeiro/causas do santo com segurança
        if (novenaSelecionada.getPadroeiro() != null && !novenaSelecionada.getPadroeiro().isEmpty()) {
            txtPadroeiro.setVisibility(View.VISIBLE);
            txtPadroeiro.setText("Causas: "+novenaSelecionada.getPadroeiro());
        } else {
            txtPadroeiro.setVisibility(View.GONE);
        }

        String tradicao = novenaSelecionada.getDataTradicional();
        String festa = novenaSelecionada.getDiaFesta();

        txtDataTradicional.setText((tradicao != null && !tradicao.isEmpty() ? tradicao : "---"));
        txtDiaFesta.setText((festa != null && !festa.isEmpty() ? festa : "---"));

        Glide.with(this).load(novenaSelecionada.getImagem()).into(imgHeader);

        if (!dataInicioUser.isEmpty() && diasConcluidosUser < 9) {
            btnIniciar.setText("CONTINUAR NOVENA");
            btnIniciar.setOnClickListener(v -> abrirProgresso());
        } else {
            btnIniciar.setText("INICIAR NOVENA");
            btnIniciar.setOnClickListener(v -> mostrarDialogoIntencao());
        }
    }

    private void mostrarDialogoIntencao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_intencao, null);
        builder.setView(dialogView);

        EditText edtIntencao = dialogView.findViewById(R.id.edtIntencao);
        TimePicker timePicker = dialogView.findViewById(R.id.timePickerAlarme);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarIntencao);
        ImageButton btnFechar = dialogView.findViewById(R.id.btnFecharDialog);

        timePicker.setIs24HourView(true);

        try {
            int minuteId = Resources.getSystem().getIdentifier("minute", "id", "android");
            View minuteView = timePicker.findViewById(minuteId);
            if (minuteView != null) minuteView.setVisibility(View.GONE);

            int dividerId = Resources.getSystem().getIdentifier("divider", "id", "android");
            View dividerView = timePicker.findViewById(dividerId);
            if (dividerView != null) dividerView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(0);
        } else {
            timePicker.setCurrentMinute(0);
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        this.dialogIntencaoRef = dialog; // Salva a referência interna

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            if (minute != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.setMinute(0);
                } else {
                    timePicker.setCurrentMinute(0);
                }
            }
            btnConfirmar.setEnabled(true);
            btnConfirmar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#35AEFF")));
            btnConfirmar.setText("INICIAR AGORA");
        });

        btnFechar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            String intencao = edtIntencao.getText().toString().trim();

            int hora = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    ? timePicker.getHour()
                    : timePicker.getCurrentHour();

            if (!intencao.isEmpty()) {
                String horaFormatada = String.format(Locale.getDefault(), "%02d:00", hora);

                // Guarda os dados nas variáveis globais temporárias antes de validar a permissão
                this.intencaoPendente = intencao;
                this.horaPendente = horaFormatada;

                // NOVA FUNÇÃO DE VALIDAÇÃO DE PERMISSÃO
                verificarPermissaoEProsseguir();

            } else {
                Toast.makeText(this, "Escreva sua intenção", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // Método auxiliar para isolar a regra de negócio da permissão
    private void verificarPermissaoEProsseguir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                // Dispara diretamente o pop-up de permissão nativo do Android
                solicitarPermissaoSistema();
            } else {
                // Já tem a permissão, segue para salvar e abrir a vela
                avancarParaVela();
            }
        } else {
            // Android 12 ou inferior não precisa pedir permissão em tempo de execução
            avancarParaVela();
        }
    }

    private void solicitarPermissaoSistema() {
        androidx.core.app.ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_NOTIFICATIONS);
    }

    private void avancarParaVela() {
        if (dialogIntencaoRef != null && dialogIntencaoRef.isShowing()) {
            dialogIntencaoRef.dismiss();
        }
        mostrarDialogoVela(intencaoPendente, horaPendente);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Usuário aceitou! Avança para salvar
                avancarParaVela();
            } else {
                // Usuário recusou. Vamos checar se o Android bloqueou o pop-up nativo
                boolean podeMostrarPopupNovamente = androidx.core.app.ActivityCompat
                        .shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permissão Necessária")
                        .setCancelable(false);

                if (podeMostrarPopupNovamente) {
                    // Primeira recusa: Ainda podemos tentar mostrar o pop-up nativo
                    builder.setMessage("O lembrete diário depende das notificações para avisar você no horário correto da sua reza. Por favor, permita o acesso.")
                            .setPositiveButton("TENTAR NOVAMENTE", (dialog, which) -> {
                                dialog.dismiss();
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                    solicitarPermissaoSistema();
                                }, 400);
                            });
                } else {
                    // Segunda recusa (Bloqueio do Android): O pop-up nativo não abre mais, precisamos enviar para as configurações
                    builder.setMessage("O sistema Android bloqueou o aviso de permissão por segurança. Para continuar e ativar seu lembrete, você precisará ativar as notificações nas configurações do seu celular.")
                            .setPositiveButton("IR PARA CONFIGURAÇÕES", (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Não foi possível abrir as configurações.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                builder.setNegativeButton("AGORA NÃO", null);
                builder.show();
            }
        }
    }
    private void iniciarNovenaNoBanco(String intencao,
                                      String horaLembrete,
                                      boolean vela,
                                      String corVela) {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "d 'de' MMMM 'de' yyyy 'às' HH:mm",
                new Locale("pt", "BR")
        );

        String dataFormatada = sdf.format(new Date());

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                URL_UPDATE_INICIAR,

                response -> {

                    registrarAcaoGlobal("leitura");

                    Toast.makeText(this,
                            "Novena iniciada com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    abrirProgresso();
                },

                error -> Toast.makeText(this,
                        "Erro de conexão",
                        Toast.LENGTH_SHORT).show()

        ) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();

                params.put("androidid", androidId);
                params.put("id_novena", idNovenaAtual);
                params.put("causa", intencao);
                params.put("data_inicio", dataFormatada);
                params.put("hora_lembrete", horaLembrete);

                // ======================
                // VELA
                // ======================

                params.put("vela", String.valueOf(vela));
                params.put("cor_vela", corVela);

                return params;
            }
        };

        Volley.newRequestQueue(this).add(postRequest);
    }

    private void registrarAcaoGlobal(String acao) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_REGISTRAR_ACAO,
                response -> Log.d("ACAO_GLOBAL", acao + " registrada. Resposta: " + response),
                error -> Log.e("ACAO_GLOBAL", "Erro ao registrar " + acao)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("androidid", androidId); // <--- LINHA ADICIONADA
                params.put("id_novena", idNovenaAtual);
                params.put("acao", acao);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void compartilharNovena() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Estou rezando a Novena de " + novenaSelecionada.getNome() + " pela minha intenção. Reze comigo! Baixe o App na Play Store. "+ "https://play.google.com/store/apps/details?id=" + NovenaActivity.this.getPackageName());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Compartilhar Novena"));
    }

    private void abrirProgresso() {
        Intent intent = new Intent(this, NovenaActivityProgresso.class);
        intent.putExtra("novena_selecionada", novenaSelecionada);
        startActivity(intent);
        finish();
    }
    // =====================================================
// DIALOGO DA VELA
// =====================================================

    private void mostrarDialogoVela(String intencao,
                                    String horaLembrete) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this)
                .inflate(R.layout.dialog_candle, null);

        builder.setView(layout);

        ImageView candle =
                layout.findViewById(R.id.dialog_candle);

        ImageView wick =
                layout.findViewById(R.id.dialog_wick);

        ImageView flame =
                layout.findViewById(R.id.dialog_flame);

        Button btnConcluir =
                layout.findViewById(R.id.btnConcluirVela);

        Button btnPular =
                layout.findViewById(R.id.btnPularVela);

        flame.setAlpha(1f);

        startFlickerAnimation(flame);

        setupPalette(layout, candle);

        updateCandleVisual(candle, wick, flame);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        btnConcluir.setOnClickListener(v -> {

            iniciarNovenaNoBanco(
                    intencao,
                    horaLembrete,
                    true,
                    selectedCandleColor
            );

            dialog.dismiss();
        });

        btnPular.setOnClickListener(v -> {

            iniciarNovenaNoBanco(
                    intencao,
                    horaLembrete,
                    false,
                    ""
            );

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCandleVisual(ImageView candle,
                                    ImageView wick,
                                    ImageView flame) {

        int heightPx =
                dp(candleHeights[currentDay - 1]);

        int offsetPx =
                dp(flameOffsets[currentDay - 1]);

        candle.getLayoutParams().height = heightPx;
        wick.getLayoutParams().height = heightPx;

        flame.setTranslationY(-(heightPx - offsetPx));

        candle.requestLayout();
        wick.requestLayout();
        flame.requestLayout();
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

        rotate.setRepeatMode(ValueAnimator.REVERSE);

        rotate.setRepeatCount(ValueAnimator.INFINITE);

        rotate.setInterpolator(new LinearInterpolator());

        rotate.start();

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(
                        flame,
                        "scaleX",
                        0.97f,
                        1.03f
                );

        scaleX.setDuration(110);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.setRepeatMode(ValueAnimator.REVERSE);

        scaleX.start();
    }

    private void setupPalette(View layout,
                              ImageView candle) {

        View.OnClickListener colorClick = v -> {

            int id = v.getId();

            if (id == R.id.colorWhite)
                selectedCandleColor = "#E0E0E0";

            else if (id == R.id.colorRed)
                selectedCandleColor = "#C62828";

            else if (id == R.id.colorBlue)
                selectedCandleColor = "#1565C0";

            else if (id == R.id.colorGreen)
                selectedCandleColor = "#2E7D32";

            else if (id == R.id.colorPurple)
                selectedCandleColor = "#6A1B9A";

            else if (id == R.id.colorYellow)
                selectedCandleColor = "#FFDE21";

            else if (id == R.id.colorPink)
                selectedCandleColor = "#FF1D8D";

            else if (id == R.id.colorBlack)
                selectedCandleColor = "#000000";

            tintCandle(candle, selectedCandleColor);
        };

        layout.findViewById(R.id.colorWhite)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorRed)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorBlue)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorGreen)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorPurple)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorYellow)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorPink)
                .setOnClickListener(colorClick);

        layout.findViewById(R.id.colorBlack)
                .setOnClickListener(colorClick);
    }

    private void tintCandle(ImageView candle,
                            String colorHex) {

        Drawable drawable = candle.getDrawable();

        if (drawable != null) {

            drawable = drawable.mutate();

            drawable.setColorFilter(
                    Color.parseColor(colorHex),
                    PorterDuff.Mode.MULTIPLY
            );

            candle.setImageDrawable(drawable);
        }
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // Método utilitário para verificar trava de 24h entre os dias
    public static boolean estaBloqueadoPorData(String dataInicio, int position) {
        if (dataInicio == null || dataInicio.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR"));
            Date dataIni = sdf.parse(dataInicio);
            Date dataAtual = new Date();
            if (dataIni != null) {
                long diffMilissegundos = dataAtual.getTime() - dataIni.getTime();
                long horasPassadas = diffMilissegundos / (1000 * 60 * 60);
                return horasPassadas < (position * 24);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
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
    public void onResume() {
        super.onResume();
        esconderBotoesFisicos();
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