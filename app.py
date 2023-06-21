from flask import Flask, jsonify, render_template, request
import tensorflow.keras as keras
from tensorflow.keras.applications import EfficientNetB0
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.losses import *
from tensorflow.keras.layers import Dropout, Flatten, Dense, Input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import Model
from tensorflow.keras.callbacks import *
import numpy as np
from PIL import Image
import json
import cv2
import base64
app = Flask(__name__)


baseModel = EfficientNetB0(weights='imagenet', include_top=False,input_tensor= Input(shape=(264, 264, 3)))
headModel = baseModel.output
headModel = Flatten(name="flatten")(headModel)
headModel = Dense(32, activation="relu")(headModel)
headModel = Dropout(0.5)(headModel)
headModel = Dense(5, activation="softmax")(headModel)
model = Model(inputs=baseModel.input, outputs=headModel)
optimizer = Adam(learning_rate=1e-4, beta_1=0.9, beta_2=0.999, clipnorm=1.0)
model.load_weights("./model_v2.h5")
model.compile(loss="categorical_crossentropy", optimizer=optimizer,metrics=["accuracy"])
print('------Model Loaded------')


def preprocess_image(image, target_size):
    if image.mode != 'RGB':
        image = image.convert('RGB')
    image = image.resize(target_size)
    image = img_to_array(image)
    image = np.expand_dims(image, axis=0)
    return image


# img = Image.open("./image.jpeg")
# processed_image = preprocess_image(img, target_size=(264, 264))
# print('Shape: ', processed_image.shape)
# prediction = model.predict(processed_image)
# prediction = np.squeeze(prediction[0])
# # lists = prediction.tolist()
# ind = np.argmax(prediction)
# print(ind)

labels = {
 0 : "No DR",
 1 : "Mild",
 2 : "Moderate",
 3 : "Severe",
 4 : "Proliferative DR"
}

# img = Image.open("./image.jpeg")
# img = img.resize((264,264))
# img =np.array(img)
#
# img = img.reshape((1,264,264,3))
#
# #processed_image = preprocess_image(img, target_size=(264, 264))
# #print('Shape: ', processed_image.shape)
# #prediction = model.predict(processed_image)
# prediction = model.predict(img)
# #prediction = np.squeeze(prediction[0])
# ind = np.argmax(prediction,axis=1)
# print(ind)
# output = labels[ind[0]]
# print(output)

@app.route("/")
def index():
    return "<h1 style='color: red;'>Hack and Cheese!</h1>"


@app.route("/predict", methods=['POST', 'GET'])
def predict():
    message = request.get_json(force=True)
    print(type(message))
    encoded = message['data']
    decoded = base64.b64decode(encoded)
    with open("./image.jpeg", 'wb') as wfile:
        wfile.write(decoded)
    img = Image.open("./image.jpeg")
    img = img.resize((264,264))
    img =np.array(img)
    img = img.reshape((1,264,264,3))
    prediction = model.predict(img)
    ind = np.argmax(prediction,axis=1)
    output = labels[ind[0]]
    response = {
    'prediction': output
    }
    return jsonify(response)
