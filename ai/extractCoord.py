import cv2
import mediapipe as mp
import numpy as np
import os
from gestureLabel import gesture

cv2.ocl.setUseOpenCL(True)

# Settings for MediaPipe hands
max_num_hands = 2  # Number of hands to detect

# Gesture labels
gesture_labels = list(gesture.keys())

# MediaPipe hands model initialization
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(
    max_num_hands=max_num_hands,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)

# Gesture data CSV file path
file_path = 'data/coord.csv'

# Function to save joint data to CSV
def save_joint_data(joint, label):
    joint_flattened = joint.flatten()  # Flatten joint array
    data_to_save = np.append(joint_flattened, label)  # Append label to joint data

    # Append data to CSV file
    with open(file_path, 'a') as f:
        np.savetxt(f, [data_to_save], delimiter=',', fmt='%.15f')  # Use suitable format for floating points

# Dataset path for images
dataset_path = "image/"

# Function to process image in parallel
def process_image(args):
    gesture_label, image_file = args
    img_path = os.path.join(dataset_path, gesture[gesture_label], image_file)
    img = cv2.imread(img_path)

    if img is None:
        print(f"Could not load image '{image_file}'.")
        return None
    
    img_rotated = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    # Hand landmark detection
    result = hands.process(img_rotated)

    if result.multi_hand_landmarks is not None:
        for res in result.multi_hand_landmarks:
            joint = np.zeros((21, 3))
            for j, lm in enumerate(res.landmark):
                joint[j] = [lm.x, lm.y, lm.z]
            
            # Save joint data and corresponding label
            save_joint_data(joint, gesture_label)

# Main function to run multiprocessing
def main():
    # Create a list of all image files and their corresponding gesture labels
    for gesture_label in gesture_labels:
        gesture_folder = gesture[gesture_label]
        images = os.listdir(os.path.join(dataset_path, gesture_folder))
        for image_file in images:
            process_image((gesture_label, image_file))

if __name__ == "__main__":
    main()
