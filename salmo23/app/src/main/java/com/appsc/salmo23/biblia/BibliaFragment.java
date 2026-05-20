package com.appsc.salmo23.biblia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.appsc.salmo23.Principal_Oracao;
import com.appsc.salmo23.R;
import com.appsc.salmo23.biblia.favorito.FavoritosActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.NoEncryption;

public class BibliaFragment extends Fragment {

    public ViewPager2 viewPager;
    private TabLayout tabLayout;

    private String selectedBook = "";
    private int selectedChapter = 0;
    private int selectedVerse = 0;

    private static final String KEY_LAST_BOOK = "last_accessed_book";
    private static final String KEY_LAST_CHAP = "last_main_chapter";

    public String getSelectedBook() {
        return (selectedBook == null || selectedBook.isEmpty()) ? "Gênesis" : selectedBook;
    }

    public int getSelectedChapter() {
        return (selectedChapter > 0) ? selectedChapter : 1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.biblia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Habilita o menu específico para este Fragment
        setHasOptionsMenu(true);

        if (!Hawk.isBuilt()) {
            Hawk.init(requireContext()).setEncryption(new NoEncryption()).build();
        }

        selectedBook = Hawk.get(KEY_LAST_BOOK, "Gênesis");
        selectedChapter = Hawk.get(KEY_LAST_CHAP, 1);

        viewPager = view.findViewById(R.id.viewPagerFav);
        tabLayout = view.findViewById(R.id.tabLayoutFav);

        updateHeaderText("Bíblia");

        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateToolbarTextByPosition(position);
                notificarMudancaAba(position);
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Livro"); break;
                case 1: tab.setText("Capítulo"); break;
                case 2: tab.setText("Versículo"); break;
            }
        }).attach();

        updateToolbarTextByPosition(viewPager.getCurrentItem());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentTab = viewPager.getCurrentItem();
                if (currentTab > 0) {
                    viewPager.setCurrentItem(currentTab - 1, true);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    // --- LOGICA DE MENU CORRIGIDA ---
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Limpa o menu da Activity pai para não duplicar ícones
        menu.clear();
        inflater.inflate(R.menu.menu_biblia, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.salvos) {
            Intent intent = new Intent(getContext(), FavoritosActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- ATUALIZAÇÃO DA TOOLBAR ---
    @Override
    public void onResume() {
        super.onResume();
        updateToolbarTextByPosition(viewPager.getCurrentItem());
    }

    private void updateHeaderText(String text) {
        if (getActivity() instanceof Principal_Oracao) {
            Principal_Oracao activityPai = (Principal_Oracao) getActivity();
            if (activityPai.viewPager.getCurrentItem() == 2 && activityPai.tv != null) {
                activityPai.tv.setText(text);
            }
        }
    }

    private void notificarMudancaAba(int position) {
        if (selectedBook == null || selectedBook.isEmpty()) {
            selectedBook = "Gênesis";
            selectedChapter = 1;
        }

        if (position == 1) {
            CapituloFragment frag = (CapituloFragment) getChildFragmentManager().findFragmentByTag("f1");
            if (frag != null) frag.carregarCapitulos(selectedBook);
        } else if (position == 2) {
            VersiculoFragment frag = (VersiculoFragment) getChildFragmentManager().findFragmentByTag("f2");
            if (frag != null) {
                int capParaCarregar = (selectedChapter > 0) ? selectedChapter : 1;
                frag.carregarVersiculos(selectedBook, capParaCarregar);
            }
        }
    }

    public void onBookSelected(String bookName, boolean jumpToChapters) {
        this.selectedBook = bookName;
        this.selectedChapter = 1;
        Hawk.put(KEY_LAST_BOOK, bookName);
        Hawk.put(KEY_LAST_CHAP, 1);
        if (jumpToChapters) viewPager.setCurrentItem(1, true);
        else updateToolbarTextByPosition(0);
    }

    public void onChapterSelected(int chapter, boolean jump) {
        this.selectedChapter = chapter;
        Hawk.put(KEY_LAST_CHAP, chapter);
        if (jump) viewPager.setCurrentItem(2, true);
        else updateToolbarTextByPosition(1);
    }

    public void onVerseSelected(int verse) {
        Intent intent = new Intent(getActivity(), ReadingActivity.class);
        intent.putExtra("LIVRO", selectedBook);
        intent.putExtra("CAPITULO", (selectedChapter > 0 ? selectedChapter : 1));
        intent.putExtra("VERSICULO", verse);
        startActivity(intent);
    }

    private void updateToolbarTextByPosition(int position) {
        String book = getSelectedBook();
        int capDisplay = getSelectedChapter();

        switch (position) {
            case 0: updateHeaderText(book); break;
            case 1: updateHeaderText(book + " " + capDisplay); break;
            case 2: updateHeaderText(book + " " + capDisplay + (selectedVerse > 0 ? ":" + selectedVerse : "")); break;
        }
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @NonNull @Override public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new LivrosFragment();
                case 1: return new CapituloFragment();
                case 2: return new VersiculoFragment();
                default: return new LivrosFragment();
            }
        }
        @Override public int getItemCount() { return 3; }
    }
}