package com.example.handbrainserver.music.util;


import com.example.handbrainserver.music.util.BeatExtractor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BeatExtractorTest {
    @Test
    public void testExtractBeat(){
        String testFilePath = "/home/ubuntu/ETA.mp3";

        // BeatExtractor 클래스의 extractBeat 메소드 호출
        String result = BeatExtractor.extractBeat(testFilePath);

        // 결과가 비어있지 않은지 확인
        assertNotNull(result);

        // 예시: 결과 리스트의 첫 번째 값이 예상한 값인지 확인 (값은 예시일 뿐, 실제 값에 맞게 수정)
        assertTrue(result.length() > 0); // 리스트가 비어있지 않음 확인
    }

}
