package com.concessionaria.estoque.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @Column(nullable = false)
    private Integer anoFabricacao;

    @Column(nullable = false, length = 30)
    private String cor;

    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private Integer quilometragem;

    @Column(nullable = false, length = 30)
    private String statusDisponibilidade = "Disponível";

    public Veiculo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Modelo getModelo() { return modelo; }
    public void setModelo(Modelo modelo) { this.modelo = modelo; }
    public Integer getAnoFabricacao() { return anoFabricacao; }
    public void setAnoFabricacao(Integer anoFabricacao) { this.anoFabricacao = anoFabricacao; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
    public Integer getQuilometragem() { return quilometragem; }
    public void setQuilometragem(Integer quilometragem) { this.quilometragem = quilometragem; }
    public String getStatusDisponibilidade() { return statusDisponibilidade; }
    public void setStatusDisponibilidade(String statusDisponibilidade) { this.statusDisponibilidade = statusDisponibilidade; }
}