import os
from dotenv import load_dotenv

# Carrega as variáveis definidas no arquivo .env
load_dotenv()

class Config:
  # Captura cada variável isolada do ambiente
  db_user = os.getenv('DB_USER')
  db_password = os.getenv('DB_PASSWORD')
  db_host = os.getenv('DB_HOST')
  db_port = os.getenv('DB_PORT')
  db_name = os.getenv('DB_NAME')
    
  # Monta a string de conexão de forma segura
  SQLALCHEMY_DATABASE_URI = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"
    
  SQLALCHEMY_TRACK_MODIFICATIONS = False