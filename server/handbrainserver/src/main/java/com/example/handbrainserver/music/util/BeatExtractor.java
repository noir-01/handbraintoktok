package com.example.handbrainserver.music.util;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class BeatExtractor {
    public static String extractBeat(String filePath){
        String beats = null;
        try{
            ProcessBuilder builder = new ProcessBuilder("/home/ubuntu/handbraintoktok/musicVenv/bin/python",
                    "src/main/java/com/example/handbrainserver/music/util/BeatExtract.py", filePath);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            //에러 발생 시 출력
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String errorLine = errorReader.readLine();
//            while (errorLine != null) {
//                System.err.println("Error: " + errorLine);  // 오류 내용을 확인
//                errorLine = errorReader.readLine();
//            }

            beats = reader.readLine();
            int exitCode = process.waitFor();

        }catch(Exception e){
            e.printStackTrace();
        }
        return beats;
    }
}
