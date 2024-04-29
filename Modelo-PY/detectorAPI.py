from flask import Flask, request, jsonify
import pickle
import face_recognition
import numpy as np
from collections import Counter
from pathlib import Path

app = Flask(__name__)

# Load or initialize face encodings
ENCODINGS_PATH = "output/encodings.pkl"
with open(ENCODINGS_PATH, "rb") as f:
    loaded_encodings = pickle.load(f)

@app.route("/recognize", methods=["POST"])
def recognize_faces():
    # Get input image from request
    image_file = request.files["image"]
    input_image = face_recognition.load_image_file(image_file)

    # Get face locations and encodings
    input_face_locations = face_recognition.face_locations(input_image)
    input_face_encodings = face_recognition.face_encodings(input_image, input_face_locations)

    # Perform face recognition
    recognized_names = []
    for unknown_encoding in input_face_encodings:
        name = _recognize_face(unknown_encoding)
        if not name:
            name = "Unknown"
        recognized_names.append(name)

    return jsonify({"names": recognized_names})

@app.route("/train", methods=["POST"])
def train_model():
    # Get input image and name from request
    image_file = request.files["image"]
    name = request.form.get("name")

    # Load image and encode face using HOG method
    input_image = face_recognition.load_image_file(image_file)
    face_locations = face_recognition.face_locations(input_image, model="hog")
    face_encodings = face_recognition.face_encodings(input_image, face_locations)

    # Update loaded encodings with new face
    num_new_encodings = 0
    if face_encodings:
        num_new_encodings = len(face_encodings)
        loaded_encodings["encodings"].extend(face_encodings)
        loaded_encodings["names"].extend([name] * len(face_encodings))
        with open(ENCODINGS_PATH, "wb") as f:
            pickle.dump(loaded_encodings, f)

    return jsonify({
        "message": f"{num_new_encodings} new encoding(s) added for {name}. Model trained successfully."
    })

def _recognize_face(unknown_encoding):
    boolean_matches = face_recognition.compare_faces(loaded_encodings["encodings"], unknown_encoding)
    votes = Counter(name for match, name in zip(boolean_matches, loaded_encodings["names"]) if match)
    if votes:
        return votes.most_common(1)[0][0]

if __name__ == "__main__":
    app.run(debug=True)
