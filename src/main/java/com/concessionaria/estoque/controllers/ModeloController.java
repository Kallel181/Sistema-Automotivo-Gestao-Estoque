package com.concessionaria.estoque.controllers;

import com.concessionaria.estoque.entities.Marca;
import com.concessionaria.estoque.entities.Modelo;
import com.concessionaria.estoque.repositories.MarcaRepository;
import com.concessionaria.estoque.repositories.ModeloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/modelos")
public class ModeloController {

    @Autowired
    private ModeloRepository modeloRepository;
    @Autowired
    private MarcaRepository marcaRepository;

    @PostMapping
    public ResponseEntity<?> cadastrarModelo(@RequestBody Map<String, String> body) {
        if (body == null || !body.containsKey("nome_modelo") || !body.containsKey("nome_marca")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Dados inválidos"));
        }

        String nomeModelo = body.get("nome_modelo").strip();
        String nomeMarca = body.get("nome_marca").strip();

        Marca marca = marcaRepository.findByNomeMarcaIgnoreCase(nomeMarca)
                .orElseGet(() -> marcaRepository.save(new Marca(nomeMarca)));

        if (modeloRepository.findByNomeModeloAndMarcaId(nomeModelo, marca.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "O modelo '" + nomeModelo + "' já existe para a marca."));
        }

        Modelo novoModelo = modeloRepository.save(new Modelo(nomeModelo, marca));
        return ResponseEntity.status(HttpStatus.CREATED).body(novoModelo);
    }

    @GetMapping
    public ResponseEntity<?> listarModelos() {
        return ResponseEntity.ok(modeloRepository.findAll());
    }

    @GetMapping("/marca/{nomeMarca}")
    public ResponseEntity<?> listarPorMarca(@PathVariable String nomeMarca) {
        return ResponseEntity.ok(modeloRepository.findByMarcaNomeMarcaIgnoreCase(nomeMarca));
    }
}