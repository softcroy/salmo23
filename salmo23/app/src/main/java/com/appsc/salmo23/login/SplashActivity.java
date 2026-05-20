package com.appsc.salmo23.login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appsc.salmo23.MyApplication;
import com.appsc.salmo23.R;
import com.appsc.salmo23.figurinhas.EntryActivity;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivityUMP";
    private static final String GET_PEDIDOS_URL = "https://softcroy.com/app_santos_v2/api3/login/verificar.php";

    private ConsentInformation consentInformation;
    private String androidId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pega o Android ID
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Inicia o fluxo do Google UMP antes de qualquer outra ação de rede ou anúncios
        iniciarFluxoConsentimentoUMP();
    }

    private void iniciarFluxoConsentimentoUMP() {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);

        consentInformation.requestConsentInfoUpdate(
                this, // Aqui o 'this' funciona perfeitamente pois estamos em uma Activity
                params,
                () -> {
                    // Carrega e exibe o formulário se for necessário (Regiões com LGPD/GDPR)
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            formError -> {
                                if (formError != null) {
                                    Log.w(TAG, formError.getErrorCode() + ": " + formError.getMessage());
                                }

                                // Consentimento resolvido! Inicializa o SDK e continua o fluxo do app
                                processarPosConsentimento();
                            }
                    );
                },
                requestConsentError -> {
                    Log.w(TAG, requestConsentError.getErrorCode() + ": " + requestConsentError.getMessage());
                    // Fallback em caso de erro de conexão com os servidores do Google, não trava o usuário
                    processarPosConsentimento();
                }
        );
    }

    private void processarPosConsentimento() {
        // Avisa a classe Application para ativar o AdMob se a liberação de anúncios foi concedida
        MyApplication myApp = (MyApplication) getApplication();
        if (consentInformation.canRequestAds()) {
            myApp.initializeMobileAdsSdk();
        }

        // Agora sim executa a sua lógica nativa de checagem do servidor
        new CheckUserTask(androidId).execute();
    }

    private class CheckUserTask extends AsyncTask<Void, Void, String> {
        private final String androidId;

        CheckUserTask(String androidId) {
            this.androidId = androidId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(GET_PEDIDOS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                String postData = "androidid=" + Utils.urlEncode(androidId);
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader br = (responseCode == HttpURLConnection.HTTP_OK)
                        ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                        : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                conn.disconnect();
                return sb.toString();
            } catch (Exception e) {
                Log.e("CheckUserTask", "Erro na verificação do usuário", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                // Se houver falha de rede na API, tenta novamente para evitar tela preta travada
                new CheckUserTask(androidId).execute();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                int    success = json.optInt("success", 0);
                String nome    = json.optString("nome");
                if (success == 1) {
                    // Usuário existe
                    Intent intent = new Intent(SplashActivity.this, EntryActivity.class);
                    intent.putExtra("androidid", androidId);
                    intent.putExtra("NOMEUSER", nome);
                    startActivity(intent);
                    finish();
                } else {
                    // success == 0 → usuário não cadastrado
                    startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
                    finish();
                }
            } catch (Exception e) {
                Log.e("CheckUserTask", "Erro ao parsear JSON", e);
                // Se o JSON vier quebrado, leva ao cadastro para segurança
                startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
                finish();
            }
        }
    }
}