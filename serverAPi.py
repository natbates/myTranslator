from flask import Flask, request, jsonify
from libretranslate import Translator

app = Flask(__name__)
translator = Translator()

@app.route('/translate', methods=['POST'])
def translate():
    data = request.json
    q = data.get('q')
    source = data.get('source', 'auto')
    target = data.get('target', 'en')
    translated_text = translator.translate(q, source_lang=source, target_lang=target)
    return jsonify({'translation': translated_text})

if __name__ == '__main__':
    app.run(host='localhost', port=5000)
