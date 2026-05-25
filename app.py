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
#POST marcas
@app.route('/marcas', methods=['POST'])
def cadastrar_marca():
  data = request.get_json()
  if not data or 'nome_marca' not in data:
    return jsonify({"erro": "Dados invalidos"}), 400
    
  # Remove espaços em branco extras nas pontas para evitar "Fiat " e "Fiat"
  nome = data['nome_marca'].strip()
    
  # Busca no banco se já existe uma marca com esse exato nome
  marca_existente = Marca.query.filter_by(nome_marca=nome).first()
  if marca_existente:
    return jsonify({"erro": f"A marca '{nome}' ja esta cadastrada no sistema."}), 409
        
  nova_marca = Marca(nome_marca=nome)
  db.session.add(nova_marca)
  db.session.commit()
  return jsonify(nova_marca.to_dict()), 201


# GET Listar todas as marcas
@app.route('/marcas', methods=['GET'])
def listar_marcas():
  marcas = Marca.query.all()
  # Retorna uma lista de dicionários contendo [{"id": 1, "nome_marca": "Fiat"}, ...]
  return jsonify([m.to_dict() for m in marcas]), 200


# GET Buscar uma marca específica pelo ID
@app.route('/marcas/<int:id>', methods=['GET'])
def obter_marca(id):
  marca = Marca.query.get(id)
  if not marca:
    return jsonify({"erro": f"Marca com ID {id} nao encontrada."}), 404
        
  return jsonify(marca.to_dict()), 200








@app.route('/modelos', methods=['POST'])
def cadastrar_modelo():
  data = request.get_json()
    
  # Agora esperamos 'nome_marca' como string em vez de 'marca_id'
  if not data or 'nome_modelo' not in data or 'nome_marca' not in data:
    return jsonify({"erro": "Dados invalidos"}), 400
        
  nome_modelo = data['nome_modelo'].strip()
  nome_marca = data['nome_marca'].strip()
    
  # 1. Busca a marca por texto no banco de dados (ignorando maiúsculas/minúsculas)
  marca = Marca.query.filter(Marca.nome_marca.ilike(nome_marca)).first()
    
  # 2. Se a marca não existir, o sistema cria ela automaticamente na hora!
  if not marca:
    marca = Marca(nome_marca=nome_marca)
    db.session.add(marca)
    db.session.flush() # Executa a criação no banco para gerar o ID, mas não finaliza a transação ainda
    
  # 3. Agora que temos a garantia do objeto 'marca' (novo ou existente), verificamos duplicidade do modelo
  modelo_duplicado = Modelo.query.filter_by(nome_modelo=nome_modelo, marca_id=marca.id).first()
  if modelo_duplicado:
    return jsonify({"erro": f"O modelo '{nome_modelo}' ja existe para a marca '{marca.nome_marca}'."}), 409
        
  # 4. Cria o modelo usando o ID que o Flask localizou ou gerou
  novo_modelo = Modelo(nome_modelo=nome_modelo, marca_id=marca.id)
  db.session.add(novo_modelo)
  db.session.commit() # Salva tudo de uma vez no MySQL
    
  return jsonify(novo_modelo.to_dict()), 201


@app.route('/modelos', methods=['GET'])
def listar_modelos():
  modelos = Modelo.query.all()
  # Retorna uma lista com [{"id": 1, "nome_modelo": "Uno", "marca": "Fiat"}, ...]
  return jsonify([m.to_dict() for m in modelos]), 200



@app.route('/modelos/marca/<string:nome_marca>', methods=['GET'])
def listar_modelos_por_marca(nome_marca):
  # Busca a marca ignorando maiúsculas/minúsculas
  marca = Marca.query.filter(Marca.nome_marca.ilike(nome_marca)).first()
    
  if not marca:
    return jsonify({"erro": f"Marca '{nome_marca}' nao encontrada."}), 404
        
  # Retorna apenas os modelos atrelados a essa marca específica
  modelos = Modelo.query.filter_by(marca_id=marca.id).all()
  return jsonify([m.to_dict() for m in modelos]), 200






# --- ROTAS DO CRUD DE VEÍCULOS ---

# 1. Cadastro de Veículo (POST)
@app.route('/veiculos', methods=['POST'])
def cadastrar_veiculo():
  data = request.get_json()
    
  # Busca no banco se já existe uma marca com esse exato nome, caso não exista o carro não é valido
  nome_marca = data['nome_marca'].strip()  
  marca_existente = Marca.query.filter_by(nome_marca=nome_marca).first()
  if not marca_existente:
    return jsonify({"erro": f"A marca '{nome_marca}' nao esta cadastrada no sistema."}), 409


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
    return jsonify({"erro": "Erro ao cadastrar veiculo. Verifique os campos e chaves estrangeiras."}), 400

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
  
  return jsonify({"mensagem": f"Veiculo ID {id} removido com sucesso do estoque."}), 200

if __name__ == '__main__':
  app.run(debug=True)