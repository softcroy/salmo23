package com.appsc.salmo23;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.biblia.NotificacaoVersciculo;
import com.appsc.salmo23.figurinhas.EntryActivity;
import com.appsc.salmo23.novos.PlayNotification;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.hawk.Hawk;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String FCM_PARAM_TITLE = "title";
    public static final String FCM_PARAM_BODY = "body";
    public static final String FCM_PARAM_ICON = "icon";
    public static final String FCM_PARAM_BIG = "picture";
    public static final String FCM_PARAM_LINK = "link";
    public static final String FCM_PARAM_IDIOMA = "idioma";
    public static final String FCM_PARAM_ID = "id";
    public static final String FCM_PARAM_APP = "app";

    public static final String FCM_PARAM_IDANTERIOR = "idanterior";
    public static final String FCM_PARAM_NOME = "nome";
    public static final String FCM_PARAM_ICONE = "icone";
    public static final String FCM_PARAM_IMAGE = "image";
    public static final String FCM_PARAM_ORACAO = "oracao";
    public static final String FCM_PARAM_SOND = "sond";
    public static final String FCM_PARAM_TIPO = "tipo";
    public static final String FCM_PARAM_VERSICULO_TEXT = "versciculo_text";

    Bitmap icone = null;
    private String chegou;
    private String chegou_oracao;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        chegou = "https://softcroy.com/app_santos_v2/api3/notification/chegou_lac_santos1.php?id=";
        chegou_oracao = "https://softcroy.com/app_santos_v2/api3/notification/lac_chegou_oracoes.php?id=";

        Map<String, String> data = remoteMessage.getData();
        Log.d("FROM", remoteMessage.getFrom());

        String appValue = data.get(FCM_PARAM_APP);
        if (appValue != null && (appValue.equals("santos_v2") || appValue.equals("todos"))) {

            if (data.get(FCM_PARAM_TIPO) != null && data.get(FCM_PARAM_TIPO).equals("versciculo")) {
                sendNotificationVersciculo(data);
                return;
            }

            if (data.get(FCM_PARAM_IDIOMA) != null) {
                if (data.get(FCM_PARAM_IDIOMA).equals("en")) {
                    if (Locale.getDefault().getLanguage().equals("en")) {
                        if (data.get(FCM_PARAM_TIPO) != null && data.get(FCM_PARAM_TIPO).equals("oracoes_english_v2")) {
                            sendNotificationOracao(data);
                        } else {
                            sendNotification(data);
                        }
                    }
                } else {
                    if (data.get(FCM_PARAM_TIPO) != null && data.get(FCM_PARAM_TIPO).equals("oracoes_v2")) {
                        sendNotificationOracao(data);
                    } else {
                        sendNotification(data);
                    }
                }
            }
        }
    }

    private void sendNotification(Map<String, String> data) {
        com.android.volley.RequestQueue mRequestQueue1 = Volley.newRequestQueue(MyFirebaseMessagingService.this.getApplicationContext());
        JsonObjectRequest jsonObjReq1 = new JsonObjectRequest(Request.Method.GET,
                chegou + data.get(FCM_PARAM_ID), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int socketTimeout1 = 30000;
        RetryPolicy policy1 = new DefaultRetryPolicy(socketTimeout1, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq1.setRetryPolicy(policy1);
        mRequestQueue1.add(jsonObjReq1);

        try {
            icone = BitmapFactory.decodeStream((InputStream) new URL(data.get(FCM_PARAM_ICON)).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, EntryActivity.class);
        intent.putExtra("ID_SEND_TO", data.get(FCM_PARAM_ID));
        intent.putExtra("LINK_SEND_TO", data.get(FCM_PARAM_LINK));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        String channelId = getString(R.string.notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_TITLE)))
                        .setContentText(StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_BODY)))
                        .setLargeIcon(icone)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        try {
            String picture = data.get(FCM_PARAM_BIG);
            if (picture != null && !"".equals(picture)) {
                URL url = new URL(picture);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(data.get(FCM_PARAM_BODY))
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationOracao(Map<String, String> data) {
        com.android.volley.RequestQueue mRequestQueue1 = Volley.newRequestQueue(MyFirebaseMessagingService.this.getApplicationContext());
        JsonObjectRequest jsonObjReq1 = new JsonObjectRequest(Request.Method.GET,
                chegou_oracao + data.get(FCM_PARAM_ID), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        int socketTimeout1 = 30000;
        RetryPolicy policy1 = new DefaultRetryPolicy(socketTimeout1, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq1.setRetryPolicy(policy1);
        mRequestQueue1.add(jsonObjReq1);

        try {
            icone = BitmapFactory.decodeStream((InputStream) new URL(data.get(FCM_PARAM_ICONE)).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, PlayNotification.class);
        intent.putExtra("NOME_SEND", data.get(FCM_PARAM_NOME));
        intent.putExtra("ID_SEND", data.get(FCM_PARAM_ID));
        intent.putExtra("IDANTERIOR_SEND", data.get(FCM_PARAM_IDANTERIOR));
        intent.putExtra("IMAGE_SEND", data.get(FCM_PARAM_IMAGE));
        intent.putExtra("ORACAO_SEND", data.get(FCM_PARAM_ORACAO));
        intent.putExtra("SOND_SEND", data.get(FCM_PARAM_SOND));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        String channelId = getString(R.string.notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_NOME)))
                        .setLargeIcon(icone)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        try {
            String picture = data.get(FCM_PARAM_IMAGE);
            if (picture != null && !"".equals(picture)) {
                URL url = new URL(picture);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(data.get(FCM_PARAM_ORACAO))
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendNotificationVersciculo(Map<String, String> data) {
        Context appContext = getApplicationContext();
        com.android.volley.RequestQueue mRequestQueue = Volley.newRequestQueue(appContext);

        if (!Hawk.isBuilt()) Hawk.init(appContext).build();

        // 1. REGISTRO DE RECEBIMENTO: Dispara assincronamente +1 para a coluna 'chegou'
        String idReg = data.get(FCM_PARAM_ID);
        if (idReg != null && !idReg.isEmpty()) {
            String urlUpdate = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/update_lac.php?id=" + idReg + "&acao=chegou";
            StringRequest requestChegou = new StringRequest(Request.Method.GET, urlUpdate,
                    response -> Log.d("FCM_BACKGROUND", "Computado com sucesso: chegou"),
                    error -> Log.e("FCM_BACKGROUND", "Erro de rede ao computar chegou"));
            mRequestQueue.add(requestChegou);
        }

        // 2. CAPTURA O ANDROID ID PARA VERIFICAÇÃO DE NOVENA ATIVA DO USUÁRIO
        String androidId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String urlChecarNovena = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/checar_novena_usuario.php?androidid=" + androidId;

        StringRequest novenaCheckRequest = new StringRequest(Request.Method.GET, urlChecarNovena,
                response -> {
                    final boolean[] possuiNovenaAtiva = {false};
                    final String[] tituloFinal = {data.get(FCM_PARAM_NOME)};
                    final String[] corpoFinal = {data.get(FCM_PARAM_VERSICULO_TEXT)};
                    final String[] urlImagemNovena = {""};

                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("status") && jsonResponse.getString("status").equals("ativa")) {
                            possuiNovenaAtiva[0] = true;

                            String nomeSanto = jsonResponse.optString("nome_novena", "Sua Novena");
                            String msgDias = jsonResponse.optString("mensagem_dias", "Continue sua novena!");
                            int proximoDia = jsonResponse.optInt("proximo_dia", 1);
                            urlImagemNovena[0] = jsonResponse.optString("imagem_novena", "");

                            boolean isAssinado = Hawk.get("Assinatura", "").equals("Assinado");
                            if (!isAssinado && proximoDia > 6) {
                                tituloFinal[0] = "Falta pouco para sua graça, " + nomeSanto + " te ouve!";
                                corpoFinal[0] = "Assine agora e continue sua caminhada santa para alcançar sua benção.";
                            } else {
                                tituloFinal[0] = "Lembrete: " + nomeSanto;
                                corpoFinal[0] = msgDias;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!possuiNovenaAtiva[0]) {
                        urlImagemNovena[0] = data.get(FCM_PARAM_IMAGE);
                    }

                    // 3. FAZ O DOWNLOAD DA IMAGEM E CONDICIONA A INSERÇÃO DE TEXTO
                    new Thread(() -> {
                        Bitmap imagemFinalNotificacao = null;
                        if (urlImagemNovena[0] != null && !urlImagemNovena[0].isEmpty()) {
                            try {
                                InputStream in = new URL(urlImagemNovena[0]).openStream();
                                Bitmap fundoOriginal = BitmapFactory.decodeStream(in);

                                if (fundoOriginal != null) {
                                    // TRAVA INTELIGENTE: Se for lembrete de novena ativa, NÃO desenha o texto por cima (deixa limpa)
                                    if (possuiNovenaAtiva[0]) {
                                        imagemFinalNotificacao = fundoOriginal;
                                    } else {
                                        // Se for Versículo Diário padrão, roda o Canvas e escreve o texto na imagem
                                        String textoDoVersiculo = StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_VERSICULO_TEXT));
                                        imagemFinalNotificacao = desenharTextoNaImagemDeNotificacao(fundoOriginal, textoDoVersiculo);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Mapeamento de clique para as intents
                        Intent intent;
                        if (possuiNovenaAtiva[0]) {
                            intent = new Intent(this, Principal_Oracao.class);
                            intent.putExtra("ABRIR_FRAGMENT_NOVENAS", true);
                        } else {
                            intent = new Intent(this, NotificacaoVersciculo.class);
                            intent.putExtra("id_registro", data.get(FCM_PARAM_ID));
                            intent.putExtra("versciculo", data.get(FCM_PARAM_VERSICULO_TEXT));
                            intent.putExtra("oracao", data.get(FCM_PARAM_ORACAO));
                            intent.putExtra("audio", data.get(FCM_PARAM_SOND));
                            intent.putExtra("image", urlImagemNovena[0]);
                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                        String channelId = getString(R.string.notification_channel_id);
                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(this, channelId)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(StringEscapeUtils.unescapeJava(tituloFinal[0]))
                                        .setContentText(StringEscapeUtils.unescapeJava(corpoFinal[0]))
                                        .setAutoCancel(true)
                                        .setSound(defaultSoundUri)
                                        .setContentIntent(pendingIntent);

                        if (imagemFinalNotificacao != null) {
                            notificationBuilder.setLargeIcon(imagemFinalNotificacao);
                            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(imagemFinalNotificacao)
                                    .setSummaryText(StringEscapeUtils.unescapeJava(corpoFinal[0])));
                        }

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(channelId,
                                    "Canal de Versículos",
                                    NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }

                        notificationManager.notify(2, notificationBuilder.build());
                    }).start();

                },
                error -> {
                    Log.e("FCM_BACKGROUND", "Erro ao checar novena ativa, usando fallback padrão de versículo.");
                    Intent intentFallback = new Intent(this, NotificacaoVersciculo.class);
                    intentFallback.putExtra("id_registro", data.get(FCM_PARAM_ID));
                    intentFallback.putExtra("versciculo", data.get(FCM_PARAM_VERSICULO_TEXT));
                    intentFallback.putExtra("oracao", data.get(FCM_PARAM_ORACAO));
                    intentFallback.putExtra("audio", data.get(FCM_PARAM_SOND));
                    intentFallback.putExtra("image", data.get(FCM_PARAM_IMAGE));
                    intentFallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PendingIntent pendingIntentFallback = PendingIntent.getActivity(this, 2, intentFallback,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_NOME)))
                                    .setContentText(StringEscapeUtils.unescapeJava(data.get(FCM_PARAM_VERSICULO_TEXT)))
                                    .setAutoCancel(true)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setContentIntent(pendingIntentFallback);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(2, notificationBuilder.build());
                });

        novenaCheckRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(novenaCheckRequest);
    }

    /**
     * FUNÇÃO AUXILIAR: Desenha o texto centralizado com overlay escuro por cima do Bitmap
     * antes de inflá-lo na bandeja de notificações.
     */
    private Bitmap desenharTextoNaImagemDeNotificacao(Bitmap fundoOriginal, String textoVersiculo) {
        int width = fundoOriginal.getWidth();
        int height = fundoOriginal.getHeight();

        Bitmap bitmapResultado = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResultado);

        // 1. Pinta a foto de fundo original
        Paint paintFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(fundoOriginal, 0, 0, paintFundo);

        // 2. Aplica filtro escuro de legibilidade (#73000000)
        Paint paintSombra = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSombra.setColor(Color.parseColor("#73000000"));
        paintSombra.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paintSombra);

        // 3. Desenha a linha dourada perimetral decorativa (#DDB04D) com cantos arredondados
        Paint paintBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorda.setColor(Color.parseColor("#DDB04D"));
        paintBorda.setStyle(Paint.Style.STROKE);
        float espessuraBorda = width * 0.015f;
        paintBorda.setStrokeWidth(espessuraBorda);

        float recuoPx = width * 0.02f;
        float raioPx = width * 0.04f;
        RectF areaBorda = new RectF(
                recuoPx + (espessuraBorda / 2),
                recuoPx + (espessuraBorda / 2),
                width - recuoPx - (espessuraBorda / 2),
                height - recuoPx - (espessuraBorda / 2)
        );
        canvas.drawRoundRect(areaBorda, raioPx, raioPx, paintBorda);

        // 4. Configuração do Texto para quebra automática de linhas
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(width * 0.055f);
        textPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        textPaint.setShadowLayer(4f, 2f, 2f, Color.parseColor("#CC000000"));

        if (textoVersiculo != null) {
            textoVersiculo = textoVersiculo.replaceAll("<[^>]*>", "");
        } else {
            textoVersiculo = "";
        }

        int paddingTexto = (int) (width * 0.09f);
        int larguraDisponivelTexto = width - (2 * paddingTexto);

        StaticLayout staticLayout = new StaticLayout(
                textoVersiculo, textPaint, larguraDisponivelTexto,
                Layout.Alignment.ALIGN_CENTER, 1.1f, 0.0f, false
        );

        canvas.save();
        float xTexto = paddingTexto;
        float yTexto = (height / 2f) - (staticLayout.getHeight() / 2f);
        canvas.translate(xTexto, yTexto);
        staticLayout.draw(canvas);
        canvas.restore();

        return bitmapResultado;
    }
}