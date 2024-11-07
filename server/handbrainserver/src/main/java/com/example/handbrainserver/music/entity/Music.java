package com.example.handbrainserver.music.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String artist;
    private LocalTime duration;
    private String filePath;
    @Lob
    private String beatList;

    // Convert JSON back to List<Float> after reading from the database
    public List<Float> getBeatTime() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // List<Float>로 변환하기 위해 TypeReference를 사용
            return objectMapper.readValue(this.beatList, new TypeReference<List<Float>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
