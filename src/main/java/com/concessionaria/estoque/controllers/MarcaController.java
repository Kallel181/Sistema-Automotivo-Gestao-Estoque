package com.concessionaria.estoque.controllers;

import com.concessionaria.estoque.entities.Marca;
import com.concessionaria.estoque.repositories.MarcaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marcas")
public class MarcaController {

    @Autowired
    private MarcaRepository marcaRepository;

    @PostMapping
    public ResponseEntity<?> cadastrarMarca(@RequestBody Map<String, String> body) {
        if (body == null || !body.containsKey("nome_marca")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Dados inválidos"));
        }
        String nome = body.get("nome_marca").strip();

        if (marcaRepository.findByNomeMarcaIgnoreCase(nome).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "A marca '" + nome + "' já está cadastrada."));
        }

        Marca novaMarca = marcaRepository.save(new Marca(nome));
        return ResponseEntity.status(HttpStatus.CREATED).body(novaMarca);
    }

    @GetMapping
    public List<Marca> listarMarcas() {
        return marcaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obterMarca(@PathVariable Long id) {
        return marcaRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Marca não encontrada")));
    }
}