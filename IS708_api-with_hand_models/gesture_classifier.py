import pickle
import json
import tsfel
import os

PATH = os.getcwd() + '/classifier_model/'
GESTURE_MODEL_FILENAME = 'gesture_clf'
SCALER_FILENAME = 'scaler'
CONFIG = 'config.json'

GESTURE_MODEL_PATHNAME = PATH + GESTURE_MODEL_FILENAME
SCALER_PATHNAME = PATH + SCALER_FILENAME
CONFIG_PATHNAME = PATH + CONFIG

with open(CONFIG_PATHNAME, 'r') as j:
    cfg_file = json.loads(j.read())

classifier = pickle.load(open(GESTURE_MODEL_PATHNAME, 'rb'))
scaler = pickle.load(open(SCALER_PATHNAME, 'rb'))


def predict(raw_segment_dataframe):

    # preprocess the signal: drop useless column, then pad the signal
    signal_segment = raw_segment_dataframe.copy()
    signal_segment = signal_segment.drop(["Timestamp", "A_X", "A_Y", "A_Z", "Gesture"], axis=1)

    if signal_segment.shape[0] > 50:
        signal_segment = signal_segment.iloc[:50,:]
    else: 
        signal_segment = signal_segment.reindex(range(50), fill_value=0)
    
    # extract features
    fs=25
    window_size=50
    features = tsfel.time_series_features_extractor(cfg_file, signal_segment, fs=fs, window_size=window_size)
    features = scaler.transform(features)

    # predict gesture
    classifier_output = classifier.predict(features)

    if classifier_output == 0:
        gesture = "Null"
    elif classifier_output == 1:
        gesture = "Nodding"
    elif classifier_output == 2:
        gesture = "Shaking"

    return gesture

if __name__ == '__main__':
	pass