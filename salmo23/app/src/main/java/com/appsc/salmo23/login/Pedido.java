package com.appsc.salmo23.login;

public class Pedido {
    private String nome;
    private String pedido;
    private String data;
    private int likes;
    private String androidId;
    private String ownerAndroidId;

    public Pedido(String nome, String pedido, String data, int likes) {
        this.nome   = nome;
        this.pedido = pedido;
        this.data   = data;
        this.likes  = likes;
        this.androidId   = androidId;
        this.ownerAndroidId  = ownerAndroidId;
    }

    public String getNome() {
        return nome;
    }

    public String getPedido() {
        return pedido;
    }

    public String getData() {
        return data;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getAndroidId()   { return androidId; }
    public String getOwnerAndroidId() { return ownerAndroidId; }
}
