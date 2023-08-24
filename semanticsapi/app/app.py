from flask import Flask, request
from embedding import Embedding

app = Flask(__name__)

embedding = Embedding()

@app.route("/bert", methods=['GET'])
def bert():
    text_query_param = request.args.get('words')

    embedding_result = embedding.determine_embedding(text_query_param)

    # Call explicitly jsonfiy because Flask cannot directly handle arrays as response
    return { 'values': embedding_result }

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8081, debug=True)