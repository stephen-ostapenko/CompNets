#!/usr/bin/python3
from flask import Flask, jsonify, abort, make_response, request

app = Flask(__name__)

items = [
    {
        "id": 0,
        "name": "Apple",
        "description": "apple"
    },
    {
        "id": 1,
        "name": "Banana",
        "description": "banana"
    }
]

def compose_item(id, name, description):
    return {
        "id": id,
        "name": name,
        "description": description
    }        

@app.route("/api/items", methods = ["GET"])
def get_items():
    return jsonify({"items": items})

@app.route("/api/items/<int:item_id>", methods = ["GET"])
def get_item(item_id):
    item = list(filter(lambda it: it["id"] == item_id, items))
    
    if (len(item) == 0):
        abort(404)
    
    if (len(item) > 1):
        abort(500)

    return jsonify({"item": item[0]})

@app.route("/api/items", methods = ["POST"])
def create_item():
    if (not request.json or "name" not in request.json):
        abort(400)

    id = max(items, key = lambda it: it["id"])["id"] + 1
    item = compose_item(id, request.json["name"], request.json.get("description", ""))
    items.append(item)
    
    return jsonify({"item": item}), 201

@app.route("/api/items/<int:item_id>", methods = ["PUT"])
def update_item(item_id):
    item = list(filter(lambda it: it["id"] == item_id, items))
    
    if (len(item) == 0):
        abort(404)
    
    if (len(item) > 1):
        abort(500)

    if (not request.json):
        abort(400)
    
    item = item[0]
    item["name"] = request.json.get("name", item["name"])
    item["description"] = request.json.get("description", item["description"])
    
    return jsonify({"item": item})

@app.route("/api/items/<int:item_id>", methods = ["DELETE"])
def delete_item(item_id):
    item = list(filter(lambda it: it["id"] == item_id, items))
    
    if (len(item) == 0):
        abort(404)
    
    if (len(item) > 1):
        abort(500)

    items.remove(item[0])
    
    return jsonify({"result": True})

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({"error": "Not found"}), 404)

@app.errorhandler(500)
def unknown_error(error):
    return make_response(jsonify({"error": "Unknown error"}), 500)

if (__name__ == "__main__"):
    app.run()
