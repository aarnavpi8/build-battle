package com.substring.buildbattle.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "drawings")
public class Drawing {

    @Id
    private String id;

    private String roomId;
    private String userId;

    private String username;

    private List<String> pixels;

    private int totalScore;

    private Set<String> voters = new HashSet<>();

}
