package com.substring.buildbattle.controllers;

import com.substring.buildbattle.entities.Room;
import com.substring.buildbattle.repositories.RoomRepository;
import com.substring.buildbattle.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public record CreateRoomRequest(String hostId, String nickname) {}
    public record JoinRoomRequest(String userId, String nickname) {}

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest roomRequest) {
        Room newRoom = roomService.createRoom(roomRequest.hostId(), roomRequest.nickname());
        return ResponseEntity.ok(newRoom);
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<Room> joinRoom(@PathVariable String roomCode, @RequestBody JoinRoomRequest roomRequest) {
        try {
            Room room = roomService.joinRoom(roomCode, roomRequest.userId(), roomRequest.nickname());
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
