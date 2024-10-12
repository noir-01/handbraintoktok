import numpy as np
import pandas as pd
import os

# 제스처 레이블 딕셔너리
gesture = {
    0: 'middle_finger',
    1: 'heart',
    2: 'heart_twohands',
    3: 'thumb_up',
    4: 'v',
    5: 'ok',
    6: 'call',
    7: 'alien',
    8: 'baby',
    9: 'four',
    10: 'mandoo',
    11: 'one',
    12: 'rabbit',
    13: 'rock',
    14: 'three',
    15: 'two'
}

# 제스처 레이블 설정
gesture_labels = [i for i in range(len(gesture))]

# 제스처 데이터 CSV 파일 경로
file_path = 'data/gesture_25_dist_15.csv'
dim = 40
dim+=1
if os.path.isfile(file_path):
    os.remove(file_path)
else:
    print(f"{file_path} does not exist.")

# 기존 제스처 데이터를 로드하거나 새로 초기화
try:
    file = np.genfromtxt(file_path, delimiter=',')
    if file.size == 0:
        file = np.empty((0, dim))
    elif file.ndim == 1:
        file = file.reshape(1, -1)  # 한 줄 데이터 처리
except OSError:
    file = np.empty((0, dim))

# CSV 파일에서 점의 좌표와 레이블을 얻는 함수
def load_data_from_csv(file_path):
    data = pd.read_csv(file_path)
    return data.values  # numpy array로 반환

import numpy as np

def calculate_angle(joint):
    # 관절 사이 벡터 계산 (부모-자식 관계)
    v1 = joint[[0, 1, 2, 3, 0, 5, 6, 7, 0, 9, 10, 11, 0, 13, 14, 15, 0, 17, 18, 19], :]  
    v2 = joint[[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20], :]  
    v = v2 - v1  
    v = v / np.linalg.norm(v, axis=1)[:, np.newaxis]  # 벡터 정규화

    # 각도 계산
    angle = np.arccos(np.einsum('nt,nt->n',
                                  v[[0, 1, 2, 4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18], :], 
                                  v[[1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 19], :]))  
    angle = np.degrees(angle)  # 각도를 도 단위로 변환


    palm_center = joint[0, :]  # 손바닥 중심
    finger_tips = joint[[4, 8, 12, 16, 20], :]  # 손가락 끝
    pv = finger_tips - palm_center
    pv = pv / np.linalg.norm(pv, axis=1)[:, np.newaxis]
    finger_angle = np.arccos(np.einsum('nt,nt->n',
                                  pv[[0, 0, 0, 0, 1, 1, 1, 2, 2, 3], :], 
                                  pv[[1, 2, 3, 4, 2, 3, 4, 3, 4, 4], :]))
    finger_angle = np.degrees(finger_angle)
    # 두 배열을 결합
    #return angle
    return np.concatenate((angle, finger_angle))

def calculate_distances(joint):
    distances = []
    
    # 1. 손바닥 중심과 각 손가락 끝 사이의 거리
    palm_center = joint[0]
    finger_tips = joint[[4, 8, 12, 16, 20]]
    palm_to_tip_distances = np.linalg.norm(finger_tips - palm_center, axis=1)
    distances.extend(palm_to_tip_distances)
    
    # 2. 인접한 손가락 끝 사이의 거리
    for i in range(5):
        for j in range(i+1,5):
            dist = np.linalg.norm(finger_tips[i] - finger_tips[j])
            distances.append(dist)
    
    return np.array(distances)*200


# 각 제스처별로 처리
def process_gesture(gesture_label, data):
    gesture_data = []
    for row in data:
        joint = row[:-1].reshape(21, 3)  # 마지막 열을 레이블로 간주하고 나머지를 관절 좌표로 변환
        angles = calculate_angle(joint)  # 각도 계산
        distances = calculate_distances(joint)
        gesture_data.append(np.hstack([angles,distances, gesture_label]))
        #gesture_data.append(np.hstack([angles, gesture_label]))

    return np.array(gesture_data)

if __name__ == "__main__":
    all_data = []

    # CSV에서 점의 좌표와 레이블을 로드
    data = load_data_from_csv('data/coord.csv')  # 좌표와 레이블이 포함된 CSV 파일 경로

    # 각 제스처에 대해 데이터 처리 (Sequentially)
    for gesture_label in gesture_labels:
        gesture_specific_data = data[data[:, -1] == gesture_label]  # 현재 제스처에 해당하는 데이터 필터링
        processed_data = process_gesture(gesture_label, gesture_specific_data)  # 각도 계산
        if processed_data.size > 0:
            all_data.append(processed_data)

    # 모든 데이터를 결합하여 CSV 파일에 저장
    if all_data:
        all_data = np.vstack(all_data)
        file = np.vstack((file, all_data))
        np.savetxt(file_path, file, delimiter=',')

    print(f"모든 제스처 데이터가 {file_path}에 저장되었습니다.")
