import numpy as np
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.keras import layers
import os

os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"   # see issue #152
os.environ["CUDA_VISIBLE_DEVICES"] = ""

package_dir = os.path.dirname(os.path.abspath(__file__))
thefile = os.path.join(package_dir,'data/forDL/IODataAll.txt')

my_data = np.genfromtxt(thefile, delimiter=',')

trainInput = my_data[:,0:12000]
trainOutput = my_data[:,12000:12052]

print(trainOutput)

input = keras.Input(shape=(12000,), name="px", dtype="float")

x = layers.Dense(400, activation="relu")(input)
x = layers.Dense(40, activation="relu")(x)
x = layers.Dense(30, activation="relu")(x)
x = layers.Dropout(0.5)(x)
output = layers.Dense(52, activation="sigmoid")(x)
model = keras.Model(input, output)
model.compile("adam", "binary_crossentropy", metrics=["accuracy"])

model.fit(trainInput, trainOutput, epochs=400)


print("Evaluating:")

thefile = os.path.join(package_dir,'data/forDLt/IODataT.txt')

myt_data = np.genfromtxt(thefile, delimiter=',', dtype=float)

testInput = myt_data[:,0:12000]
testOutput = myt_data[:,12000:12052]


print("Evaluate:", model.evaluate(testInput, testOutput))

model.save(os.path.join(package_dir,'data/model'))





def getletter(predictions):
    weights = []
    max = -1
    maxx = -1
    for x in range(len(predictions)):
        w =  predictions[x]
        item = [chr(ord('A') + x),w]
        if w > max:
            maxx =x
            max = w
        print(item)
        weights.append(item)
    
    print(weights)
    
    return weights[maxx]
    
    
#testInputx = myt_data[0:1, 0:12000]

    
#predictions = model.predict(testInputx);

#print(predictions)

#result = getletter(predictions[0][0:26]), getletter(predictions[0][26:52]);

#print(result)

