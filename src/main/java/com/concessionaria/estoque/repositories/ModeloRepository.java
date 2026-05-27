package com.concessionaria.estoque.repositories;

import com.concessionaria.estoque.entities.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    Optional<Modelo> findByNomeModeloAndMarcaId(String nomeModelo, Long marcaId);
    List<Modelo> findByMarcaNomeMarcaIgnoreCase(String nomeMarca);
}