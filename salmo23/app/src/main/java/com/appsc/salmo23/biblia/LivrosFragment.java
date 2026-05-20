package com.appsc.salmo23.biblia;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;
import com.appsc.salmo23.biblia.favorito.FavModel;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LivrosFragment extends Fragment {

    private RecyclerView rvBooks, rvSearchVerses;
    private EditText etSearch;
    private View layoutSearchVerses;
    private TextView tvTitleBooks;

    private BookAdapter adapter;
    private List<String> bookList = new ArrayList<>();
    private List<FavModel> searchResultsVerses = new ArrayList<>();

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private SearchVersesAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livros, container, false);

        if (!Hawk.isBuilt()) {
            Hawk.init(requireContext()).setEncryption(new NoEncryption()).build();
        }

        rvBooks = view.findViewById(R.id.rvBooks);
        rvSearchVerses = view.findViewById(R.id.rvSearchVerses);
        etSearch = view.findViewById(R.id.etSearchBook);
        layoutSearchVerses = view.findViewById(R.id.layoutSearchVerses);
        tvTitleBooks = view.findViewById(R.id.tvTitleBooks);

        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBooks.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        rvSearchVerses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchVerses.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        loadBooksFromAPI();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    tvTitleBooks.setVisibility(View.GONE);
                    rvBooks.setVisibility(View.VISIBLE);
                    layoutSearchVerses.setVisibility(View.GONE);
                    if (adapter != null) adapter.getFilter().filter("");
                    searchResultsVerses.clear();
                    if (searchAdapter != null) searchAdapter.notifyDataSetChanged();
                } else {
                    if (adapter != null) {
                        adapter.getFilter().filter(query, countResult -> {
                            tvTitleBooks.setVisibility(countResult > 0 ? View.VISIBLE : View.GONE);
                            rvBooks.setVisibility(countResult > 0 ? View.VISIBLE : View.GONE);
                        });
                    }
                    layoutSearchVerses.setVisibility(View.VISIBLE);
                    searchHandler.removeCallbacks(searchRunnable);
                    if (query.length() >= 3) {
                        searchRunnable = () -> searchVersesFromAPI(query);
                        searchHandler.postDelayed(searchRunnable, 600);
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchVersesFromAPI(String query) {
        String url = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/api.php?action=search&query=" + query;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    searchResultsVerses.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String vKey = obj.getString("livro") + "_" + obj.getInt("capitulo") + "_" + obj.getInt("versiculo");
                            String ref = obj.getString("livro") + " " + obj.getInt("capitulo") + ":" + obj.getInt("versiculo");
                            searchResultsVerses.add(new FavModel(vKey, ref, obj.getString("texto"), null, "", null));
                        }
                        setupVersesAdapter();
                    } catch (JSONException e) {
                        Log.e("SEARCH_ERROR", "Erro no JSON");
                    }
                },
                error -> Log.e("SEARCH_ERROR", "Erro de rede")
        );
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void setupVersesAdapter() {
        if (searchAdapter == null) {
            searchAdapter = new SearchVersesAdapter(searchResultsVerses, item -> {
                // MUDANÇA AQUI: Pegamos o Fragment pai em vez da Activity
                Fragment parent = getParentFragment();
                if (parent instanceof BibliaFragment) {
                    String[] partes = item.getVKey().split("_");
                    BibliaFragment biblia = (BibliaFragment) parent;

                    biblia.onBookSelected(partes[0], false);
                    biblia.onChapterSelected(Integer.parseInt(partes[1]), false);
                    biblia.onVerseSelected(Integer.parseInt(partes[2]));
                }
            });
            rvSearchVerses.setAdapter(searchAdapter);
        } else {
            searchAdapter.notifyDataSetChanged();
        }
    }

    private void loadBooksFromAPI() {
        String url = "https://softcroy.com/app_santos_v2/biblia/biblia_pt_br_arc/api/api.php?action=get_books";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    bookList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            bookList.add(response.getString(i));
                        }
                        if (!bookList.isEmpty()) setupAdapter();
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "Erro nos livros");
                    }
                },
                error -> {
                    if (isAdded()) Toast.makeText(getContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void setupAdapter() {
        adapter = new BookAdapter(bookList, bookName -> {
            // MUDANÇA AQUI: Pegamos o Fragment pai
            Fragment parent = getParentFragment();
            if (parent instanceof BibliaFragment) {
                ((BibliaFragment) parent).onBookSelected(bookName, true);
            }
        });
        rvBooks.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}