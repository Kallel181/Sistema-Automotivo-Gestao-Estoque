package com.concessionaria.estoque.repositories;

import com.concessionaria.estoque.entities.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    @Query("SELECT v FROM Veiculo v JOIN v.modelo m JOIN m.marca br WHERE " +
            "(:marca IS NULL OR br.nomeMarca LIKE %:marca%) AND " +
            "(:modelo IS NULL OR m.nomeModelo LIKE %:modelo%) AND " +
            "(:ano IS NULL OR v.anoFabricacao = :ano) AND " +
            "(:status IS NULL OR v.statusDisponibilidade = :status) AND " +
            "(:precoMax IS NULL OR v.preco <= :precoMax)")
    List<Veiculo> buscarComFiltros(
            @Param("marca") String marca,
            @Param("modelo") String modelo,
            @Param("ano") Integer ano,
            @Param("status") String status,
            @Param("precoMax") Double precoMax
    );
}