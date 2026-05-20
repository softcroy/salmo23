package com.appsc.salmo23.novena;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Novena implements Serializable {
    private String id, nome, imagem, padroeiro, dataTradicional, diaFesta, historia, rezasJson;

    // Campos auxiliares para carregar o progresso e as rezas na NovenaActivityProgresso
    private String auxCausa;
    private String auxDataInicio;
    private String auxDataTermino;
    private int auxDiasConcluidos;

    // NOVO: Campo para ordenação por proximidade da festa (calculado pelo PHP)
    private int diasRestantes;
    private int ordemOriginal;

    private List<String> listaRezas; // Armazena apenas o texto principal (compatibilidade)

    // NOVO: Armazena o JSON completo de cada dia (texto + orações extras)
    private List<String> listaRezasCompletas;

    // Novo campo para armazenar a data de conclusão de cada dia (Dia -> Data Formatada)
    private Map<Integer, String> datasConclusao = new HashMap<>();

    public Novena() {}

    // Getters e Setters Padrão
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }

    public String getPadroeiro() { return padroeiro; }
    public void setPadroeiro(String padroeiro) { this.padroeiro = padroeiro; }

    public String getDataTradicional() { return dataTradicional; }
    public void setDataTradicional(String dataTradicional) { this.dataTradicional = dataTradicional; }
    public String getDiaFesta() { return diaFesta; }
    public void setDiaFesta(String diaFesta) { this.diaFesta = diaFesta; }
    public String getHistoria() { return historia; }
    public void setHistoria(String historia) { this.historia = historia; }
    public String getRezasJson() { return rezasJson; }
    public void setRezasJson(String rezasJson) { this.rezasJson = rezasJson; }

    // NOVO: Métodos para gerenciar a ordenação por data de festa
    public int getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(int diasRestantes) { this.diasRestantes = diasRestantes; }

    public int getOrdemOriginal() {
        return ordemOriginal;
    }

    public void setOrdemOriginal(int ordemOriginal) {
        this.ordemOriginal = ordemOriginal;
    }

    // Getters e Setters Auxiliares
    public String getAuxCausa() { return auxCausa; }
    public void setAuxCausa(String auxCausa) { this.auxCausa = auxCausa; }
    public String getAuxDataInicio() { return auxDataInicio; }
    public void setAuxDataInicio(String auxDataInicio) { this.auxDataInicio = auxDataInicio; }

    public String getAuxDataTermino() { return auxDataTermino; }
    public void setAuxDataTermino(String auxDataTermino) { this.auxDataTermino = auxDataTermino; }

    public int getAuxDiasConcluidos() { return auxDiasConcluidos; }
    public void setAuxDiasConcluidos(int auxDiasConcluidos) { this.auxDiasConcluidos = auxDiasConcluidos; }

    // Métodos para a Lista de Rezas (Texto Simples)
    public List<String> getListaRezas() { return listaRezas; }
    public void setListaRezas(List<String> listaRezas) { this.listaRezas = listaRezas; }

    // NOVO: Métodos para a Lista de Rezas Completas (Objetos JSON com extras)
    public List<String> getListaRezasCompletas() { return listaRezasCompletas; }
    public void setListaRezasCompletas(List<String> listaRezasCompletas) { this.listaRezasCompletas = listaRezasCompletas; }

    // Métodos para gerenciar as datas de conclusão
    public void addDataConclusao(int dia, String data) {
        this.datasConclusao.put(dia, data);
    }

    public String getDataDoDia(int dia) {
        return this.datasConclusao.get(dia);
    }

    public Map<Integer, String> getDatasConclusao() {
        return datasConclusao;
    }

    public void limparHistorico() {
        this.datasConclusao.clear();
    }
}