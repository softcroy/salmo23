package com.appsc.salmo23.novena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.appsc.salmo23.R;

public class NovenaPrincipalFragment extends Fragment {

    public NovenaPrincipalFragment() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_principal, container, false);

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation);

        // Define a tela inicial
        if (savedInstanceState == null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ListaNovenasFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                selectedFragment = new ListaNovenasFragment();
            } else if (id == R.id.nav_concluidas) {
                selectedFragment = new ConcluidasFragment();
            }

            if (selectedFragment != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        return view;
    }
}