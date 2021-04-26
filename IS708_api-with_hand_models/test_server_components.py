'''
@author: gprana
'''
import gesture_segment_picker
import gesture_classifier
import object_detector
import pointing_resolver
import target_resolver
import cv2
import json
import glob
import pandas

if __name__ == '__main__':
    print("Pointing resolution API test calls")
    output_target_list = []
    for idx,fname in enumerate(['scene_image_train/scene_image_01.JPG',
              'scene_image_train/scene_image_02.JPG',
              'scene_image_train/scene_image_03.JPG']):   
        try:
            scene_image = cv2.imread(fname)
            detected_objects = object_detector.detect_objects(scene_image)
            print(f"{fname} - detected objects: {str(detected_objects)}")
        except:
            print(f"Unable to detect objects in {fname}")
            
        try:
            pointing_direction = pointing_resolver.resolve_pointing_direction(scene_image)
            print(f"{fname} - Pointing direction : {str(pointing_direction)}")
        except:
            print(f"Unable to detect pointing direction in {fname}")
    
        try:
            target_object_bounding_box = target_resolver.resolve_target(scene_image, detected_objects, pointing_direction)
            scene_image = cv2.rectangle(scene_image,(target_object_bounding_box[1][0],target_object_bounding_box[1][1]),(target_object_bounding_box[1][2],target_object_bounding_box[1][3]),(0,255,0),3)
#             cv2.imwrite("out/tmp_image_"+str(idx+1)+".jpg",scene_image)
            print(f"{fname} - Target object bounding box: {str(target_object_bounding_box)}")
            print("\n")
            output_target_list.append([fname, str(target_object_bounding_box)])
        except:
            print(f"Unable to detect target object in {fname}")
    
    print("\n\nGesture detection cycle API test calls")
    print("Nodding")
    segment_code = 1
    for i in range(0,3):
        gesture_segment, fname = gesture_segment_picker.pick_gesture_segment(segment_code)
        gesture_type = gesture_classifier.predict(gesture_segment)
        print(f"Run 1. Segment file: {fname}. Predicted gesture: {gesture_type}")
    
    print("Shaking")
    segment_code = 2
    for i in range(0,3):
        gesture_segment, fname = gesture_segment_picker.pick_gesture_segment(segment_code)
        gesture_type = gesture_classifier.predict(gesture_segment)
        print(f"Run 2. Segment file: {fname}. Predicted gesture: {gesture_type}")
		
    print("NULL")
    segment_code = 0
    for i in range(0,3):
        gesture_segment, fname = gesture_segment_picker.pick_gesture_segment(segment_code)
        gesture_type = gesture_classifier.predict(gesture_segment)
        print(f"Run 2. Segment file: {fname}. Predicted gesture: {gesture_type}")