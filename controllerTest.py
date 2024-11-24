"""
1. 회원가입
"""
# import requests

# # 엔드포인트 URL
# url = "http://localhost:8080/register"

# # 요청에 포함할 데이터
# payload = {"name": "test2"}

# try:
#     # POST 요청 보내기
#     response = requests.post(url, data=payload)

#     # 응답 처리
#     if response.status_code == 200:
#         # 성공적으로 토큰 받음
#         token = response.json().get("token")
#         print(f"Token received: {token}")
#     else:
#         # 실패 응답
#         print(f"Failed to register: {response.text} (Status code: {response.status_code})")
# except requests.exceptions.RequestException as e:
#     # 요청 중 예외 발생
#     print(f"Error during request: {e}")

"""
2. history/rhythm/get 
"""
# import requests

# # 엔드포인트 URL
# url = "http://localhost:8080/history/rhythm/get/"

# # Bearer 토큰 (register 요청에서 받은 토큰을 사용)
# token = ""

# # 헤더 설정
# headers = {
#     "Authorization": f"Bearer {token}"
# }

# try:
#     # GET 요청 보내기
#     response = requests.get(url+"9", headers=headers)

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
import requests
# 엔드포인트 URL
url = "http://localhost/history/random/get"

# Bearer 토큰 (register 요청에서 받은 토큰을 사용)
token = ""

# 헤더 설정
headers = {
    "Authorization": f"Bearer {token}"
}
params={
    "gameType":"COPY",
    "period":"DAILY"

}
try:
    # GET 요청 보내기
    response = requests.get(url, headers=headers,params=params)

    # 응답 처리
    if response.status_code == 200:
        # 성공적으로 데이터를 받음
        data = response.json()
        print("Rhythm Game History Data:")
        for item in data:
            print(item)
    else:
        # 실패 응답
        print(f"Failed to get data: {response.text} (Status code: {response.status_code})")
except requests.exceptions.RequestException as e:
    # 요청 중 예외 발생
    print(f"Error during request: {e}")