package com.appsc.salmo23.biblia;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CapituloFragment extends Fragment {

    private RecyclerView rvCapitulos;
    private GridAdapter adapter;
    private List<Integer> chapterList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capitulo, container, false);

        rvCapitulos = view.findViewById(R.id.rvCapitulos);
        rvCapitulos.setLayoutManager(new GridLayoutManager(getContext(), 5));

        return view;
    }

    public void carregarCapitulos(String livro) {
        chapterList.clear();
        if (adapter != null) {
            adapter.setSelectedPosition(-1);
            adapter.notifyDataSetChanged();
        }

        String url = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/api.php?action=get_chapters&livro=" + livro;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            chapterList.add(response.getInt(i));
                        }

                        if (chapterList.size() > 0) {
                            setupAdapter();

                            // Comunicação com o Fragment Pai (BibliaFragment)
                            Fragment parent = getParentFragment();
                            if (parent instanceof BibliaFragment) {
                                ((BibliaFragment) parent).onChapterSelected(chapterList.get(0), false);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("API_ERROR", "Erro ao processar capítulos");
                    }
                },
                error -> {
                    if (isAdded())
                        Toast.makeText(getContext(), "Erro ao carregar capítulos", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext().getApplicationContext());
        queue.add(request);
    }

    private void setupAdapter() {
        if (getContext() == null) return;

        String livroAtual = "";
        Fragment parent = getParentFragment();
        if (parent instanceof BibliaFragment) {
            livroAtual = ((BibliaFragment) parent).getSelectedBook();
        }

        final String key = "last_cap_" + livroAtual;
        int ultimoCapitulo = Hawk.get(key, 1);

        adapter = new GridAdapter(chapterList, chapter -> {
            Hawk.put(key, chapter);
            if (parent instanceof BibliaFragment) {
                ((BibliaFragment) parent).onChapterSelected(chapter, true);
            }
        });

        int posicaoParaMarcar = 0;
        for (int i = 0; i < chapterList.size(); i++) {
            if (chapterList.get(i) == ultimoCapitulo) {
                posicaoParaMarcar = i;
                break;
            }
        }

        adapter.setSelectedPosition(posicaoParaMarcar);
        rvCapitulos.setAdapter(adapter);
        rvCapitulos.scrollToPosition(posicaoParaMarcar);
    }

    @Override
    public void onResume() {
        super.onResume();
        Fragment parent = getParentFragment();
        if (parent instanceof BibliaFragment) {
            BibliaFragment fragmentPai = (BibliaFragment) parent;
            String livro = fragmentPai.getSelectedBook();

            if (chapterList.isEmpty() && livro != null && !livro.isEmpty()) {
                carregarCapitulos(livro);
            }
        }
    }
}