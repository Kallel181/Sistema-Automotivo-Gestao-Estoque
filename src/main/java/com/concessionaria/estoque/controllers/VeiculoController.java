package com.concessionaria.estoque.controllers;

import com.concessionaria.estoque.entities.Modelo;
import com.concessionaria.estoque.entities.Veiculo;
import com.concessionaria.estoque.repositories.ModeloRepository;
import com.concessionaria.estoque.repositories.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoRepository veiculoRepository;
    @Autowired
    private ModeloRepository modeloRepository;

    @PostMapping
    public ResponseEntity<?> cadastrarVeiculo(@RequestBody Map<String, Object> body) {
        try {
            Long modeloId = Long.valueOf(body.get("modelo_id").toString());
            Integer ano = (Integer) body.get("ano_fabricacao");
            String cor = (String) body.get("cor");
            Double preco = Double.valueOf(body.get("preco").toString());
            Integer km = (Integer) body.get("quilometragem");
            String status = body.containsKey("status_disponibilidade") ? (String) body.get("status_disponibilidade") : "Disponível";

            Modelo modelo = modeloRepository.findById(modeloId)
                    .orElseThrow(() -> new RuntimeException("Modelo inexistente"));

            Veiculo v = new Veiculo();
            v.setModelo(modelo);
            v.setAnoFabricacao(ano);
            v.setCor(cor);
            v.setPreco(preco);
            v.setQuilometragem(km);
            v.setStatusDisponibilidade(status);

            return ResponseEntity.status(HttpStatus.CREATED).body(veiculoRepository.save(v));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Erro ao cadastrar veículo."));
        }
    }

    @GetMapping
    public List<Veiculo> listarVeiculos(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double preco_max) {
        return veiculoRepository.buscarComFiltros(marca, modelo, ano, status, preco_max);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarVeiculo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return veiculoRepository.findById(id).map(v -> {
            if (body.containsKey("preco")) v.setPreco(Double.valueOf(body.get("preco").toString()));
            if (body.containsKey("quilometragem")) v.setQuilometragem((Integer) body.get("quilometragem"));
            if (body.containsKey("status_disponibilidade")) v.setStatusDisponibilidade((String) body.get("status_disponibilidade"));

            return ResponseEntity.ok(veiculoRepository.save(v));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerVeiculo(@PathVariable Long id) {
        return veiculoRepository.findById(id).map(v -> {
            veiculoRepository.delete(v);
            return ResponseEntity.ok(Map.of("mensagem", "Veículo ID " + id + " removido."));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}