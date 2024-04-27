import pickle
from collections import Counter
from pathlib import Path

import face_recognition
import cv2
import numpy as np
from sklearn.neighbors import KNeighborsClassifier

DEFAULT_ENCODINGS_PATH = Path("output/encodings.pkl")
BOUNDING_BOX_COLOR = (255, 0, 0)  # Blue color in BGR format
TEXT_COLOR = (255, 255, 255)  # White color in BGR format

# Load face encodings
with DEFAULT_ENCODINGS_PATH.open(mode="rb") as f:
    loaded_encodings = pickle.load(f)

# Load KNN classifier
with open("output/knn_classifier.pkl", "rb") as f:
    knn_classifier = pickle.load(f)

def recognize_faces(frame, model="hog"):
    """
    Given a frame from the webcam, get the locations and encodings of any faces and
    compares them against the known encodings to find potential matches.
    """
    input_face_locations = face_recognition.face_locations(frame, model=model)
    input_face_encodings = face_recognition.face_encodings(frame, input_face_locations)

    for bounding_box, unknown_encoding in zip(input_face_locations, input_face_encodings):
        name = _recognize_face(unknown_encoding)
        if not name:
            name = "Unknown"
        _display_face(frame, bounding_box, name)

    cv2.imshow("Webcam", frame)

def _recognize_face(unknown_encoding):
    """
    Given an unknown encoding, predict the name of the face using KNN classifier.
    """
    name = knn_classifier.predict([unknown_encoding])
    return name[0] if name else None

def _display_face(frame, bounding_box, name):
    """
    Draws bounding boxes around faces, a caption area, and text captions.
    """
    top, right, bottom, left = bounding_box
    # Adjust the width and height of the bounding box
    width = right - left
    height = bottom - top
    # Increase the width and height by 20 pixels on each side
    left = max(0, left - 20)
    top = max(0, top - 20)
    right = min(frame.shape[1], right + 20)
    bottom = min(frame.shape[0], bottom + 20)
    cv2.rectangle(frame, (left, top), (right, bottom), BOUNDING_BOX_COLOR, 2)
    cv2.rectangle(frame, (left, bottom - 35), (right, bottom), BOUNDING_BOX_COLOR, cv2.FILLED)
    font = cv2.FONT_HERSHEY_DUPLEX
    cv2.putText(frame, name, (left + 6, bottom - 6), font, 0.5, TEXT_COLOR, 1)

def real_time_recognition(model="hog"):
    """
    Runs real-time face recognition on video stream from webcam.
    """
    video_capture = cv2.VideoCapture(0)
    while True:
        ret, frame = video_capture.read()
        if not ret:
            break
        recognize_faces(frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):  # Press 'q' to quit
            break

    video_capture.release()
    cv2.destroyAllWindows()

# Start real-time face recognition
real_time_recognition()
