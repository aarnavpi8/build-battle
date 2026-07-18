package com.substring.buildbattle.services;

import com.substring.buildbattle.entities.Drawing;
import com.substring.buildbattle.entities.Room;
import com.substring.buildbattle.repositories.DrawingRepository;
import com.substring.buildbattle.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import com.substring.buildbattle.config.SchedulerConfig;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private static final int THEME_VOTE_SECONDS = 15;
    private static final int DRAWING_SECONDS = 120;
    private static final int ART_VOTE_PER_DRAWING_SECONDS = 10;
    private static final int LEADERBOARD_SECONDS = 30;

    private final RoomRepository roomRepository;
    private final DrawingRepository drawingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;

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
                new PhaseUpdate("THEME_VOTE", THEME_VOTE_SECONDS, room.getThemeOptions())
        );

        taskScheduler.schedule(
                () -> startDrawingPhase(roomId),
                Instant.now().plusSeconds(THEME_VOTE_SECONDS)
        );
    }

    public void startDrawingPhase(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setStatus("DRAWING");

        room.setSelectedTheme(resolveWinningTheme(room));
        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/phase",
                new PhaseUpdate("DRAWING", DRAWING_SECONDS, room.getSelectedTheme())
        );

        taskScheduler.schedule(
                () -> startArtVotePhase(roomId),
            Instant.now().plusSeconds((DRAWING_SECONDS + 3))
        );
    }

    private String resolveWinningTheme(Room room) {
        Map<String, Integer> votes = room.getThemeVotes();

        if(votes == null || votes.isEmpty()) {
            return room.getThemeOptions().get(0);
        }

        return votes.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(room.getThemeOptions().get(0));
    }

    public void saveDrawing(String roomId, String userId, List<String> pixels) {
        Drawing drawing = drawingRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(Drawing::new);

        drawing.setUserId(userId);
        drawing.setRoomId(roomId);
        drawing.setPixels(pixels);

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

        int displayDurationSeconds = ART_VOTE_PER_DRAWING_SECONDS;

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
//        Drawing drawing = drawingRepository.findById(drawingId)
//                .orElseThrow(() -> new IllegalArgumentException("Drawing not found"));
//
//        drawing.setTotalScore(drawing.getTotalScore() + score);
//        drawingRepository.save(drawing);
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }
        // Atomic increment so concurrent voters can't clobber each other's updates.
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(drawingId)),
                new Update().inc("totalScore", score),
                Drawing.class
        );
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
                new PhaseUpdate("LEADERBOARD", LEADERBOARD_SECONDS, rankedDrawings)
        );
    }

}
