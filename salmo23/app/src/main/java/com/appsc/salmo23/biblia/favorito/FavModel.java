package com.appsc.salmo23.biblia.favorito;

public class FavModel {
    private String vKey;      // Ex: Genesis_1_1
    private String referencia; // Ex: Gênesis 1:1
    private String texto;      // Texto do versículo
    private String nota;       // Conteúdo da nota (se houver)
    private String data;       // Mar, 03, 2026
    private String cor;        // Hex da cor (se for aba Cor)

    public FavModel(String vKey, String referencia, String texto, String nota, String data, String cor) {
        this.vKey = vKey;
        this.referencia = referencia;
        this.texto = texto;
        this.nota = nota;
        this.data = data;
        this.cor = cor;
    }

    // Getters
    public String getReferencia() { return referencia; }
    public String getTexto() { return texto; }
    public String getNota() { return nota; }
    public String getData() { return data; }
    public String getCor() { return cor; }
    public String getVKey() { return vKey; }
}