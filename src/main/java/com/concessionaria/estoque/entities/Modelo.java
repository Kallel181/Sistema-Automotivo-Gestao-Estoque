package com.concessionaria.estoque.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "modelos")
public class Modelo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nomeModelo;

    @ManyToOne
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    public Modelo() {}
    public Modelo(String nomeModelo, Marca marca) {
        this.nomeModelo = nomeModelo;
        this.marca = marca;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeModelo() { return nomeModelo; }
    public void setNomeModelo(String nomeModelo) { this.nomeModelo = nomeModelo; }
    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = marca; }
}