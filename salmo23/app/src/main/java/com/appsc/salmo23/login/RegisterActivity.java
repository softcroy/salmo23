package com.appsc.salmo23.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.appsc.salmo23.R;
import com.appsc.salmo23.figurinhas.EntryActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNome;
    private Button btnRegister;

    private static final String REGISTER_URL = "https://softcroy.com/app_santos_v2/api3/login/register_user.php";

    // Mesmo nome de SharedPreferences usado em SplashActivity
    private static final String PREFS_NAME = "MeuAppPrefs";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_register);


        etNome = findViewById(R.id.etNome);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            if (TextUtils.isEmpty(nome)) {
                etNome.setError("Nome obrigatório");
                return;
            }

            String androidId = getAndroidId(this);

            // Monta um JSONArray contendo apenas o package name do próprio app
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(getPackageName());
            String appsJsonString = jsonArray.toString();

            // Executa AsyncTask para chamar register_user.php
            new RegisterUserTask(androidId, nome, appsJsonString).execute();
        });
    }

    private String getAndroidId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, String> {
        private final String androidId, nome, appsJson;

        RegisterUserTask(String androidId, String nome, String appsJson) {
            this.androidId = androidId;
            this.nome = nome;
            this.appsJson = appsJson;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(REGISTER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "androidid=" + androidId +
                        "&nome=" + Utils.urlEncode(nome) +
                        "&apps=" + Utils.urlEncode(appsJson);

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                conn.disconnect();
                return sb.toString();

            } catch (Exception e) {
                Log.e("RegisterUserTask", "Erro no POST", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(RegisterActivity.this, "Erro na conexão", Toast.LENGTH_SHORT).show();
                return;
            }
            // Supondo que o PHP retorne algo como {"success":1, "message":"..."}
            if (result.contains("\"success\":1")) {
                // Salva em SharedPreferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isRegistered", true);
                editor.putString("androidid", androidId);
                editor.putString("nome", nome);
                editor.apply();

                Toast.makeText(RegisterActivity.this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();

                // Vai para PedidoActivity
                Intent intent = new Intent(RegisterActivity.this, EntryActivity.class);
                intent.putExtra("androidid", androidId);
                intent.putExtra("NOMEUSER", nome);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
