from flask import request, jsonify
from app import app
from transformers import pipeline, AutoModelForTokenClassification, AutoTokenizer

tokenizer = AutoTokenizer.from_pretrained("dbmdz/bert-large-cased-finetuned-conll03-english")
model = AutoModelForTokenClassification.from_pretrained("dbmdz/bert-large-cased-finetuned-conll03-english")
nlp_ner = pipeline("ner", model=model, tokenizer=tokenizer)

# route for processing text
@app.route('/process', methods=['POST'])
def process_text():
    text = request.json.get('text', '')
    result = nlp_ner(text)
    return jsonify(result)
