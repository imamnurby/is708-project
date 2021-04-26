'''
@author: gprana

Mock client that simulates calls to server API and sends image & gesture code data
Requires server_api to be running
'''
import requests
import cv2
import base64

BASE_URL = 'http://192.168.2.45:5000'
if __name__ == '__main__':
    print("Test of target detection") 
    try:
        fname = 'scene_image_train/scene_image_01.JPG'
        url = f'{BASE_URL}/detect_target'
        scene_image_file = cv2.imread(fname)
        files = {'scene_image_file': open(fname, 'rb')}
        resp = requests.post(url, files=files, data={'command_text':''})
        print(resp.status_code)
        print(resp.text)
    except:
        print(f"Exception when calling target detection API")
        print(resp.text)
    
    print("\n\nGesture detection cycle API test calls")
    for gesture_code in [1,2]:
        try:
            url = f'{BASE_URL}/detect_gesture'
            print(f"Calling API at {url} for segment code {gesture_code}")
            resp = requests.post(url, data={'gesture_code' : f'{gesture_code}'})
            print(resp.status_code)
            print(resp.text)
        except:
            print(f"Exception when calling gesture detection API")