from flask import Flask, request, jsonify
from config import Config
from models import db, Marca, Modelo, Veiculo

app = Flask(__name__)
app.config.from_object(Config)
db.init_app(app)

# Cria as tabelas no MySQL se não existirem ao iniciar o app
with app.app_context():
  db.create_all()

# --- ROTAS DE CADASTRO AUXILIARES (Marca e Modelo) ---
@app.route('/marcas', methods=['POST'])
def cadastrar_marca():
  data = request.get_json()
  if not data or 'nome_marca' not in data:
    return jsonify({"erro": "Dados inválidos"}), 400
    
  # Remove espaços em branco extras nas pontas para evitar "Fiat " e "Fiat"
  nome = data['nome_marca'].strip()
    
  # Busca no banco se já existe uma marca com esse exato nome
  marca_existente = Marca.query.filter_by(nome_marca=nome).first()
  if marca_existente:
    return jsonify({"erro": f"A marca '{nome}' já está cadastrada no sistema."}), 409
        
  nova_marca = Marca(nome_marca=nome)
  db.session.add(nova_marca)
  db.session.commit()
  return jsonify(nova_marca.to_dict()), 201


@app.route('/modelos', methods=['POST'])
def cadastrar_modelo():
  data = request.get_json()
    
  # 1. Validação básica de campos obrigatórios
  if not data or 'nome_modelo' not in data or 'marca_id' not in data:
    return jsonify({"erro": "Dados inválidos"}), 400
        
  nome = data['nome_modelo'].strip()
  id_da_marca = data['marca_id']
    
  # 2. Verifica se a marca informada existe no banco de dados
  marca_existe = Marca.query.get(id_da_marca)
  if not marca_existe:
    return jsonify({"erro": f"Operação abortada. A marca com ID {id_da_marca} não existe no sistema."}), 409

  # 3. Verifica se este modelo já existe cadastrado PARA ESTA mesma marca
  modelo_duplicado = Modelo.query.filter_by(nome_modelo=nome, marca_id=id_da_marca).first()
  if modelo_duplicado:
    return jsonify({"erro": f"O modelo '{nome}' já está cadastrado para a marca '{marca_existe.nome_marca}'."}), 409
        
  # Se passou por todas as barreiras, faz a inserção segura
  novo_modelo = Modelo(nome_modelo=nome, marca_id=id_da_marca)
  db.session.add(novo_modelo)
  db.session.commit()
    
  return jsonify(novo_modelo.to_dict()), 201


# --- ROTAS DO CRUD DE VEÍCULOS ---

# 1. Cadastro de Veículo (POST)
@app.route('/veiculos', methods=['POST'])
def cadastrar_veiculo():
  data = request.get_json()
  try:
    novo_veiculo = Veiculo(
      modelo_id=data['modelo_id'],
      ano_fabricacao=data['ano_fabricacao'],
      cor=data['cor'],
      preco=data['preco'],
      quilometragem=data['quilometragem'],
      status_disponibilidade=data.get('status_disponibilidade', 'Disponível')
    )
    db.session.add(novo_veiculo)
    db.session.commit()
    
    return jsonify(novo_veiculo.to_dict()), 201
  
  except Exception as e:
      return jsonify({"erro": "Erro ao cadastrar veículo. Verifique os campos e chaves estrangeiras."}), 400

# 2. Consulta com Filtros Inteligentes (GET)
@app.route('/veiculos', methods=['GET'])
def listar_veiculos():
  query = Veiculo.query
    
  # Capturando parâmetros da URL para os filtros (?marca=Fiat&cor=Preto...)
  marca = request.args.get('marca')
  modelo = request.args.get('modelo')
  ano = request.args.get('ano')
  status = request.args.get('status')
  preco_max = request.args.get('preco_max')

  # Aplicando os filtros dinamicamente caso venham na requisição
  if marca:
    query = query.join(Modelo).join(Marca).filter(Marca.nome_marca.like(f"%{marca}%"))
  if modelo:
    query = query.join(Modelo).filter(Modelo.nome_modelo.like(f"%{modelo}%"))
  if ano:
    query = query.filter(Veiculo.ano_fabricacao == int(ano))
  if status:
    query = query.filter(Veiculo.status_disponibilidade == status)
  if preco_max:
    query = query.filter(Veiculo.preco <= float(preco_max))

  veiculos = query.all()
  
  return jsonify([v.to_dict() for v in veiculos]), 200

# 3. Atualização Parcial (PUT / PATCH)
@app.route('/veiculos/<int:id>', methods=['PUT'])
def atualizar_veiculo(id):
  veiculo = Veiculo.query.get_or_400(id)
  data = request.get_json()

  # Atualiza apenas o que foi enviado no JSON (Flexibilidade)
  if 'preco' in data:
    veiculo.preco = data['preco']
  if 'quilometragem' in data:
    veiculo.quilometragem = data['quilometragem']
  if 'status_disponibilidade' in data:
    veiculo.status_disponibilidade = data['status_disponibilidade']

  db.session.commit()
  
  return jsonify(veiculo.to_dict()), 200

# 4. Remoção de Veículo (DELETE)
@app.route('/veiculos/<int:id>', methods=['DELETE'])
def remover_veiculo(id):
  veiculo = Veiculo.query.get_or_400(id)
  db.session.delete(veiculo)
  db.session.commit()
  
  return jsonify({"mensagem": f"Veículo ID {id} removido com sucesso do estoque."}), 200

if __name__ == '__main__':
  app.run(debug=True)