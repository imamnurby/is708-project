'''
@author: gprana
'''
import cv2
import numpy as np
import time

# Returns line of pointing direction
def resolve_pointing_direction(scene_image):
    """Given scene image containing a pointing right hand, this function returns the line of pointing direction
    Argument:
    scene_image -- binary data of scene image containing pointing hand
    Return value:
    List containing coordinates representing a line corresponding to pointing direction 
    of the hand in the scene image
    """

    '''
    TODO: You should implement code to get proper value of gradient and intercept 
    to replace assignments of mock values below.
    '''
    
    img = np.copy(scene_image)

    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img_hsv = cv2.cvtColor(img_rgb, cv2.COLOR_RGB2HSV)

    # extract hand by extracting the hand countrour (picking the largest countour area)
    upper = np.array([0, 0.35*255, 0])
    bottom = np.array([15, 0.75*255, 255])
    mask = cv2.inRange(img_hsv, upper, bottom)
    masked = cv2.bitwise_and(img_rgb, img_rgb, mask=mask)
    blur = cv2.GaussianBlur(masked, (7, 7), 0)
    ret,thresh = cv2.threshold(blur,64,255,cv2.THRESH_BINARY)
    thresh = cv2.cvtColor(thresh, cv2.COLOR_BGR2GRAY)
    contours, hierarchy = cv2.findContours (thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
    max_cnt = max (contours, key = lambda x: cv2.contourArea (x))
    cv2.drawContours(img, max_cnt, -1, (255,255,0), 2)

    # identify centroid of the countour
    M = cv2.moments(max_cnt)
    cX = int(M["m10"] / M["m00"])
    cY = int(M["m01"] / M["m00"])
    cv2.circle(img, (int(cX), int(cY)), 3, (0, 255, 255), thickness=-1, lineType=cv2.FILLED)
    cv2.putText(img, "centroid", (int(cX), int(cY)), cv2.FONT_HERSHEY_SIMPLEX, .8, (0, 0, 255), 2, lineType=cv2.LINE_AA)

    # identify fingertip
    extTop = tuple(max_cnt[max_cnt[:, :, 1].argmin()][0])
    cv2.circle(img, extTop, 3, (255, 0, 0), -1, lineType=cv2.FILLED) 
    cv2.putText(img, "fingertip", extTop, cv2.FONT_HERSHEY_SIMPLEX, .8, (0, 0, 255), 2, lineType=cv2.LINE_AA)

    # draw line between centroid and fingertip
    cv2.line(img, extTop, (cX, cY), 1, 3)
    
    # plt.figure(figsize=(10,10))
    # plt.imshow(img)
    # plt.show()
    cv2.imwrite("result_pointing_resolver.jpg",img)
    
    # calculate gradient and slope
    x2, y2 = extTop
    x1, y1 = (cX, cY)
    gradient=(y2-y1)/(x2-x1)
    intercept= y2 - gradient*(x2)

    return [gradient, intercept]

    
if __name__ == '__main__':
    # Code to allow test run of this component by running 'python pointing_resolver.py'
    for fname in ['scene_image_train/scene_image_01.JPG',
                  'scene_image_train/scene_image_02.JPG',
                  'scene_image_train/scene_image_03.JPG']:   
        try:
            frame = cv2.imread(fname)
            output = resolve_pointing_direction(frame)  
            print(f"{fname} : {str(output)}")
        except Exception as e:
            print(e)
            print(f"Unable to detect pointing direction for {fname}")
