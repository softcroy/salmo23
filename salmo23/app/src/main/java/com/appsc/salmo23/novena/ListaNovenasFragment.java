package com.appsc.salmo23.novena;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaNovenasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListaNovenasAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText pesquisa;

    private final List<Novena> listaOriginalEmAndamento = new ArrayList<>();
    private final List<Novena> listaOriginalDisponiveis = new ArrayList<>();

    private final String URL_GET =
            "https://softcroy.com/app_santos_v2/novena/get_novenas.php?androidid=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_lista_novenas,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerListaNovenasGeral);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        pesquisa = view.findViewById(R.id.pesquisa);

        // Atualizar deslizando
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (getContext() != null) {

                String androidId = Settings.Secure.getString(
                        requireContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );

                carregarDados(androidId);
            }
        });

        // Pesquisa em tempo real
        pesquisa.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {

                filtrarLista(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (getContext() != null) {

            String androidId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            carregarDados(androidId);
        }

        return view;
    }

    private void filtrarLista(String texto) {

        if (adapter == null) return;

        texto = texto.toLowerCase().trim();

        List<Novena> filtradaAndamento = new ArrayList<>();
        List<Novena> filtradaDisponiveis = new ArrayList<>();

        for (Novena n : listaOriginalEmAndamento) {

            if (n.getNome() != null &&
                    n.getNome().toLowerCase().contains(texto)) {

                filtradaAndamento.add(n);
            }
        }

        for (Novena n : listaOriginalDisponiveis) {

            if (n.getNome() != null &&
                    n.getNome().toLowerCase().contains(texto)) {

                filtradaDisponiveis.add(n);
            }
        }

        if (getContext() != null) {

            adapter = new ListaNovenasAdapter(
                    getContext(),
                    filtradaAndamento,
                    filtradaDisponiveis
            );

            recyclerView.setAdapter(adapter);
        }
    }

    private void carregarDados(String androidId) {

        swipeRefreshLayout.setRefreshing(true);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_GET + androidId,
                null,

                response -> {

                    try {

                        List<Novena> catalogoCompleto = new ArrayList<>();

                        // 1. Carregar catálogo do servidor
                        JSONArray listaArray =
                                response.getJSONArray("lista_novenas");

                        for (int i = 0; i < listaArray.length(); i++) {

                            JSONObject obj = listaArray.getJSONObject(i);

                            Novena n = new Novena();

                            n.setId(obj.getString("id"));
                            n.setNome(obj.getString("nome"));
                            n.setImagem(obj.getString("imagem"));

                            // Guarda posição original
                            n.setOrdemOriginal(i);

                            n.setPadroeiro(
                                    obj.optString("padroeiro", "")
                            );

                            n.setHistoria(
                                    obj.optString("historia", "")
                            );

                            n.setDataTradicional(
                                    obj.optString("data_tradicional", "---")
                            );

                            n.setDiaFesta(
                                    obj.optString("dia_festa", "---")
                            );

                            n.setRezasJson(
                                    obj.optString("rezas_dias", "")
                            );

                            catalogoCompleto.add(n);
                        }

                        // 2. Mapear progresso do usuário
                        Map<String, JSONObject> progressoMap =
                                new HashMap<>();

                        JSONArray progressoArray =
                                response.getJSONArray("progresso_usuario");

                        for (int j = 0;
                             j < progressoArray.length();
                             j++) {

                            JSONObject pObj =
                                    progressoArray.getJSONObject(j);

                            String idNovena =
                                    pObj.optString("id da novena");

                            if (!idNovena.isEmpty()) {

                                progressoMap.put(idNovena, pObj);
                            }
                        }

                        List<Novena> emAndamento =
                                new ArrayList<>();

                        List<Novena> disponiveis =
                                new ArrayList<>();

                        // 3. Separar listas
                        for (Novena n : catalogoCompleto) {

                            if (progressoMap.containsKey(n.getId())) {

                                JSONObject dadosProgresso =
                                        progressoMap.get(n.getId());

                                int concluidos =
                                        dadosProgresso.optInt(
                                                "dias concluídos",
                                                0
                                        );

                                if (concluidos == 0) {

                                    concluidos =
                                            dadosProgresso.optInt(
                                                    "dias concluu00eddos",
                                                    0
                                            );
                                }

                                if (concluidos > 0 &&
                                        concluidos < 9) {

                                    // Em andamento

                                    n.setAuxCausa(
                                            dadosProgresso.optString(
                                                    "causa",
                                                    "Minha Intenção"
                                            )
                                    );

                                    String dataIni =
                                            dadosProgresso.optString(
                                                    "data de início",
                                                    ""
                                            );

                                    if (dataIni.isEmpty()) {

                                        dataIni =
                                                dadosProgresso.optString(
                                                        "data de inu00edcio",
                                                        ""
                                                );
                                    }

                                    n.setAuxDataInicio(dataIni);

                                    n.setAuxDiasConcluidos(
                                            concluidos
                                    );

                                    emAndamento.add(n);

                                } else {

                                    // Concluída

                                    n.setAuxDataInicio("");
                                    n.setAuxDiasConcluidos(0);
                                    n.setAuxCausa("");

                                    disponiveis.add(n);
                                }

                            } else {

                                // Nunca iniciada

                                n.setAuxDataInicio("");
                                n.setAuxDiasConcluidos(0);

                                disponiveis.add(n);
                            }
                        }

                        // Salvar listas originais
                        listaOriginalEmAndamento.clear();
                        listaOriginalDisponiveis.clear();

                        listaOriginalEmAndamento.addAll(
                                emAndamento
                        );

                        listaOriginalDisponiveis.addAll(
                                disponiveis
                        );

                        // Configurar adapter
                        if (getContext() != null) {

                            adapter = new ListaNovenasAdapter(
                                    getContext(),
                                    emAndamento,
                                    disponiveis
                            );

                            recyclerView.setAdapter(adapter);
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    swipeRefreshLayout.setRefreshing(false);

                },

                error -> {

                    swipeRefreshLayout.setRefreshing(false);

                    if (getContext() != null) {

                        Toast.makeText(
                                getContext(),
                                "Erro de conexão com o servidor",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

        if (getContext() != null) {

            Volley.newRequestQueue(getContext()).add(request);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (getContext() != null) {

            String androidId = Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            carregarDados(androidId);
        }
    }
}