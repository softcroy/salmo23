package com.appsc.salmo23.novos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodasOracoes extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;
    List<Oracoes_GetDataAdapter> GetDataAdapter1;
    RecyclerView recyclerView;
    TodasOracoes_RecyclerViewAdapter recyclerViewadapter;

    EditText pesquisaEditText;

    String ORACOES;
    String ORACOES_BURCAR;

    String JSON_id = "id";
    String JSON_identificador = "identificador";
    String JSON_nome = "nome";
    String JSON_icone = "icone";
    String JSON_image = "image";
    String JSON_oracao = "oracao";
    String JSON_sond = "sond";
    String JSON_visualizacao = "visualizacao";
    String JSON_data = "data";

    RequestQueue requestQueue;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.oracoestodas, container, false);
        setHasOptionsMenu(true);

        if (isEnglish()) {
            ORACOES = "https://softcroy.com/app_santos_v2/api3/prayer/api_todas_oracoes_en.php";
            ORACOES_BURCAR = "https://softcroy.com/app_santos_v2/api3/prayer/api_buscar_oracoes_en.php?nome=";
        } else {
            ORACOES = "https://softcroy.com/app_santos_v2/api3/prayer/api_todas_oracoes_2.php";
            ORACOES_BURCAR = "https://softcroy.com/app_santos_v2/api3/prayer/api_buscar_oracoes.php?nome=";
        }

        swipeRefreshLayout = myFragmentView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        GetDataAdapter1 = new ArrayList<>();

        recyclerView = myFragmentView.findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Inicializa o adapter com a lista vazia
        recyclerViewadapter = new TodasOracoes_RecyclerViewAdapter(GetDataAdapter1, getActivity());
        recyclerView.setAdapter(recyclerViewadapter);

        requestQueue = Volley.newRequestQueue(getActivity());

        JSON_DATA_WEB_CALL();

        swipeRefreshLayout.setOnRefreshListener(this::JSON_DATA_WEB_CALL);

        pesquisaEditText = myFragmentView.findViewById(R.id.pesquisa);

        pesquisaEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnable != null) handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                runnable = () -> {
                    if (query.isEmpty()) {
                        JSON_DATA_WEB_CALL();
                    } else if (query.length() >= 2) {
                        GET_JSON_BURCAR(query);
                    }
                };
                handler.postDelayed(runnable, 500);
            }
        });

        return myFragmentView;
    }

    private boolean isEnglish() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }

    public void GET_JSON_BURCAR(String nomePesquisado) {
        swipeRefreshLayout.setRefreshing(true);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(ORACOES_BURCAR + nomePesquisado,
                response -> {
                    processarJSON(response);
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    // Em caso de erro na busca, limpa a lista usando o método do adapter
                    recyclerViewadapter.updateList(new ArrayList<>());
                });
        requestQueue.add(jsonArrayRequest);
    }

    public void JSON_DATA_WEB_CALL() {
        swipeRefreshLayout.setRefreshing(true);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(ORACOES,
                response -> {
                    processarJSON(response);
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> swipeRefreshLayout.setRefreshing(false));
        requestQueue.add(jsonArrayRequest);
    }

    private void processarJSON(JSONArray array) {
        // Criamos uma lista temporária para preencher com os dados do JSON
        List<Oracoes_GetDataAdapter> listaTemporaria = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            Oracoes_GetDataAdapter item = new Oracoes_GetDataAdapter();
            try {
                JSONObject json = array.getJSONObject(i);
                item.setid(json.optString(JSON_id));
                item.setidentificador(json.optString(JSON_identificador));
                item.setnome(json.optString(JSON_nome));
                item.seticone(json.optString(JSON_icone));
                item.setimage(json.optString(JSON_image));
                item.setoracao(json.optString(JSON_oracao));
                item.setsond(json.optString(JSON_sond));
                item.setvisualizacao(json.optString(JSON_visualizacao));
                item.setdata(json.optString(JSON_data));
                listaTemporaria.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // MUDANÇA PRINCIPAL:
        // Em vez de apenas dar notify, enviamos a nova lista para o método updateList
        // do adapter, que cuidará de colocar os favoritos no topo.
        if (recyclerViewadapter != null) {
            recyclerViewadapter.updateList(listaTemporaria);
        }
    }
}