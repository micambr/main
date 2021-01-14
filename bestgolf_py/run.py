import numpy as np
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.keras import layers
import os
import flask as f
import sys

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"  # Force TF to use only the CPU

app = f.Flask(__name__)

package_dir = os.path.dirname(os.path.abspath(__file__))
print(package_dir)
model_file = os.path.join(package_dir,'data')
print(model_file)
model = keras.models.load_model(model_file, compile = True)

@app.route('/heart')
def heart():
    return 'beating'

@app.route('/predict', methods = ['POST'])
def predict():
    try:
        input = f.request.form['input']
        #return str(type(input))
        array = input.split(',')
        #return str(len(array))
        if len(array) != 12000:
            f.abort(501)
        return str(predict_int(array))
    except:
        return str("Unexpected error:" + sys.exc_info()[0])   
    
    
def predict_int(array):
    
    def getletter(predictions):
        weights = []
        max = -1.0
        maxx = -1

        for x in  range(len(predictions)):
            w =  predictions[x]
            item = [chr(ord('A') + x),w]
            if w > max:
                maxx =x
                max = w
            #print(item)
            #print(item, max)
            weights.append(item)
        
        #print(weights)
    
        return weights[maxx]
    
    npa = np.array(list(map(float, array)))
    npa = np.array([npa])
    
    predictions = model.predict(npa)


    result = getletter(predictions[0][0:26]), getletter(predictions[0][26:52]);

    #print(result)
    
    return str(result[0][0] + result[1][0])

    
    
thefile = os.path.join(package_dir,'data/forDLt/IODataT.txt')

myt_data = np.genfromtxt(thefile, delimiter=',', dtype=float)

testInput = myt_data[0][0:12000]


print(predict_int(testInput))

#print(np.shape(testInput))


   
print(predict_int(np.full(12000, 0)))
#print('Done')