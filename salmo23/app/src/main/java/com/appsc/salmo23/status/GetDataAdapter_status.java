package com.appsc.salmo23.status;

public class GetDataAdapter_status {

    int id;
    String image;
    int visualizacao; // Adicionado campo visualizacao

    private boolean favorite;

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return image;
    }

    public String getimage() {
        return image;
    }

    public void setimage(String image) {
        this.image = image;
    }

    public int getVisualizacao() { // Getter para visualizacao
        return visualizacao;
    }

    public void setVisualizacao(int visualizacao) { // Setter para visualizacao
        this.visualizacao = visualizacao;
    }
}
