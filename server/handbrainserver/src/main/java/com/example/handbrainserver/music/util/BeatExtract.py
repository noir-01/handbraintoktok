import librosa
import json
import sys
# MP3 파일 로드
# filename is given by ARGS
filename = sys.argv[1:][0]
y, sr = librosa.load(filename)
# 비트 트래킹 수행
bps, beat_frames = librosa.beat.beat_track(y=y, sr=sr)

# 비트가 몇 초에 있는지 위치한 리스트 생성
beat_times = librosa.frames_to_time(beat_frames, sr=sr)
print(json.dumps([float(num) for num in beat_times]))