package com.appsc.salmo23.login;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MeusPedidosFragment extends Fragment implements PedidoAdapter.OnLikeClickListener {
    private RecyclerView rvPedidos;
    private EditText etPedido;
    private ImageView btnEnviarPedido;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String androidId;
    private PedidoAdapter adapter;
    RelativeLayout tela_no_content;
    private List<Pedido> listaPedidos = new ArrayList<>();

    private static final String GET_PEDIDOS_URL  = "https://softcroy.com/app_santos_v2/api3/login/get_pedidos.php";
    private static final String ADD_PEDIDO_URL   = "https://softcroy.com/app_santos_v2/api3/login/add_pedido.php";
    private static final String UPDATE_LIKE_URL  = "https://softcroy.com/app_santos_v2/api3/login/update_like.php";

    public MeusPedidosFragment() { /* required empty constructor */ }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            androidId = Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_pedido, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPedidos = view.findViewById(R.id.rvPedidos);
        etPedido = view.findViewById(R.id.etPedido);
        btnEnviarPedido = view.findViewById(R.id.btnEnviarPedido);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        tela_no_content = view.findViewById(R.id.tela_no_content);

        rvPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PedidoAdapter(requireContext(), listaPedidos, this);

        // ✅ Atualiza a lista quando um pedido for excluído
        adapter.setOnPedidoExcluidoListener(() -> {
            new GetPedidosTask(androidId, requireContext().getPackageName()).execute();
        });

        rvPedidos.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimary);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            new GetPedidosTask(androidId, requireContext().getPackageName()).execute();
        });

        btnEnviarPedido.setOnClickListener(v -> {
            String texto = etPedido.getText().toString().trim();
            if (TextUtils.isEmpty(texto)) {
                etPedido.setError("Digite um pedido");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "d 'de' MMMM 'de' yyyy 'às' HH:mm",
                    new Locale("pt", "BR")
            );
            sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
            String dataFormatada = sdf.format(new Date());

            new SendPedidoTask(androidId,
                    requireContext().getPackageName(),
                    texto,
                    dataFormatada).execute();
        });

        // primeira carga
        swipeRefreshLayout.setRefreshing(true);
        new GetPedidosTask(androidId, requireContext().getPackageName()).execute();
    }

    @Override
    public void onLikeClicked(int position) {
        Pedido p = listaPedidos.get(position);
        swipeRefreshLayout.setRefreshing(true);
        new UpdateLikeTask(
                androidId,
                requireContext().getPackageName(),
                p.getPedido(),
                p.getData()
        ).execute();
    }

    private class GetPedidosTask extends AsyncTask<Void, Void, String> {
        private final String androidId, appPackage;
        GetPedidosTask(String androidId, String appPackage) {
            this.androidId = androidId;
            this.appPackage = appPackage;
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

                String postData = "androidid=" + Utils.urlEncode(androidId)
                        + "&app="       + Utils.urlEncode(appPackage);

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
                Log.e("GetPedidosTask", "Erro ao obter pedidos", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isAdded()) return;
            swipeRefreshLayout.setRefreshing(false);

            if (result == null) {
                Toast.makeText(requireContext(),
                        "Erro ao carregar pedidos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                if (json.getInt("success") == 1) {
                    listaPedidos.clear();
                    JSONArray arr = json.getJSONArray("pedidos");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        listaPedidos.add(new Pedido(
                                obj.getString("nome"),
                                obj.getString("pedido"),
                                obj.getString("data"),
                                obj.getInt("likes")
                        ));
                    }

                    sortPedidosPorDataDesc();
                    adapter.notifyDataSetChanged();
                    Principal_Oracao.getmInstanceActivity().ATUALIZAR();

                    if (listaPedidos.isEmpty()) {
                        tela_no_content.setVisibility(View.VISIBLE);
                    } else {
                        tela_no_content.setVisibility(View.GONE);
                    }

                } else {
                    tela_no_content.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(),
                            "Falha ao carregar: " + json.getString("message"),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("GetPedidosTask", "Erro ao parsear JSON", e);
                tela_no_content.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(),
                        "Resposta inesperada do servidor",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendPedidoTask extends AsyncTask<Void, Void, String> {
        private final String androidId, app, pedido, data;
        SendPedidoTask(String androidId, String app, String pedido, String data) {
            this.androidId = androidId;
            this.app = app;
            this.pedido = pedido;
            this.data = data;
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ADD_PEDIDO_URL);
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

                String postData = "androidid=" + Utils.urlEncode(androidId)
                        + "&app="       + Utils.urlEncode(app)
                        + "&pedido="    + Utils.urlEncode(pedido)
                        + "&data="      + Utils.urlEncode(data);

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
                Log.e("SendPedidoTask", "Erro no POST", e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if (!isAdded()) return;
            swipeRefreshLayout.setRefreshing(false);

            if (result == null) {
                Toast.makeText(requireContext(),
                        "Erro na conexão",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                if (json.getInt("success") == 1) {
                    etPedido.setText("");
                    tela_no_content.setVisibility(View.GONE);
                    new GetPedidosTask(androidId, requireContext().getPackageName()).execute();
                    Principal_Oracao.getmInstanceActivity().ATUALIZAR();
                } else {
                    Toast.makeText(requireContext(),
                            "Falha ao enviar: " + json.getString("message"),
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e("SendPedidoTask", "Erro ao parsear JSON", e);
                Toast.makeText(requireContext(),
                        "Resposta inesperada do servidor",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateLikeTask extends AsyncTask<Void, Void, String> {
        private final String androidId, appPackage, pedido, data;
        UpdateLikeTask(String androidId, String appPackage, String pedido, String data) {
            this.androidId   = androidId;
            this.appPackage  = appPackage;
            this.pedido      = pedido;
            this.data        = data;
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(UPDATE_LIKE_URL);
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

                String postData = "androidid=" + Utils.urlEncode(androidId)
                        + "&app="    + Utils.urlEncode(appPackage)
                        + "&pedido=" + Utils.urlEncode(pedido)
                        + "&data="   + Utils.urlEncode(data);

                try (OutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
                    os.write(postData.getBytes("UTF-8"));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK
                                ? conn.getInputStream()
                                : conn.getErrorStream(),
                        "UTF-8"
                ));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                conn.disconnect();
                return sb.toString();
            } catch (Exception e) {
                Log.e("UpdateLikeTask", "Erro ao atualizar like", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("WWWWW", result);
            if (!isAdded()) return;
            swipeRefreshLayout.setRefreshing(false);

            if (result == null) {
                Toast.makeText(requireContext(),
                        "Erro ao dar like",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                if (json.getInt("success") == 1) {
                    tela_no_content.setVisibility(View.GONE);
                    new GetPedidosTask(androidId, requireContext().getPackageName()).execute();
                    Principal_Oracao.getmInstanceActivity().ATUALIZAR();
                } else {
                    Toast.makeText(requireContext(),
                            "Falha ao atualizar like: " + json.getString("message"),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("UpdateLikeTask", "Erro ao parsear JSON", e);
                Toast.makeText(requireContext(),
                        "Resposta inesperada ao dar like",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sortPedidosPorDataDesc() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "d 'de' MMMM 'de' yyyy 'às' HH:mm",
                new Locale("pt", "BR")
        );
        sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        Collections.sort(listaPedidos, new Comparator<Pedido>() {
            @Override
            public int compare(Pedido p1, Pedido p2) {
                try {
                    Date d1 = sdf.parse(p1.getData());
                    Date d2 = sdf.parse(p2.getData());
                    return d2.compareTo(d1);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }
}