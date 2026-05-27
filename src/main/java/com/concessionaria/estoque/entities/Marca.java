package com.concessionaria.estoque.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "marcas")
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nomeMarca;

    @OneToMany(mappedBy = "marca", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Modelo> modelos;

    public Marca() {}
    public Marca(String nomeMarca) { this.nomeMarca = nomeMarca; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeMarca() { return nomeMarca; }
    public void setNomeMarca(String nomeMarca) { this.nomeMarca = nomeMarca; }
}