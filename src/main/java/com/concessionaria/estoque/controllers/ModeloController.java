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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarModelo(@PathVariable Long id) {
        // 1. Verifica se o modelo realmente existe no banco de dados
        if (!modeloRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Modelo com ID " + id + " não encontrado."));
        }

        try {
            // 2. Tenta deletar o modelo
            modeloRepository.deleteById(id);
            // Retorna status 204 No Content (sucesso, sem corpo na resposta)
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Caso o modelo esteja amarrado a um Veículo (Chave Estrangeira)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Não é possível deletar este modelo pois existem veículos vinculados a ele."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarModelo(@PathVariable Long id, @RequestBody Map<String, String> body) {
        // 1. Busca o modelo existente pelo ID. Se não achar, retorna 404.
        return modeloRepository.findById(id).map(modeloExistente -> {

            // 2. Valida se o corpo da requisição não veio vazio
            if (body == null || (!body.containsKey("nome_modelo") && !body.containsKey("nome_marca"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Nenhum dado válido para atualização foi fornecido."));
            }

            // 3. Atualiza a Marca (se foi enviada no JSON)
            if (body.containsKey("nome_marca")) {
                String nomeMarca = body.get("nome_marca").strip();
                // Busca a marca pelo nome ou cria uma nova se não existir
                Marca marca = marcaRepository.findByNomeMarcaIgnoreCase(nomeMarca)
                        .orElseGet(() -> marcaRepository.save(new Marca(nomeMarca)));
                modeloExistente.setMarca(marca);
            }

            // 4. Atualiza o Nome do Modelo (se foi enviado no JSON)
            if (body.containsKey("nome_modelo")) {
                String novoNomeModelo = body.get("nome_modelo").strip();

                // Valida se já existe OUTRO modelo com esse mesmo nome para a mesma marca (evita duplicidade)
                var modeloDuplicado = modeloRepository.findByNomeModeloAndMarcaId(novoNomeModelo, modeloExistente.getMarca().getId());
                if (modeloDuplicado.isPresent() && !modeloDuplicado.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("erro", "O modelo '" + novoNomeModelo + "' já existe para esta marca."));
                }

                modeloExistente.setNomeModelo(novoNomeModelo); // Ajuste para o nome correto do seu set (ex: setNome ou setNomeModelo)
            }

            // 5. Salva as alterações. Como o objeto veio do banco e tem ID, o Hibernate faz um UPDATE
            Modelo modeloAtualizado = modeloRepository.save(modeloExistente);
            return ResponseEntity.ok(modeloAtualizado);

        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Modelo com ID " + id + " não encontrado.")));
    }
}