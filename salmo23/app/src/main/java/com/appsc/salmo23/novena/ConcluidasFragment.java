package com.appsc.salmo23.novena;

import android.animation.Animator;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConcluidasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListaNovenasConcluidasAdapter adapter;
    private RelativeLayout layoutSemNovena;
    private LottieAnimationView lottieEmpty;
    private TextView txtAvisoVazio;

    private final String URL_CONCLUIDAS = "https://softcroy.com/app_santos_v2/novena/get_concluidas.php?androidid=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_concluidas, container, false);

        recyclerView = view.findViewById(R.id.recyclerConcluidas);
        layoutSemNovena = view.findViewById(R.id.layoutSemNovena);
        lottieEmpty = view.findViewById(R.id.lottieEmpty);
        txtAvisoVazio = view.findViewById(R.id.txtAvisoVazio);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        lottieEmpty.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                txtAvisoVazio.setVisibility(View.VISIBLE);
                txtAvisoVazio.setAlpha(0f);
                txtAvisoVazio.animate().alpha(1f).setDuration(500).start();
            }
        });

        carregarDados();
        return view;
    }

    private void carregarDados() {
        if (getContext() == null) return;

        String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String urlCompleta = URL_CONCLUIDAS + androidId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlCompleta, null,
                response -> {
                    try {
                        List<Novena> concluidas = new ArrayList<>();
                        JSONArray listaArray = response.optJSONArray("lista_concluidas");

                        if (listaArray != null) {
                            for (int i = 0; i < listaArray.length(); i++) {
                                JSONObject obj = listaArray.getJSONObject(i);

                                Novena n = new Novena();
                                n.setId(obj.getString("id"));
                                n.setNome(obj.getString("nome"));
                                n.setImagem(obj.getString("imagem"));
                                n.setPadroeiro(obj.getString("padroeiro"));

                                n.setAuxCausa(obj.optString("causa", "Graça Alcançada"));
                                n.setAuxDataInicio(obj.optString("data_inicio", "---"));
                                n.setAuxDataTermino(obj.optString("data_conclusao", "---"));

                                // NOVO: Captura o histórico detalhado de cada um dos 9 dias
                                // Isso permite que a tela de progresso mostre as datas ao ser aberta
                                JSONArray histBruto = obj.optJSONArray("historico_dias");
                                if (histBruto != null) {
                                    n.getDatasConclusao().clear(); // Garante mapa limpo
                                    for (int j = 0; j < histBruto.length(); j++) {
                                        JSONObject diaObj = histBruto.getJSONObject(j);
                                        n.addDataConclusao(diaObj.optInt("dia"), diaObj.optString("data"));
                                    }
                                }

                                n.setAuxDiasConcluidos(9);
                                concluidas.add(n);
                            }
                        }

                        atualizarUI(concluidas);

                    } catch (Exception e) {
                        e.printStackTrace();
                        atualizarUI(new ArrayList<>());
                    }
                }, error -> {
            if (isAdded()) {
                atualizarUI(new ArrayList<>());
            }
        });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void atualizarUI(List<Novena> lista) {
        if (lista.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutSemNovena.setVisibility(View.VISIBLE);
            txtAvisoVazio.setVisibility(View.INVISIBLE);
            lottieEmpty.playAnimation();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutSemNovena.setVisibility(View.GONE);

            // Passamos a lista de concluídas como o primeiro parâmetro (emAndamento)
            // O Adapter já tem lógica para tratar o título se diasConcluidos >= 9
            adapter = new ListaNovenasConcluidasAdapter(getContext(), lista, new ArrayList<>());
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarDados();
    }
}