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

    // --- 1. POST: CADASTRAR VEÍCULO ---
    @PostMapping
    public ResponseEntity<?> cadastrarVeiculo(@RequestBody VeiculoDTO dto) {
        // Validação de segurança inicial
        if (dto.getNomeModelo() == null || dto.getNomeModelo().strip().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "O campo 'nomeModelo' é obrigatório."));
        }

        String nomeModeloLimpio = dto.getNomeModelo().strip();

        // Solução Universal: Busca todos os modelos e filtra via Stream usando o getter getNomeModelo()
        Modelo modelo = modeloRepository.findAll().stream()
                .filter(m -> m.getNomeModelo() != null && m.getNomeModelo().equalsIgnoreCase(nomeModeloLimpio))
                .findFirst()
                .orElse(null);

        if (modelo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", "O modelo '" + nomeModeloLimpio + "' não existe no sistema. Cadastre o modelo primeiro."));
        }

        // Cria e popula a entidade Veiculo
        Veiculo v = new Veiculo();
        v.setModelo(modelo); // O veículo ganha o modelo encontrado (que já possui a marca vinculada)
        v.setAnoFabricacao(dto.getAnoFabricacao());
        v.setCor(dto.getCor());
        v.setPreco(dto.getPreco());
        v.setQuilometragem(dto.getQuilometragem());

        // Define status padrão caso venha vazio
        String status = (dto.getStatusDisponibilidade() != null && !dto.getStatusDisponibilidade().strip().isEmpty())
                ? dto.getStatusDisponibilidade().strip()
                : "Disponível";
        v.setStatusDisponibilidade(status);

        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoRepository.save(v));
    }

    // --- 2. GET: LISTAR VEÍCULOS (COM FILTROS) ---
    @GetMapping
    public List<Veiculo> listarVeiculos(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double preco_max) {
        return veiculoRepository.buscarComFiltros(marca, modelo, ano, status, preco_max);
    }

    // --- 3. PUT: ATUALIZAR VEÍCULO ---
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarVeiculo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return veiculoRepository.findById(id).map(v -> {
            if (body.containsKey("preco")) v.setPreco(Double.valueOf(body.get("preco").toString()));
            if (body.containsKey("quilometragem")) v.setQuilometragem((Integer) body.get("quilometragem"));
            if (body.containsKey("status_disponibilidade")) v.setStatusDisponibilidade((String) body.get("status_disponibilidade"));
            if (body.containsKey("cor")) v.setCor((String) body.get("cor"));
            if (body.containsKey("anoFabricacao")) v.setAnoFabricacao((Integer) body.get("anoFabricacao"));

            return ResponseEntity.ok(veiculoRepository.save(v));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // --- 4. DELETE: REMOVER VEÍCULO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerVeiculo(@PathVariable Long id) {
        if (!veiculoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Veículo com ID " + id + " não encontrado."));
        }
        veiculoRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    // --- CLASSE AUXILIAR (DTO) ---
    public static class VeiculoDTO {
        private String nomeModelo;
        private Integer anoFabricacao;
        private String cor;
        private Double preco;
        private Integer quilometragem;
        private String statusDisponibilidade;

        public VeiculoDTO() {
        }

        public String getNomeModelo() { return nomeModelo; }
        public void setNomeModelo(String nomeModelo) { this.nomeModelo = nomeModelo; }

        public Integer getAnoFabricacao() { return anoFabricacao; }
        public void setAnoFabricacao(Integer anoFabricacao) { this.anoFabricacao = anoFabricacao; }

        public String getCor() { return cor; }
        public void setCor(String cor) { this.cor = cor; }

        public Double getPreco() { return preco; }
        public void setPreco(Double preco) { this.preco = preco; }

        public Integer getQuilometragem() { return quilometragem; }
        public void setQuilometragem(Integer quilometragem) { this.quilometragem = quilometragem; }

        public String getStatusDisponibilidade() { return statusDisponibilidade; }
        public void setStatusDisponibilidade(String statusDisponibilidade) { this.statusDisponibilidade = statusDisponibilidade; }
    }
}