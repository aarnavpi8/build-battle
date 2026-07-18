package com.substring.buildbattle.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    private String hostId;
    private List<String> players = new ArrayList<>();

    private String status;

    private List<String> themeOptions = new ArrayList<>();
    private String selectedTheme;
    private Map<String, Integer> themeVotes =  new HashMap<>();

    private long phaseEndsAt;

    private Map<String, List<String>> submissions = new ConcurrentHashMap<>();

    public void addSubmission(String playerId, List<String> pixels) {
        this.submissions.put(playerId, pixels);
    }

}
