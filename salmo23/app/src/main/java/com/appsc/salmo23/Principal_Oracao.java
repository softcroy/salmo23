package com.appsc.salmo23;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.novena.NovenaPrincipalFragment;
import com.appsc.salmo23.biblia.BibliaFragment;
import com.appsc.salmo23.figurinhas.EntryActivity;
import com.appsc.salmo23.activities.MyStickersFragment;
import com.appsc.salmo23.figurinhas.StickerPackListActivity;
import com.appsc.salmo23.identities.StickerPacksContainer;
import com.appsc.salmo23.login.MeusPedidosFragment;
import com.appsc.salmo23.login.PedidoAll;
import com.appsc.salmo23.login.PedidoAdapterAll;
import com.appsc.salmo23.login.Utils;
import com.appsc.salmo23.novos.TodasOracoes;
import com.appsc.salmo23.santos.GetDataAdapter_santos;
import com.appsc.salmo23.santos.RecyclerViewAdapter_santos;
import com.appsc.salmo23.status.Status;
import com.appsc.salmo23.utils.StickerPacksManager;
import com.appsc.salmo23.whatsapp_api.AddStickerPackActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.UserMessagingPlatform;
import com.orhanobut.hawk.Hawk;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Principal_Oracao extends AddStickerPackActivity implements PedidoAdapterAll.OnLikeClickListener{
    public static WeakReference<Principal_Oracao> weakActivity;

    public static Principal_Oracao getmInstanceActivity() {
        return weakActivity.get();
    }

    private FrameLayout adContainerView;
    private AdView adView;
    private static final String AD_BANNER_ID = "ca-app-pub-3882038212063780/3070020738";

    private TabLayout tabLayout;
    public ViewPager viewPager;
    private int alturaRvPedidos = 0;
    DrawerLayout drawer;

    public Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;
    List<GetDataAdapter_santos> GetDataAdapter1;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    RecyclerView.Adapter recyclerViewadapter;
    String JSON_IDENTIFICADOR = "identificador";
    String JSON_IMAGE = "image";
    String JSON_CONT = "cont";
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    String SANTOS;
    public TextView tv;
    private RecyclerView rvPedidos;
    private PedidoAdapterAll adapter;
    private String androidId;
    private ArrayList<PedidoAll> listaPedidos = new ArrayList<>();

    private static final String GET_ALL_PEDIDOS_URL = "https://softcroy.com/app_santos_v2/api3/login/get_all_pedidos.php";
    private static final String UPDATE_LIKE_URL = "https://softcroy.com/app_santos_v2/api3/login/update_like.php";
    private String contador = "https://softcroy.com/app_santos_v2/api3/config/contadordedownloadapps2.php?app=";

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 1;

    private InterstitialAd mInterstitialAd;
    private final String TAG = "AdMob25_Main";
    private final String AD_UNIT_ID = "ca-app-pub-3882038212063780/3481676164";

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupKeyboardListener();
        if (!Hawk.get("Assinatura").equals("Assinado")) {
            // Se não for assinado, carrega ou mantém o banner
            loadBanner();
            loadInterstitial();
        } else {
            // Se for assinado, remove o banner da tela imediatamente
            FrameLayout adContainerView = findViewById(R.id.ad_view_container);
            if (adContainerView != null) {
                adContainerView.removeAllViews(); // Remove o AdView (o banner em si)
                adContainerView.setVisibility(View.GONE); // Esconde o espaço que ele ocupava
            }
        }

        androidId = getIntent().getStringExtra("androidid");
        if (androidId == null || androidId.isEmpty()) {
            androidId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        }

        // Verificação do idioma do dispositivo
        if (isEnglish()) {
            SANTOS = "https://softcroy.com/app_santos_v2/api3/prayer/api_santos_en.php";
        } else {
            SANTOS = "https://softcroy.com/app_santos_v2/api3/prayer/api_santos.php";
        }

        contador();


        String packageName = getApplicationContext().getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Verifica se "versao" é menor que packageInfo.versionName
        if (packageInfo.versionName.compareTo(EntryActivity.versao) < 0) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(EntryActivity.titlo)
                    .setContentText(EntryActivity.mensagem)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            Uri uri = Uri.parse(EntryActivity.novo_link);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            Principal_Oracao.this.startActivity(intent);
                        }
                    })
                    .show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_NOTIFICATIONS);
            } else {

            }
        } else {

        } /*TIRAMISSU*/

        weakActivity = new WeakReference<>(Principal_Oracao.this);

        //////////-> MENU/////////////////
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setZ(10f);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                //inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //////////<- MENU/////////////////

        //////////<- SANTOS/////////////////

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        GetDataAdapter1 = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerViewlayoutManager = new GridLayoutManager(Principal_Oracao.this, 3);
        recyclerView.setLayoutManager(recyclerViewlayoutManager);


        JSON_DATA_WEB_CALL();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                JSON_DATA_WEB_CALL();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        //////////<- SANTOS/////////////////


        tv = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(getString(R.string.app_name));
        tv.setTextSize(23);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);

        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv.setMarqueeRepeatLimit(-1); // -1 = infinito
        tv.setSelected(true); // Necessário para o marquee funcionar

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(tv);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tv != null) {
            int position = tab.getPosition();
            if (position == 0) {
                tv.setText(getString(R.string.app_name));
            } else {
                CharSequence title = viewPager.getAdapter().getPageTitle(position);
                tv.setText(title);
            }

            // --- ADICIONE ESTA LINHA AQUI ---
            gerenciarRvPedidosPorPagina(position);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}
});

        // Dentro do seu onCreate, logo após o setupWithViewPager
        List<Integer> listaIcones = new ArrayList<>();

        listaIcones.add(R.drawable.menu_cruz);
        listaIcones.add(R.drawable.menu_maos);
        listaIcones.add(R.drawable.menu_biblia);
        listaIcones.add(R.drawable.rosario);
        listaIcones.add(R.drawable.menu_praying);
        listaIcones.add(R.drawable.menu_figurinhas);

// Lógica condicional
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            listaIcones.add(R.drawable.menu_criar);
        }

        listaIcones.add(R.drawable.menu_wallpaper);

// Se você precisar EXATAMENTE de um int[], converta assim:
        int[] ícones = new int[listaIcones.size()];
        for (int i = 0; i < listaIcones.size(); i++) {
            ícones[i] = listaIcones.get(i);
        }

        // Melhore o loop de ícones para garantir que ele respeite o total de abas criadas
for (int i = 0; i < tabLayout.getTabCount(); i++) {
    TabLayout.Tab tab = tabLayout.getTabAt(i);
    if (tab != null) {
        View customView = getLayoutInflater().inflate(R.layout.custom_tab, null);
        TextView tabText = customView.findViewById(R.id.text);
        ImageView tabIcon = customView.findViewById(R.id.icon);

        // Pega o título direto do adapter (sempre estará correto)
        tabText.setText(viewPager.getAdapter().getPageTitle(i));
        
        // Verifica se o índice do ícone existe para evitar Crash
        if (i < ícones.length) {
            tabIcon.setImageResource(ícones[i]);
        }

        tab.setCustomView(customView);
    }
}

        StickerPacksManager.stickerPacksContainer = new StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this));
        // Coloque isso no final do seu onCreate
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    Window window = getWindow();
    
    // Define a cor do fundo da barra como preto
    window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // IMPORTANTE: Remova a flag de "Light Status Bar" para que os ícones fiquem BRANCOS
        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        
        // Isso remove o modo de "ícones escuros", tornando-os brancos/claros nativamente
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; 
        decor.setSystemUiVisibility(flags);
    }
}

        rvPedidos = findViewById(R.id.rvPedidos);
        if (rvPedidos == null) {
            throw new RuntimeException(
                    "RecyclerView não encontrada em activity_main.xml; verifique o id!");
        }

        rvPedidos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PedidoAdapterAll(this, listaPedidos, this);
        rvPedidos.setAdapter(adapter);

        // Carrega todos os pedidos
        new GetAllPedidosTask().execute();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("ABRIR_FRAGMENT_NOVENAS", false)) {
            abrirFragmentoNovenaPrincipal();
        }
    }
    private void abrirFragmentoNovenaPrincipal() {
        try {
            if (viewPager != null) {
                // 1. Move o ViewPager para a aba 3 (onde fica a sua seção de novenas)
                viewPager.setCurrentItem(3, true);

                // 2. Executa a sua função nativa para ajustar as margens e sumir com o rvPedidos
                gerenciarRvPedidosPorPagina(3);

                Toast.makeText(this, "Retornando à sua novena em andamento!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ERRO_NOVENA", "ViewPager nulo ao tentar abrir a aba da novena.");
            }
        } catch (Exception e) {
            Log.e("ERRO_NOVENA", "Falha ao mudar para a aba de novenas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ATUALIZAR() {
        new GetAllPedidosTask().execute();
    }

    @Override
    public void onLikeClicked(int position) {
        // recupera o objeto Pedido que contém os campos 'pedido' e 'data'
        PedidoAll p = listaPedidos.get(position);
        // dispara a task com os quatro parâmetros
        new UpdateLikeTask(
                androidId,
                Principal_Oracao.this.getPackageName(),
                p.getPedido(),
                p.getData()
        ).execute();
    }

    private class UpdateLikeTask extends AsyncTask<Void, Void, String> {
        private final String androidId;
        private final String appPackage;
        private final String pedido;
        private final String data;

        UpdateLikeTask(String androidId,
                       String appPackage,
                       String pedido,
                       String data) {
            this.androidId = androidId;
            this.appPackage = appPackage;
            this.pedido = pedido;
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(UPDATE_LIKE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

                // monta o corpo da requisição com os 4 parâmetros
                String postData = "androidid=" + Utils.urlEncode(androidId)
                        + "&app=" + Utils.urlEncode(appPackage)
                        + "&pedido=" + Utils.urlEncode(pedido)
                        + "&data=" + Utils.urlEncode(data);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int code = conn.getResponseCode();
                InputStream is = (code == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    return sb.toString();
                }
            } catch (Exception e) {
                Log.e("UpdateLikeTask", "doInBackground erro", e);
                return null;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("UpdateLikeTask", result);
            if (result == null) {
                Toast.makeText(
                        Principal_Oracao.this,
                        "Erro ao dar like",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            result = result.trim();
            try {
                JSONObject json = new JSONObject(result);
                if (json.optInt("success", 0) == 1) {
                    listaPedidos.clear();
                    JSONArray arr = json.getJSONArray("pedidos");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        listaPedidos.add(new PedidoAll(
                                obj.getString("nome"),
                                obj.getString("pedido"),
                                obj.getString("data"),
                                obj.getInt("likes")
                        ));
                    }
                    ATUALIZAR();
                } else {
                    String msg = json.optString("message", "Falha ao dar like");
                    Toast.makeText(Principal_Oracao.this, msg, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("UpdateLikeTask", "JSON inválido", e);
                Toast.makeText(
                        Principal_Oracao.this,
                        "Resposta inesperada ao dar like:\n" + result,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }


    private class GetAllPedidosTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(GET_ALL_PEDIDOS_URL);
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
                // Prepara os parâmetros: androidid (vazio para todos) e app
                String meuPackage = getPackageName(); // mas aqui em inner class precisar capturar do contexto da Activity
                String postData = "androidid="
                        // manter vazio para listar todos os usuários; se quiser filtrar só do usuário atual, usar androidId
                        + ""
                        + "&app=" + Utils.urlEncode(meuPackage);
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode == HttpURLConnection.HTTP_OK
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                conn.disconnect();
                return sb.toString();
            } catch (Exception e) {
                Log.e("GetAllPedidosTask", "Erro ao obter pedidos", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // mesma lógica de parse JSON e popular lista
            if (result == null) {
                Toast.makeText(Principal_Oracao.this, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject json = new JSONObject(result);
                if (json.getInt("success") == 1) {
                    listaPedidos.clear();
                    JSONArray arr = json.getJSONArray("pedidos");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        listaPedidos.add(new PedidoAll(
                                obj.getString("nome"),
                                obj.getString("pedido"),
                                obj.getString("data"),
                                obj.getInt("likes")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(Principal_Oracao.this,
                            json.optString("message", "Erro desconhecido"),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("GetAllPedidosTask", "Erro ao parsear JSON", e);
                Toast.makeText(Principal_Oracao.this,
                        "Resposta inesperada do servidor",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /// ///////-> SANTOS/////////////////
    private boolean isEnglish() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }

    public void JSON_DATA_WEB_CALL() {
        GetDataAdapter1.clear();
        swipeRefreshLayout.setRefreshing(true);
        jsonArrayRequest = new JsonArrayRequest(SANTOS,

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        JSON_PARSE_DATA_AFTER_WEBCALL(response);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        requestQueue = Volley.newRequestQueue(Principal_Oracao.this);

        requestQueue.add(jsonArrayRequest);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {

            GetDataAdapter_santos GetDataAdapter2 = new GetDataAdapter_santos();

            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                GetDataAdapter2.setimage(json.getString(JSON_IMAGE));
                GetDataAdapter2.setidentificador(json.getString(JSON_IDENTIFICADOR));
                GetDataAdapter2.setcont(json.getString(JSON_CONT));
            } catch (JSONException e) {

                e.printStackTrace();
            }
            GetDataAdapter1.add(GetDataAdapter2);

        }


        recyclerViewadapter = new RecyclerViewAdapter_santos(GetDataAdapter1, Principal_Oracao.this);

        recyclerView.setAdapter(recyclerViewadapter);


    }

    /// ///////<- SANTOS/////////////////

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Oracoes(), "Oração");
        adapter.addFrag(new TodasOracoes(), getString(R.string.ora_es));
        adapter.addFrag(new BibliaFragment(), getString(R.string.biblia));
        adapter.addFrag(new NovenaPrincipalFragment(), getString(R.string.novena));
        adapter.addFrag(new MeusPedidosFragment(), getString(R.string.meus_pedidos));
        adapter.addFrag(new StickerPackListActivity(), getString(R.string.figurinhas));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            adapter.addFrag(new MyStickersFragment(), getString(R.string.criar));
        }
        adapter.addFrag(new Status(), "Status");
        viewPager.setAdapter(adapter); // Exemplo de onde termina sua configuração

        // ADICIONE AQUI NO FINAL:
        if (getIntent() != null && getIntent().getBooleanExtra("ABRIR_FRAGMENT_NOVENAS", false)) {
            abrirFragmentoNovenaPrincipal();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            // Criar e retornar um novo fragmento toda vez que ele for acessado
            Fragment fragment = mFragmentList.get(position);
            if (fragment != null) {
                // Reinicia o estado do fragmento se necessário, por exemplo, resetando variáveis, etc.
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        // Modificar para garantir que o fragmento seja recriado sempre
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;  // Isso força o recriamento de todos os fragments quando acessados
        }
    }


    @Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.more_menu, menu);

    MenuItem item = menu.findItem(R.id.assinatura);
    View actionView = item.getActionView();

    if (actionView != null) {
        com.airbnb.lottie.LottieAnimationView lottie = actionView.findViewById(R.id.lottieMenu);

        // Lógica de troca de ícone/animação
        if (Hawk.get("Assinatura", "").equals("Assinado")) {
            // Mostra o Lottie animado
            lottie.setVisibility(View.VISIBLE);
            lottie.setAnimation(R.raw.assinatura_on);
            lottie.playAnimation();
        } else {
            // Se não for assinado, você pode carregar um ícone estático ou outra animação
            lottie.setImageResource(R.drawable.assinatura_off); 
        }

        // Como usamos actionLayout, o clique deve ser tratado na View
        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Assinatura.class);
            startActivity(intent);
        });
    }

    return true;
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // --- NOVA OPÇÃO YOUTUBE ---
        if (item.getItemId() == R.id.menu_youtube) {

            try {
                // tenta abrir no app do YouTube
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("vnd.youtube://channel/UC-aLoRfbzCCDec1hWnnHO3A"));
                startActivity(intent);

            } catch (Exception e) {
                // fallback navegador
                Intent ii = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://youtube.com/channel/UC-aLoRfbzCCDec1hWnnHO3A"));
                startActivity(ii);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Principal_Oracao.this);
        dialogBuilder.setIcon(R.mipmap.ic_launcher);
        dialogBuilder.setTitle(getString(R.string.obrigadoporusaresteaplicativo) + "\uD83D\uDE18");
        dialogBuilder.setMessage("\uD83C\uDF43❤❤\uD83C\uDF43" + getString(R.string.Porfavoravalile5Estrelasedeixeseucomentario).toLowerCase() + "\uD83C\uDF43❤❤\uD83C\uDF43");

// Configurando os botões com texto formatado
        dialogBuilder.setPositiveButton(getString(R.string.estrelas5).toLowerCase(), null);
        dialogBuilder.setNegativeButton("sair", null);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

// Configurando os botões manualmente após o diálogo ser exibido
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setText(getString(R.string.estrelas5));
        positiveButton.setAllCaps(false); // Desativa o AllCaps (Caixa Alta)
        positiveButton.setTextColor(getResources().getColor(R.color.gren));
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPositiveButtonClicked(alertDialog);
            }
        });

        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setText(getString(R.string.sairr)); // Define "sair" em letras minúsculas diretamente
        negativeButton.setAllCaps(false); // Desativa o AllCaps (Caixa Alta)
        negativeButton.setTextColor(getResources().getColor(R.color.black));
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNegativeButtonClicked(alertDialog);
            }
        });

    }

    private void onPositiveButtonClicked(AlertDialog alertDialog) {
        alertDialog.dismiss();
        Intent ii = new Intent(Intent.ACTION_VIEW);
        ii.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + Principal_Oracao.this.getPackageName()));
        startActivity(ii);
    }

    private void onNegativeButtonClicked(AlertDialog alertDialog) {
        alertDialog.dismiss();
        Principal_Oracao.this.finish();
    }

    private void contador() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);

        // Se for a primeira execução, registre o download e atualize a flag isFirstRun
        if (isFirstRun) {
            RequestQueue mRequestQueue = Volley.newRequestQueue(Principal_Oracao.this.getApplicationContext());

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    contador + Principal_Oracao.this.getPackageName(), null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean success = response.getBoolean("success");

                        if (success) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isFirstRun", false);
                            editor.apply();
                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            int socketTimeout = 30000; // 30 segundos - altere conforme necessário
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjReq.setRetryPolicy(policy);
            mRequestQueue.add(jsonObjReq);
        }
    }
    private void loadBanner() {
        if (!UserMessagingPlatform.getConsentInformation(this).canRequestAds()) return;
        // Initialize MobileAds
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Initialization complete, handle any actions here if needed
            }
        });

        // Set test device ID (replace "ABCDEF012345" with your actual test device ID)
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Arrays.asList("ABCDEF012345"))
                        .build()
        );

        // Find the container for the ad view in your layout
        adContainerView = findViewById(R.id.ad_view_container);

        // Create a new AdView and set its Ad Unit ID
        adView = new AdView(this);
        adView.setAdUnitId(AD_BANNER_ID);

        // Remove any existing views from the container and add the ad view
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        // Get the appropriate AdSize for the ad view based on the container's width
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        // Set optional extras for the ad request (e.g., "collapsible" position)
        /*Bundle extras = new Bundle();
        extras.putString("collapsible", "bottom");

        // Build and load the ad request
        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();*/
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width
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
        if (Hawk.get("Assinatura").equals("Assinado")) {
            FrameLayout adContainerView = findViewById(R.id.ad_view_container);
            if (adContainerView != null) {
                adContainerView.removeAllViews(); // Remove o AdView (o banner em si)
                adContainerView.setVisibility(View.GONE); // Esconde o espaço que ele ocupava
            }
        }
        esconderBotoesFisicos();
        invalidateOptionsMenu();
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

    public void OPENN() {
        if (!Hawk.get("Assinatura").equals("Assinado")) {
            if (mInterstitialAd != null) {
                Log.d(TAG, "Exibindo anúncio que já estava em cache.");
                mInterstitialAd.show(this);
            } else {
                Log.w(TAG, "Anúncio ainda não estava pronto na memória. Solicitando novo download de segurança...");
                // Força o carregamento em background para que esteja pronto no próximo clique
                loadInterstitial();
            }
        }else{return;}
    }
    private void loadInterstitial() {
        if (!UserMessagingPlatform.getConsentInformation(this).canRequestAds()) {
            Log.w(TAG, "Consentimento UMP pendente. Carregamento abortado.");
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "Anúncio Intersticial Carregado e Pronto em cache.");
                        
                        // Chamar os callbacks configurados acima para gerenciar o próximo anúncio
                        setAdCallbacks(); 
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Falha ao carregar Intersticial: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }
    private void setAdCallbacks() {
        if (mInterstitialAd == null) return;

        mInterstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Executado quando o usuário clica no "X" para fechar o anúncio
                Log.d(TAG, "O anúncio foi fechado pelo usuário.");
                mInterstitialAd = null; // Limpa o anúncio antigo
                loadInterstitial();     // Pré-carrega o próximo anúncio IMEDIATAMENTE para a próxima vez
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                // Executado se o anúncio falhar na hora de abrir
                Log.e(TAG, "Falha ao exibir o anúncio: " + adError.getMessage());
                mInterstitialAd = null;
                loadInterstitial();     // Tenta baixar outro em caso de falha
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Executado quando o anúncio cobre a tela com sucesso
                Log.d(TAG, "Anúncio exibido na tela.");
            }
        });
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
    // --- COLE ESTES MÉTODOS ABAIXO ---
    private void setupKeyboardListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                android.graphics.Rect r = new android.graphics.Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Se o teclado ocupar mais de 15% da tela, consideramos "Aberto"
                if (keypadHeight > screenHeight * 0.15) {
                    onKeyboardVisible(true);
                } else {
                    onKeyboardVisible(false);
                }
            }
        });
    }
    private void onKeyboardVisible(boolean isVisible) {
        if (rvPedidos == null || viewPager == null) return;

        // Bloqueia qualquer ação do teclado no rvPedidos se estiver na aba da Novena (Posição 3)
        if (viewPager.getCurrentItem() == 3) {
            if (isVisible) {
                showSystemUI();
            } else {
                esconderBotoesFisicos();
            }
            return;
        }

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();

        if (isVisible) {
            // --- GAVETA FECHANDO (SUBINDO) ---
            showSystemUI();

            if (rvPedidos.getVisibility() == View.VISIBLE) {
                rvPedidos.animate()
                        .translationY(-alturaRvPedidos)
                        .alpha(0.8f)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> {
                            rvPedidos.setVisibility(View.GONE);
                            params.topMargin = 0;
                            viewPager.setLayoutParams(params);
                            viewPager.requestLayout();
                        })
                        .start();
            }

        } else {
            // --- GAVETA ABRINDO (DESCENDO) ---
            esconderBotoesFisicos();

            if (rvPedidos.getVisibility() == View.GONE) {
                params.topMargin = alturaRvPedidos;
                viewPager.setLayoutParams(params);
                viewPager.requestLayout();

                rvPedidos.setVisibility(View.VISIBLE);
                rvPedidos.setTranslationY(-alturaRvPedidos);
                rvPedidos.setAlpha(1f);

                rvPedidos.animate()
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }
        }
    }
    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.navigationBars());
            }
        } else {
            // Para versões antigas do Android
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }
    private void gerenciarRvPedidosPorPagina(int posicaoAtual) {
        if (rvPedidos == null || viewPager == null) return;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();

        // Alterado para a posição 3 conforme a sua estrutura
        if (posicaoAtual == 3) {
            // Esconde imediatamente o rvPedidos quando entra na Novena
            rvPedidos.setVisibility(View.GONE);
            params.topMargin = 0;
            viewPager.setLayoutParams(params);
            viewPager.requestLayout();
        } else {
            // Se sair da Novena, restaura o estado original do rvPedidos
            if (rvPedidos.getVisibility() == View.GONE) {
                params.topMargin = alturaRvPedidos;
                viewPager.setLayoutParams(params);
                viewPager.requestLayout();
                rvPedidos.setVisibility(View.VISIBLE);
                rvPedidos.setTranslationY(0f);
                rvPedidos.setAlpha(1f);
            }
        }
    }
}