from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class Marca(db.Model):
  __tablename__ = 'marcas'
  
  id = db.Column(db.Integer, primary_key=True)
  
  #Com unique=True não precisamos verificar entrada na rota
  nome_marca = db.Column(db.String(50), nullable=False, unique=True) 
  
  # Relacionamento para facilitar a busca de modelos a partir da marca
  modelos = db.relationship('Modelo', backref='marca', lazy=True)
  
  def to_dict(self):
    return {"id": self.id, "nome_marca": self.nome_marca}


#modelo é atrelado com Marca, porem a verificação se a marca existe é feita pela rota
class Modelo(db.Model):
  __tablename__ = 'modelos'
    
  id = db.Column(db.Integer, primary_key=True)
  nome_modelo = db.Column(db.String(50), nullable=False)
  marca_id = db.Column(db.Integer, db.ForeignKey('marcas.id'), nullable=False)
    
  # Relacionamento para facilitar a busca de veículos a partir do modelo
  veiculos = db.relationship('Veiculo', backref='modelo', lazy=True)

  def to_dict(self):
    return {
      "id": self.id, 
      "nome_modelo": self.nome_modelo, 
      "marca": self.marca.nome_marca if self.marca else None
    }


class Veiculo(db.Model):
  __tablename__ = 'veiculos'
    
  id = db.Column(db.Integer, primary_key=True)
  modelo_id = db.Column(db.Integer, db.ForeignKey('modelos.id'), nullable=False)
  ano_fabricacao = db.Column(db.Integer, nullable=False)
  cor = db.Column(db.String(30), nullable=False)
  preco = db.Column(db.Float, nullable=False)
  quilometragem = db.Column(db.Integer, nullable=False)
  status_disponibilidade = db.Column(db.String(30), nullable=False, default="Disponível")

  def to_dict(self):
    return {
      "id": self.id,
      "marca": self.modelo.marca.nome_marca if self.modelo else None,
      "modelo": self.modelo.nome_modelo if self.modelo else None,
      "ano_fabricacao": self.ano_fabricacao,
      "cor": self.cor,
      "preco": self.preco,
      "quilometragem": self.quilometragem,
      "status_disponibilidade": self.status_disponibilidade
    }