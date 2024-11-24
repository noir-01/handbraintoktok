import requests
base_url = "server"
local_base = "http://localhost:8080/"
token_server = ""
token_local = ""
# 헤더 설정
headers = {
    "Authorization": f"Bearer {token_server}"
}


"""
1. 회원가입
"""

##API 엔드포인트 URL
# url = local_base + "/register"

# # 요청 본문 데이터
# user_data = {
#     "name": "aaa",
#     "phoneNumber": "111"
# }

# # POST 요청 보내기
# response = requests.post(url, json=user_data, headers=headers)

# # 응답 확인
# if response.status_code == 200:
#     response_data = response.json()
#     # 토큰 추출
#     token = response_data.get("token")
#     if token:
#         print(f"Received Token: {token}")
#     else:
#         print("Token not found in response.")
# else:
#     print(f"Request failed with status code: {response.status_code}")

"""
2. history/rhythm/get
"""

# # 엔드포인트 URL
# url = base_url + "/history/rhythm/get/"
# try:
#     # GET 요청 보내기
#     response = requests.get(url+"1", headers=headers)

#     # 응답 처리
#     if response.status_code == 200:
#         # 성공적으로 데이터를 받음
#         data = response.json()
#         print("Rhythm Game History Data:")
#         for item in data:
#             print(item)
#     else:
#         # 실패 응답
#         print(f"Failed to get data: {response.text} (Status code: {response.status_code})")
# except requests.exceptions.RequestException as e:
#     # 요청 중 예외 발생
#     print(f"Error during request: {e}")
"""
3. random get
"""
# url = base_url + "/history/random/get"

# # Bearer 토큰 (register 요청에서 받은 토큰을 사용)

# params={
#     "gameType":"COPY",
#     "period":"DAILY"

# }
# try:
#     # GET 요청 보내기
#     response = requests.get(url, headers=headers,params=params)

#     # 응답 처리
#     if response.status_code == 200:
#         # 성공적으로 데이터를 받음
#         data = response.json()
#         print("Rhythm Game History Data:")
#         for item in data:
#             print(item)
#     else:
#         # 실패 응답
#         print(f"Failed to get data: {response.text} (Status code: {response.status_code})")
# except requests.exceptions.RequestException as e:
#     # 요청 중 예외 발생
#     print(f"Error during request: {e}")

"""
4. Friend 관계 업로드
"""

# # API 엔드포인트 URL
# url = base_url + "/friend/upload"

# contacts = [
#     "222",
#     "333",
# ]

# # POST 요청 보내기
# response = requests.post(url, json=contacts, headers=headers)

# if response.status_code == 200:
#     print("Successfully uploaded friend contacts.")
#     print(response)  # 서버에서 반환한 응답 내용 출력
# else:
#     print(f"Request failed with status code: {response.status_code}")
#     print(response.text)  # 실패 시 응답 본문을 출력

"""
5. history/rhythm/upload
"""

# API 엔드포인트 URL
url = base_url + "/history/rhythm/upload"

body = {
    "musicId":1,
    "difficulty":"EASY",
    "combo":25,
    "score":3700,
}

# POST 요청 보내기
response = requests.post(url, json=body, headers=headers)

if response.status_code == 200:
    print("Successfully uploaded friend contacts.")
    print(response)  # 서버에서 반환한 응답 내용 출력
else:
    print(f"Request failed with status code: {response.status_code}")
    print(response.text)  # 실패 시 응답 본문을 출력
