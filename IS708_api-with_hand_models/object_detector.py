'''
@author: gprana
'''
import cv2
import cvlib as cv
from cvlib.object_detection import draw_bbox

def detect_objects(scene_image):
    """Given scene image, this function detects objects in the scene
    
    Argument:
    scene_image -- binary data of scene image
    Return value:
    list of tuples containing object name and their bounding boxes in format of
    (obj_name, [bbox_coordinate]). E.g. ('chair',[0,0,100,100])
    """
    bbox, label, conf = cv.detect_common_objects(scene_image)
    
    detected_objects = list(zip(label, bbox))
    detected_objects = [x for x in detected_objects if x[0]!="person"]
    
    return detected_objects

if __name__ == '__main__':
    for fname in ['scene_image_train/scene_image_01.JPG',
                  'scene_image_train/scene_image_02.JPG',
                  'scene_image_train/scene_image_03.JPG',
                  'scene_image_train/scene_image_04.JPG',
                  'scene_image_train/scene_image_05.JPG',
                  'scene_image_train/scene_image_06.JPG']:   
        try:
            scene_image = cv2.imread(fname)
            objects = detect_objects(scene_image)  
            print(f"{fname} : {str(objects)}")
        except:
            #print(e)
            print(f"Unable to detect objects in {fname}")
