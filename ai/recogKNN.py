import cv2
import mediapipe as mp
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import accuracy_score
import time
from gestureLabel import gesture

# 미디어파이프 손 랜드마크 초기화
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(
    max_num_hands=2,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)

def calculate_direction(joint):
    direction = []

    # 1번에서 4번으로 가는 벡터 계산
    v_1to4 = joint[4] - joint[3]  # 1번에서 4번으로 가는 벡터
    v_1to4 = v_1to4 / np.linalg.norm(v_1to4)  # 벡터 정규화

    # 5번에서 17번으로 가는 벡터 계산
    v_5to17 = joint[17] - joint[5]  # 5번에서 17번으로 가는 벡터
    v_5to17 = v_5to17 / np.linalg.norm(v_5to17)  # 벡터 정규화

    # 방향성 계산 (-1 또는 1)
    direction.append(np.sign(np.dot(v_1to4, v_5to17)))

    return direction


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


    palm_center = joint[[2, 6, 10, 14, 18], :]
    finger_tips = joint[[4, 8, 12, 16, 20], :]  # 손가락 끝
    pv = finger_tips - palm_center
    pv = pv / np.linalg.norm(pv, axis=1)[:, np.newaxis]
    finger_angle = np.arccos(np.einsum('nt,nt->n',
                                  pv[[0, 0, 0, 0, 1, 1, 1, 2, 2, 3], :], 
                                  pv[[1, 2, 3, 4, 2, 3, 4, 3, 4, 4], :]))
    finger_angle = np.degrees(finger_angle)

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
    
    return np.array(distances)*130



# CSV 데이터 로드
data = pd.read_csv('data/gesture_25.csv', header=None)
X = data.iloc[:, :-1].values  # 특징 데이터 (관절 각도 + 거리)
y = data.iloc[:, -1].values    # 레이블 데이터(제스처)

# 데이터를 학습 및 테스트 세트로 분리
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# KNN 모델 학습 (K=5)
knn_model = KNeighborsClassifier(n_neighbors=5)
knn_model.fit(X_train, y_train)

# 테스트 정확도 출력
y_pred = knn_model.predict(X_test)
print(f'Accuracy: {accuracy_score(y_test, y_pred)}')

# 실시간 웹캠 피드로 손 제스처 인식
cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()
    if not ret:
        break
    frame = cv2.flip(frame, 1)
    # BGR에서 RGB로 변환
    img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    # 손 랜드마크 탐지
    start = time.time()
    result = hands.process(img_rgb)

    if result.multi_hand_landmarks:
        for hand_landmarks in result.multi_hand_landmarks:
            # 랜드마크 추출
            joint = np.zeros((21, 3))
            for j, lm in enumerate(hand_landmarks.landmark):
                joint[j] = [lm.x, lm.y, lm.z]

            angle = calculate_angle(joint)
            direction = calculate_direction(joint)
            dist = calculate_distances(joint)

            #features = np.hstack((angle,dist))  # 각도와 거리 데이터를 결합
            features = np.hstack((angle))
            predicted_label = knn_model.predict([features])[0]
            print("time: ",time.time()-start)

            #thumb up(3)인 경우 보정
            if predicted_label==3 and direction[0]>0:
                predicted_label=13

            # 손의 중심 위치 계산
            center_x = int((hand_landmarks.landmark[0].x + hand_landmarks.landmark[9].x) * frame.shape[1] / 2)  # 0번과 9번 랜드마크의 평균
            center_y = int((hand_landmarks.landmark[0].y + hand_landmarks.landmark[9].y) * frame.shape[0] / 2)

            # 예측된 제스처 출력
            cv2.putText(frame, gesture[predicted_label], (center_x, center_y), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

            # 랜드마크 그리기
            mp.solutions.drawing_utils.draw_landmarks(frame, hand_landmarks, mp_hands.HAND_CONNECTIONS)

    # 결과 프레임 보여주기
    cv2.imshow('Hand Gesture Recognition', frame)

    # 'q' 키를 눌러 종료
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# 자원 해제
cap.release()
cv2.destroyAllWindows()
