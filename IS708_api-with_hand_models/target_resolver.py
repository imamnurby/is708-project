import math
import spacy
nlp = spacy.load("en_core_web_sm")

def resolve_target(scene_image, detected_objects, pointing_direction, command_text=None):
    """TODO: You should implement this function and replace dummy value assignment below.
    """
    print(f"detected objects are: {detected_objects}")
    # get centroid of each detected objects

    centroid_l = []
    for obj_name, coordinate in detected_objects:
        x = (coordinate[0]+coordinate[2])/2
        y = (coordinate[1]+coordinate[3])/2
        centroid = (int(x),int(y))
        centroid_l.append((obj_name, centroid, coordinate))

    # calculate distance between centroid point and the line equation
    # ax + by + c = 0
    # y = mx + b
    # -mx + y - c = 0
    # x1, y1

    gradient = pointing_direction[0]
    intercept = pointing_direction[1]
    
    distance_l = []
    for obj_name, centroid, coordinate in centroid_l:
        x1 = centroid[0]
        y1 = centroid[1]
        d = abs((-gradient * x1 + 1 * y1 - intercept)) / (math.sqrt((-gradient) * (-gradient) + 1 * 1))    
        distance_l.append((obj_name, d, coordinate))
    
    distance_l = sorted(distance_l, key=lambda tup: tup[1])
    
    obj_l = []
    # extract wordlist
    with open('predefined_dict.txt') as f:
        lines = f.readlines()
        wordlist = [word.strip("\n") for word in lines]
        # print(wordlist)
    # process command text
    if command_text != None:
        if command_text in wordlist:
            obj_l.append(command_text)
        else:
            doc = nlp(command_text)
            obj_l = [token.text for token in doc  if token.pos_ == "NOUN"]
        temp_l = []
        print(f"object list contains {obj_l}")
        
        # check if there is an object that match the extracted obj from command text
        for obj_name, dist, coordinate in (distance_l):
            obj_name_l = obj_name.split()
            for token in obj_name_l: 
                if token in obj_l:
                    temp_l.append((obj_name, dist, coordinate))
                    break
        
        # if somehow the command input are invalid, just return object with the least distance
        if len(temp_l) == 0:
            obj_out = min(distance_l, key=lambda tup: tup[1])
        
        # if there are multiple object that match with the command input, choose the object with the least distance
        elif len(temp_l) != 1: 
            obj_out = min(temp_l, key=lambda tup: tup[1])

        # command input valid and only one object match with the command input
        else:
            obj_out = (temp_l[0])

    else:   
        obj_out = min(distance_l, key=lambda tup: tup[1])
    
    obj_out = (obj_out[0], obj_out[-1])
    target = [obj_out[0],obj_out[-1]]

    return target
	
if __name__ == '__main__':
    # Code to allow test run of this component by running 'python target_resolver.py'
    target = resolve_target(None,[("person",[0,0,100,200]),("person",[0,0,100,100])],[1,0])
    print(target)
