package com.appsc.salmo23.figurinhas;

import static com.appsc.salmo23.R.id.txt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

public class EntryActivity extends BaseActivity {
    private Handler handler1 = new Handler(Looper.getMainLooper());
    private RequestQueue requestQueue;
    private static final String DEFAULT_STRING_VALUE = "";

    public static String monetizar    = DEFAULT_STRING_VALUE;
    public static String versao       = DEFAULT_STRING_VALUE;
    public static String titlo        = DEFAULT_STRING_VALUE;
    public static String mensagem     = DEFAULT_STRING_VALUE;
    public static String novo_link    = DEFAULT_STRING_VALUE;

    private View progressBar;
    private LoadListAsyncTask loadListAsyncTask;
    private TextView mTextView;
    private static final String TAG = EntryActivity.class.getSimpleName();
    private String downloadBase = "https://softcroy.com/app_santos_v2/api/notification/download_lac_santos1.php?id=";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        requestQueue = Volley.newRequestQueue(this);

        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseMessaging.getInstance().subscribeToTopic("versciculo");
        FirebaseMessaging.getInstance().subscribeToTopic("oracoes_v2");
        FirebaseMessaging.getInstance().subscribeToTopic("oracoes_english_v2");

        mTextView = findViewById(txt);
        if (mTextView != null) mTextView.setText(R.string.subscribed);

        // Processar links de notificação
        String link = getIntent() != null ? getIntent().getStringExtra("LINK_SEND_TO") : null;
        if (link != null && link.contains("https")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(Intent.createChooser(i, "Open With"));
        }

        // Log de notificação (silencioso)
        String idSendTo = getIntent() != null ? getIntent().getStringExtra("ID_SEND_TO") : "";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, downloadBase + idSendTo, null,
                response -> Log.d(TAG, "Notification logged"),
                error -> Log.e(TAG, "Notification error"));

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1.0f));
        requestQueue.add(jsonObjReq);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        progressBar = findViewById(R.id.entry_activity_progress);

        // Inicia o carregamento da lista
        loadListAsyncTask = new LoadListAsyncTask(this);
        loadListAsyncTask.execute();

        fetchAppConfig();
        TextView myAwesomeTextView = findViewById(R.id.textView2);
        Calendar c = Calendar.getInstance();
        int hours = c.get(Calendar.HOUR_OF_DAY);
        String nome = getIntent().getStringExtra("NOMEUSER");



        if (hours >= 0 && hours <= 11) {
            setGreeting(myAwesomeTextView, R.string.bom_dia_aben_oado_a, nome);
        } else if (hours >= 12 && hours <= 17) {
            setGreeting(myAwesomeTextView, R.string.boa_tarde_aben_oado_a, nome);
        } else {
            setGreeting(myAwesomeTextView, R.string.boa_noite_aben_oado_a, nome);
        }

        overridePendingTransition(0, 0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
    private void setGreeting(TextView tv, int stringResId, String nome) {
        String texto = getString(stringResId) + " " + nome;
        tv.setText(texto);
        tv.setTextSize(30);
        tv.setTextColor(Color.WHITE);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);
    }

    private void fetchAppConfig() {
        String apiUrl = "https://softcroy.com/app_santos_v2/api/config/api_aplicativos2_config.php?app=" + getPackageName();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiUrl, null,
                this::processResponse,
                error -> Log.e(TAG, "Config Request Error"));
        requestQueue.add(jsonArrayRequest);
    }

    private synchronized void processResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                monetizar = jsonObject.optString("monetizar", DEFAULT_STRING_VALUE);
                versao = jsonObject.optString("versao", DEFAULT_STRING_VALUE);
                titlo = jsonObject.optString("titlo", DEFAULT_STRING_VALUE);
                mensagem = jsonObject.optString("mensagem", DEFAULT_STRING_VALUE);
                novo_link = jsonObject.optString("novo_link", DEFAULT_STRING_VALUE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showStickerPack(ArrayList<StickerPack> stickerPackList) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);

        if (stickerPackList != null && !stickerPackList.isEmpty()) {
            final Intent intent = new Intent(this, Principal_Oracao.class);
            intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA, stickerPackList);
            intent.putExtra("EXTRA_SHOW_UP_BUTTON", false);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            showErrorMessage("Nenhum pacote encontrado.");
        }
    }

    private void showErrorMessage(String errorMessage) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        final TextView errorMessageTV = findViewById(R.id.error_message);
        if (errorMessageTV != null) {
            errorMessageTV.setVisibility(View.VISIBLE);
            errorMessageTV.setText(getString(R.string.error_message, errorMessage));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadListAsyncTask != null) loadListAsyncTask.cancel(true);
        handler1.removeCallbacksAndMessages(null);
    }

    static class LoadListAsyncTask extends AsyncTask<Void, Void, Pair<String, ArrayList<StickerPack>>> {
        private final WeakReference<EntryActivity> contextWeakReference;

        LoadListAsyncTask(EntryActivity activity) {
            this.contextWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Pair<String, ArrayList<StickerPack>> doInBackground(Void... voids) {
            try {
                EntryActivity activity = contextWeakReference.get();
                if (activity == null) return new Pair<>("Activity destroyed", null);

                // Carrega a estrutura dos pacotes (JSON)
                ArrayList<StickerPack> stickerPackList = StickerPackLoader.fetchStickerPacks(activity);

                if (stickerPackList.isEmpty()) {
                    return new Pair<>("could not find any packs", null);
                }

                // VALIDAÇÃO RÁPIDA: Apenas metadados, sem baixar as figurinhas agora
                for (StickerPack stickerPack : stickerPackList) {
                    StickerPackValidator.verifyPackMetadataValidity(activity, stickerPack);
                }

                return new Pair<>(null, stickerPackList);
            } catch (Exception e) {
                return new Pair<>(e.getMessage(), null);
            }
        }

        @Override
        protected void onPostExecute(Pair<String, ArrayList<StickerPack>> result) {
            EntryActivity activity = contextWeakReference.get();
            if (activity != null) {
                if (result.first != null) {
                    activity.showErrorMessage(result.first);
                } else {
                    activity.showStickerPack(result.second);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        esconderBotoesFisicos();
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
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}