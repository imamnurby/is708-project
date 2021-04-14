'''
@author: gprana
'''
import pandas
from pandas import DataFrame
import glob
import logging
import random

DATAFILE_DIR='segments_train'

def pick_gesture_segment(gesture_segment_code):
    """Given an integer gesture segment code, this function returns segment data corresponding to the integer.
    Argument:
    gesture_segment_code -- integer that corresponds to a segment in the dataset   
    """

    if gesture_segment_code == 1:
        logging.debug('Picking random segment for Nodding')
        # Nodding
        segment_file_list = list(glob.glob(f'{DATAFILE_DIR}/*Nodding.csv'))
        logging.debug(f'{len(segment_file_list)} candidate segments found for Nodding.')
        target_file = random.choice(segment_file_list)
        logging.debug(f'Selecting {target_file}')
        df = pandas.read_csv(target_file)
        
    elif gesture_segment_code == 2:
        logging.debug('Picking random segment for Shaking')
        # Shaking
        segment_file_list = list(glob.glob(f'{DATAFILE_DIR}/*Shaking.csv'))
        logging.debug(f'{len(segment_file_list)} candidate segments found for Shaking.')
        target_file = random.choice(segment_file_list)
        logging.debug(f'Selecting {target_file}')
        df = pandas.read_csv(target_file)
		
    else:
        logging.debug('Picking random segment for NULL')
        # NULL / Unknown
        segment_file_list = list(glob.glob(f'{DATAFILE_DIR}/*Null.csv'))
        logging.debug(f'{len(segment_file_list)} candidate segments found for NULL.')
        target_file = random.choice(segment_file_list)
        logging.debug(f'Selecting {target_file}')
        df = pandas.read_csv(target_file)
        
    return df, str(target_file)
    
if __name__ == '__main__':
    logging.basicConfig(filename='gesture_segment_picker.log', level=logging.DEBUG)
    
    print("Testing nodding gesture selection")
    for i in range(0,3):
        df, fname = pick_gesture_segment(1)
        print(f"Run {i+1}, filename: {fname}")
        
    print("Testing shaking gesture selection")
    for i in range(0,3):
        df, fname = pick_gesture_segment(2)
        print(f"Run {i+1}, filename: {fname}")
		
    print("Testing NULL gesture selection")
    for i in range(0,3):
        df, fname = pick_gesture_segment(3)
        print(f"Run {i+1}, filename: {fname}")