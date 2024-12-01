import websocket
import threading
import time

def on_message(ws, message):
    print(f"서버로부터 받은 메시지: {message}")

def on_error(ws, error):
    print(f"에러 발생: {error}")

def on_close(ws):
    print("연결 종료")

def on_open(ws):
    def run(*args):
        while True:
            message = input("메시지를 입력하세요: ")
            if message.lower() == "exit":
                break
            ws.send(message)
        ws.close()
        print("연결이 종료되었습니다.")

    threading.Thread(target=run).start()

if __name__ == "__main__":
    websocket_url = "wss://handbraintoktok.duckdns.org:8080/ws"
    ws = websocket.WebSocketApp(websocket_url,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)

    ws.on_open = on_open
    ws.run_forever()
