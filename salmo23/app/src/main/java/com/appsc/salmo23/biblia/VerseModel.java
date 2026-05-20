package com.appsc.salmo23.biblia;

public class VerseModel {
    private int versiculo;
    private String texto;

    public VerseModel(int versiculo, String texto) {
        this.versiculo = versiculo;
        this.texto = texto;
    }

    public int getVersiculo() { return versiculo; }
    public String getTexto() { return texto; }
}