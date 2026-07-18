package com.substring.buildbattle.controllers;

import com.substring.buildbattle.services.GameEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameEngineService gameEngineService;

    public record ThemeVotePayload(String userId, String theme) {}
    public record DrawingPayload(String userId, List<String> pixels) {}
    public record ArtVotePayload(String userId, String drawingId, int score) {}

    @MessageMapping("/room/{roomId}/start")
    public void handleStartGame(@DestinationVariable String roomId) {
        gameEngineService.startThemeVote(roomId);
    }

    @MessageMapping("/room/{roomId}/theme-vote")
    public void handleThemeVote(@DestinationVariable String roomId, @Payload ThemeVotePayload payload) {

    }

    @MessageMapping("/room/{roomId}/submit-drawing")
    public void handleDrawingSubmission(@DestinationVariable String roomId, @Payload DrawingPayload payload) {
        gameEngineService.saveDrawing(roomId, payload.userId(), payload.pixels());
    }

    @MessageMapping("/room/{roomId}/art-vote")
    public void handleArtVote(@DestinationVariable String roomId, @Payload ArtVotePayload payload) {

    }

}
