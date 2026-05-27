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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarMarca(@PathVariable Long id) {
        // 1. Verifica se a marca existe no banco de dados
        if (!marcaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Marca com ID " + id + " não encontrada."));
        }

        try {
            // 2. Tenta deletar a marca
            marcaRepository.deleteById(id);
            // Retorna status 204 No Content (sucesso sem corpo na resposta)
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Trata a violação de integridade referencial (Foreign Key Constraint)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Não é possível deletar esta marca pois existem modelos vinculados a ela no sistema."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarMarca(@PathVariable Long id, @RequestBody Map<String, String> body) {
        // 1. Busca a marca existente pelo ID. Se não achar, retorna 404 Not Found.
        return marcaRepository.findById(id).map(marcaExistente -> {

            // 2. Valida se o corpo da requisição e o campo esperado foram enviados
            if (body == null || !body.containsKey("nome_marca")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "O campo 'nome_marca' é obrigatório para atualização."));
            }

            String novoNome = body.get("nome_marca").strip();

            // 3. Valida se já existe OUTRA marca cadastrada com esse mesmo nome (evita duplicidade)
            var marcaDuplicada = marcaRepository.findByNomeMarcaIgnoreCase(novoNome);
            if (marcaDuplicada.isPresent() && !marcaDuplicada.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("erro", "A marca '" + novoNome + "' já está cadastrada no sistema."));
            }

            // 4. Atualiza o nome da marca encontrada
            marcaExistente.setNomeMarca(novoNome); // Certifique-se de usar o nome exato do seu setter (ex: setNome ou setNomeMarca)

            // 5. Salva a alteração. Como o objeto possui ID do banco, o Hibernate realiza o UPDATE
            Marca marcaAtualizada = marcaRepository.save(marcaExistente);
            return ResponseEntity.ok(marcaAtualizada);

        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Marca com ID " + id + " não encontrada.")));
    }
}