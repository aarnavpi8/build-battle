package com.substring.buildbattle.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "drawings")
public class Drawing {

    @Id
    private String id;

    private String roomId;
    private String userId;

    private List<String> pixels;

    private int totalScore;

}
