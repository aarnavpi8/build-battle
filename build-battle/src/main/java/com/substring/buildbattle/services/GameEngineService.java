package com.substring.buildbattle.services;

import com.substring.buildbattle.entities.Drawing;
import com.substring.buildbattle.entities.Room;
import com.substring.buildbattle.repositories.DrawingRepository;
import com.substring.buildbattle.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import com.substring.buildbattle.config.SchedulerConfig;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private final RoomRepository roomRepository;
    private final DrawingRepository drawingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final TaskScheduler taskScheduler;

    public record PhaseUpdate(String phase, int durationSeconds, Object data) {
    }

    public void startThemeVote(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setStatus("THEME_VOTE");

        room.setThemeOptions(List.of("Car", "Plane", "House", "Space"));
        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/phase",
                new PhaseUpdate("THEME_VOTE", 15, room.getThemeOptions())
        );

        taskScheduler.schedule(
                () -> startDrawingPhase(roomId),
                Instant.now().plusSeconds(15)
        );
    }

    public void startDrawingPhase(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setStatus("DRAWING");

        room.setSelectedTheme(room.getThemeOptions().get(0));
        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "topic/room/" + roomId + "/phase",
                new PhaseUpdate("DRAWING", 120, room.getSelectedTheme())
        );

        taskScheduler.schedule(
                () -> startArtVotePhase(roomId),
                Instant.now().plusSeconds((120))
        );
    }

    public void saveDrawing(String roomId, String userId, byte[] pixels) {
        Drawing drawing = new Drawing();

        drawing.setUserId(userId);
        drawing.setRoomId(roomId);
        drawing.setPixels(pixels);
        drawing.setTotalScore(0);

        drawingRepository.save(drawing);
    }

    public void startArtVotePhase(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setStatus("ART_VOTE");
        roomRepository.save(room);

        List<Drawing> drawings = drawingRepository.findByRoomId(roomId);

        if(drawings.isEmpty()) {
            startLeaderboardPhase(roomId);
            return;
        }

        int displayDurationSeconds = 10;

        for(int i = 0; i < drawings.size(); i++) {
            Drawing currentDrawing = drawings.get(i);

            taskScheduler.schedule(
                    () -> broadcastDrawingForVoting(roomId, currentDrawing, displayDurationSeconds),
                    Instant.now().plusSeconds((long) i * displayDurationSeconds)
            );
        }

        taskScheduler.schedule(
                () -> startLeaderboardPhase(roomId),
                Instant.now().plusSeconds((long) drawings.size() * displayDurationSeconds)
        );
    }

    private void broadcastDrawingForVoting(String roomId, Drawing drawing, int displayDurationSeconds) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/phase",
                new PhaseUpdate("ART_VOTE", displayDurationSeconds, drawing)
        );
    }

    public void registerThemeVote(String roomId, String theme) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        Map<String, Integer> votes = room.getThemeVotes();
        votes.put(theme, votes.getOrDefault(theme, 0) + 1);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/theme-updates", votes);
    }

    public void registerArtVote(String roomId, String drawingId, int score) {
        Drawing drawing = drawingRepository.findById(drawingId)
                .orElseThrow(() -> new IllegalArgumentException("Drawing not found"));

        drawing.setTotalScore(drawing.getTotalScore() + score);
        drawingRepository.save(drawing);
    }

    public void startLeaderboardPhase(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setStatus("LEADERBOARD");
        roomRepository.save(room);

        List<Drawing> rankedDrawings = drawingRepository.findByRoomId(roomId)
                .stream()
                .sorted((d1, d2) -> Integer.compare(d2.getTotalScore(), d1.getTotalScore()))
                .toList();

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/phase",
                new PhaseUpdate("LEADERBOARD", 30, rankedDrawings)
        );
    }

}
