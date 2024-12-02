let audio, context, src, analyser, canvas, ctx;
let bufferLength, dataArray, WIDTH, HEIGHT, barWidth;

function loadAudio(musicUri) {
  // 오디오 요소 초기화
  audio = document.getElementById("audio");
  //audio.src = musicUri;
  audio.load(musicUri);

  // 오디오 컨텍스트 및 분석기 설정
  context = new AudioContext();
  src = context.createMediaElementSource(audio);
  analyser = context.createAnalyser();
  
  // 캔버스 설정
  canvas = document.getElementById("canvas");
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
  ctx = canvas.getContext("2d");
  
  // 연결 및 설정
  src.connect(analyser);
  analyser.connect(context.destination);
  analyser.fftSize = 256;
  
  // 초기화 파라미터
  bufferLength = analyser.frequencyBinCount;
  dataArray = new Uint8Array(bufferLength);
  WIDTH = canvas.width;
  HEIGHT = canvas.height;
  barWidth = (WIDTH / bufferLength) * 2.5;
}

function playAudio() {
  if (!audio || !analyser) {
    console.error("오디오가 로드되지 않았습니다.");
    return;
  }
  
  audio.play();
  renderFrame();
}

function renderFrame() {
  requestAnimationFrame(renderFrame);
  
  let x = 0;
  analyser.getByteFrequencyData(dataArray);
  
  // 배경 지우기
  ctx.fillStyle = "#000";
  ctx.fillRect(0, 0, WIDTH, HEIGHT);
  
  // 주파수 바 그리기
  for (let i = 0; i < bufferLength; i++) {
    let barHeight = dataArray[i];
    
    // 색상 계산
    let r = barHeight + (25 * (i/bufferLength));
    let g = 250 * (i/bufferLength);
    let b = 50;
    
    ctx.fillStyle = `rgb(${r},${g},${b})`;
    ctx.fillRect(x, HEIGHT - barHeight, barWidth, barHeight);
    x += barWidth + 1;
  }
}

// 이 부분은 kotlin에서 호출될 수 있도록 export 가능
export { loadAudio, playAudio };