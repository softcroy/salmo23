package com.appsc.salmo23.biblia;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;
import com.appsc.salmo23.login.SplashActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificacaoVersciculo extends AppCompatActivity {

    private TextView txtVersiculo, txtOracao, txtReferencia;
    private ImageView imgNovenaHeader;
    private Button btnAmen;
    private ImageButton btnFechar, btnCopiar, btnCompartilhar;
    private ToggleButton tgMusic;
    private String idRegistro;
    private MediaPlayer mediaPlayer;

    // Guarda o bitmap original para renderização gráfica na partilha
    private Bitmap bitmapFundoSorteado = null;

    private String livroExtraido = "";
    private int capituloExtraido = 1;
    private int versiculoExtraido = 1;

    private final String URL_API = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/update_lac.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao_versciculo);

        imgNovenaHeader = findViewById(R.id.imgNovenaHeader);
        txtVersiculo = findViewById(R.id.txtVersiculo);
        txtReferencia = findViewById(R.id.ir_para_versciculo);
        txtOracao = findViewById(R.id.txtHistoria);

        btnAmen = findViewById(R.id.btnAmen);
        btnFechar = findViewById(R.id.fechar);
        btnCopiar = findViewById(R.id.copiar);
        btnCompartilhar = findViewById(R.id.compartilhar);
        tgMusic = findViewById(R.id.tgMusic);

        if (txtReferencia != null) {
            txtReferencia.setPaintFlags(txtReferencia.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idRegistro = extras.getString("id_registro");
            String versiculoText = extras.getString("versciculo");
            String oracaoText = extras.getString("oracao");
            String audioUrl = extras.getString("audio");
            String imageUrl = extras.getString("image");

            if (versiculoText != null && versiculoText.contains(" - ")) {
                String[] partes = versiculoText.split(" - ", 2);
                txtReferencia.setText(partes[0]);
                txtVersiculo.setText(partes[1]);
                processarReferenciaBiblica(partes[0]);
            } else {
                txtVersiculo.setText(versiculoText);
            }

            txtOracao.setText(oracaoText);

            if (txtVersiculo != null) {
                txtVersiculo.setTextColor(android.graphics.Color.WHITE);
            }

            // -----------------------------------------------------------------
            // CARREGAMENTO DINÂMICO DA IMAGEM DO TOPO DA TELA
            // -----------------------------------------------------------------
            if (imageUrl != null && !imageUrl.isEmpty() && imgNovenaHeader != null) {
                new Thread(() -> {
                    try {
                        InputStream in = new URL(imageUrl).openStream();
                        bitmapFundoSorteado = BitmapFactory.decodeStream(in);

                        if (bitmapFundoSorteado != null) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                imgNovenaHeader.setImageBitmap(bitmapFundoSorteado);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("IMG_HEADER_ERROR", "Falha ao baixar imagem de fundo: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
            }

            atualizarControle("visualisao");

            if (audioUrl != null && !audioUrl.isEmpty() && !audioUrl.equalsIgnoreCase("null")) {
                tocarAudio(audioUrl);
            }
        }

        if (txtReferencia != null) {
            txtReferencia.setOnClickListener(v -> {
                if (!livroExtraido.isEmpty()) {
                    Intent intent = new Intent(NotificacaoVersciculo.this, ReadingActivity.class);
                    intent.putExtra("LIVRO", livroExtraido);
                    intent.putExtra("CAPITULO", capituloExtraido);
                    intent.putExtra("VERSICULO", versiculoExtraido);
                    intent.putExtra("ID_SEND_TO", idRegistro);
                    startActivity(intent);
                } else {
                    Toast.makeText(NotificacaoVersciculo.this, "Erro ao processar dados de leitura.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnAmen.setOnClickListener(v -> {
            atualizarControle("amen");
            Toast.makeText(NotificacaoVersciculo.this, "Amém! Oração confirmada.", Toast.LENGTH_SHORT).show();
            voltarParaEntry();
        });

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> voltarParaEntry());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                voltarParaEntry();
            }
        });

        if (btnCopiar != null) {
            btnCopiar.setOnClickListener(v -> {
                String textoParaCopiar = txtReferencia.getText().toString() + "\n" +
                        txtVersiculo.getText().toString() + "\n\n" +
                        txtOracao.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Versículo e Oração", textoParaCopiar);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(NotificacaoVersciculo.this, "Texto copiado!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // -----------------------------------------------------------------
        // EVENTO DE COMPARTILHAMENTO EM IMAGEM PERSONALIZADA
        // -----------------------------------------------------------------
        if (btnCompartilhar != null) {
            btnCompartilhar.setOnClickListener(v -> {
                if (bitmapFundoSorteado == null) {
                    Toast.makeText(this, "Aguarde o download da imagem de fundo...", Toast.LENGTH_SHORT).show();
                    return;
                }

                atualizarControle("compartilhado");

                String textoImagem = txtVersiculo.getText().toString() + "\n\n" + txtReferencia.getText().toString();

                Bitmap imagemCustomizada = gerarImagemEstilizada(bitmapFundoSorteado, textoImagem);

                if (imagemCustomizada != null) {
                    enviarImagemNativa(imagemCustomizada);
                }
            });
        }

        if (tgMusic != null) {
            tgMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mediaPlayer != null) {
                    if (isChecked) {
                        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                }
            });
        }
    }

    /**
     * Renderiza em memória a imagem contendo os textos e elementos visuais com cantos arredondados e margens
     */
    private Bitmap gerarImagemEstilizada(Bitmap fundoOriginal, String textoAnexado) {
        int width = fundoOriginal.getWidth();
        int height = fundoOriginal.getHeight();

        Bitmap bitmapResultado = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResultado);

        // 1. Desenha a foto de fundo sorteada (.webp)
        Paint paintFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(fundoOriginal, 0, 0, paintFundo);

        // 2. Cria a camada escura de contraste (#73000000)
        Paint paintSombra = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSombra.setColor(Color.parseColor("#73000000"));
        paintSombra.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paintSombra);

        // Converte as dimensões baseando-se na densidade de pixels do dispositivo para precisão
        float densidadeGeral = getResources().getDisplayMetrics().density;
        float margemAfastamentoPx = 4f * densidadeGeral; // Distância de 4sp da extremidade
        float raioCantosPx = 24f * densidadeGeral;       // Cantos arredondados de 24dp
        float paddingMargemPx = 14f * densidadeGeral;    // Margem interna padrão

        // 3. Desenha a linha perimetral dourada (#DDB04D) com cantos arredondados e recuo
        Paint paintBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorda.setColor(Color.parseColor("#DDB04D"));
        paintBorda.setStyle(Paint.Style.STROKE);

        float espessuraBorda = 2f * densidadeGeral;
        paintBorda.setStrokeWidth(espessuraBorda);

        RectF areaBordaComMargem = new RectF(
                margemAfastamentoPx + (espessuraBorda / 2),
                margemAfastamentoPx + (espessuraBorda / 2),
                width - margemAfastamentoPx - (espessuraBorda / 2),
                height - margemAfastamentoPx - (espessuraBorda / 2)
        );
        canvas.drawRoundRect(areaBordaComMargem, raioCantosPx, raioCantosPx, paintBorda);

        // 4. Configuração e escrita do versículo centralizado
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(width * 0.055f);
        textPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        textPaint.setShadowLayer(4f, 2f, 2f, Color.parseColor("#CC000000"));

        int paddingTexto = (int) (margemAfastamentoPx + espessuraBorda + (width * 0.06f));
        int larguraUtilTexto = width - (2 * paddingTexto);

        StaticLayout staticLayout = new StaticLayout(
                disabledHtmlString(textoAnexado), textPaint, larguraUtilTexto,
                Layout.Alignment.ALIGN_CENTER, 1.1f, 0.0f, false
        );

        canvas.save();
        float xTexto = paddingTexto;
        float yTexto = (height / 2f) - (staticLayout.getHeight() / 2f);
        canvas.translate(xTexto, yTexto);
        staticLayout.draw(canvas);
        canvas.restore();

        // 5. RENDERIZAÇÃO DO RODAPÉ: SOLUÇÃO COMPATIVEL COM ICONES ADAPTATIVOS (XML)
        float xInicioRodape = margemAfastamentoPx + espessuraBorda + paddingMargemPx;
        float yBaseRodape = height - margemAfastamentoPx - espessuraBorda - paddingMargemPx;

        int tamanhoIcone = (int) (width * 0.075f); // 7.5% proporcional da largura da imagem
        float xAvancoTexto = xInicioRodape;

        Bitmap iconeAppConvertido = obterBitmapDeDrawable(this, R.mipmap.ic_launcher, tamanhoIcone);

        if (iconeAppConvertido != null) {
            float yIcone = yBaseRodape - tamanhoIcone;

            // 1º: Desenha o ícone vetorizado na esquerda
            canvas.drawBitmap(iconeAppConvertido, xInicioRodape, yIcone, null);

            // 2º: Desloca a escrita do texto para o lado do ícone
            xAvancoTexto = xInicioRodape + tamanhoIcone + (width * 0.025f);
        }

        TextPaint paintRodapeTexto = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paintRodapeTexto.setColor(Color.WHITE);
        paintRodapeTexto.setTextSize(width * 0.045f);
        paintRodapeTexto.setTypeface(Typeface.create("serif", Typeface.BOLD));

        // Pega dinamicamente o nome real do aplicativo definido no strings.xml (R.string.app_name)
        String nomeDoAplicativoString = getString(R.string.app_name);

        // AJUSTE: Calcula o espaço máximo que sobrou até a borda dourada direita para cortar com reticências se vazar
        float xLimiteDireitoDourado = width - margemAfastamentoPx - espessuraBorda - paddingMargemPx;
        float larguraMaximaDisponivelParaTexto = xLimiteDireitoDourado - xAvancoTexto;

        // Trunca o texto automaticamente caso ele ultrapasse o limite disponível
        String nomeDoAppFormatado = TextUtils.ellipsize(
                nomeDoAplicativoString,
                paintRodapeTexto,
                larguraMaximaDisponivelParaTexto,
                TextUtils.TruncateAt.END
        ).toString();

        Rect limitesTextoRodape = new Rect();
        paintRodapeTexto.getTextBounds(nomeDoAppFormatado, 0, nomeDoAppFormatado.length(), limitesTextoRodape);

        float yTextoAlinhadoComIcone = yBaseRodape - (tamanhoIcone / 2f) + (limitesTextoRodape.height() / 2f);

        // 3º: Desenha o nome do app cortado de forma segura à direita do ícone
        canvas.drawText(nomeDoAppFormatado, xAvancoTexto, yTextoAlinhadoComIcone, paintRodapeTexto);

        return bitmapResultado;
    }

    /**
     * Extrai com segurança qualquer tipo de ícone (AdaptiveIcon/Vetor XML/PNG)
     */
    private Bitmap obterBitmapDeDrawable(Context context, int drawableId, int tamanhoDesejado) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (drawable == null) return null;

            Bitmap bitmap = Bitmap.createBitmap(tamanhoDesejado, tamanhoDesejado, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e("CONVERT_ICON_ERROR", "Falha ao processar ícone adaptativo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Remove tags HTML residuais se houver
     */
    private String disabledHtmlString(String input) {
        if (input == null) return "";
        return input.replaceAll("<[^>]*>", "");
    }

    /**
     * Salva o arquivo temporário na pasta externa de cache e aciona a Intent com o Link da Play Store anexado
     */
    private void enviarImagemNativa(Bitmap imagemPronta) {
        try {
            File pastaImagens = new File(getExternalCacheDir(), "images");
            if (!pastaImagens.exists()) {
                pastaImagens.mkdirs();
            }

            File arquivoPng = new File(pastaImagens, "versiculo_partilha.png");
            FileOutputStream saidaStream = new FileOutputStream(arquivoPng);
            imagemPronta.compress(Bitmap.CompressFormat.PNG, 100, saidaStream);
            saidaStream.flush();
            saidaStream.close();

            Uri uriSegura = FileProvider.getUriForFile(
                    this,
                    "com.appsc.salmo23.provider",
                    arquivoPng
            );

            if (uriSegura != null) {
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("image/png");

                // AJUSTE: Injeta o arquivo de imagem no stream
                intentShare.putExtra(Intent.EXTRA_STREAM, uriSegura);

                // AJUSTE: Envia junto o link oficial do aplicativo da Play Store em texto puro
                intentShare.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + this.getPackageName());

                intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intentShare, "Compartilhar por:"));
            }
        } catch (IOException e) {
            Log.e("SHARE_CANVAS_ERROR", "Erro ao processar imagem para envio: " + e.getMessage());
            Toast.makeText(this, "Não foi possível estruturar a imagem para partilha.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void processarReferenciaBiblica(String referenciaBruta) {
        try {
            Pattern pattern = Pattern.compile("^(.+?)\\s+(\\d+):(\\d+)$");
            Matcher matcher = pattern.matcher(referenciaBruta.trim());

            if (matcher.find()) {
                livroExtraido = matcher.group(1);
                capituloExtraido = Integer.parseInt(matcher.group(2));
                versiculoExtraido = Integer.parseInt(matcher.group(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
            livroExtraido = "Gênesis";
            capituloExtraido = 1;
            versiculoExtraido = 1;
        }
    }

    private void voltarParaEntry() {
        Intent intentEntry = new Intent(NotificacaoVersciculo.this, SplashActivity.class);
        intentEntry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentEntry);
        finish();
    }

    private void atualizarControle(String coluna) {
        if (idRegistro == null || idRegistro.isEmpty()) return;

        String urlCompleta = URL_API + "?id=" + idRegistro + "&acao=" + coluna;
        StringRequest request = new StringRequest(Request.Method.GET, urlCompleta,
                response -> Log.d("BD_CONTROLE", "Sucesso: " + coluna),
                error -> Log.e("BD_CONTROLE", "Erro ao computar: " + error.getMessage()));

        Volley.newRequestQueue(this).add(request);
    }

    private void tocarAudio(String url) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (bitmapFundoSorteado != null) {
            bitmapFundoSorteado.recycle();
            bitmapFundoSorteado = null;
        }
    }
}