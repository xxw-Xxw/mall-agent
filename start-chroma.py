"""ChromaDB 服务启动器（供 start-all.bat 调用）"""
import os
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
sys.argv = [
    'chroma', 'run',
    '--path', os.path.join(ROOT, 'chroma-data'),
    '--host', os.environ.get('CHROMA_HOST', '0.0.0.0'),
    '--port', os.environ.get('CHROMA_PORT', '8000'),
]
from chromadb.cli.cli import app
app()
