package com.concessionaria.estoque.repositories;

import com.concessionaria.estoque.entities.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    Optional<Marca> findByNomeMarcaIgnoreCase(String nomeMarca);
}