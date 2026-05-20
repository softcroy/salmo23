package com.appsc.salmo23.aplicativos;

/**
 * Created by JUNED on 6/16/2016.
 */
public class GetDataAdapter_aplicativos {

    String id;
    String link_image;
    String link_play;
    String tipo;
    String nome;
    private String imageUrl;


    public String getLink() {return link_image;}public void setLink(String link_image) {this.link_image = link_image;}


    public String getVisualizacao() {return nome;}public void setVisualizacao(String nome) {this.nome = nome;}



    public String getId() {return id;}public void setId(String id) {this.id = id;}


    public String gethashtags() {return link_play;}public void sethashtags(String link_play) {this.link_play = link_play;}


    public String getmodelo() {return tipo;}public void setmodelo(String tipo) {this.tipo = tipo;}


    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}public String getImageUrl() {return imageUrl;}

}
