package com.substring.buildbattle.services;

import com.substring.buildbattle.entities.Room;
import com.substring.buildbattle.repositories.DrawingRepository;
import com.substring.buildbattle.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final Random random = new Random();

    public Room createRoom(String hostId, String nickname) {
        String roomId = generateRoomCode();

        Room room = new Room();
        room.setId(roomId);
        room.setHostId(hostId);
        room.getPlayers().add(hostId);
        room.getPlayerNames().put(hostId, safeName(nickname, hostId));
        room.setStatus("LOBBY");

        roomRepository.save(room);
        return room;
    }

    private String safeName(String nickname, String userId) {
        return (nickname == null || nickname.isBlank()) ? userId : nickname.trim();
    }

    private String generateRoomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder(5);
            for(int i = 0; i < 5; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsById(code));

        return code;
    }

    public Room joinRoom(String roomCode, String userId, String nickname) {
        Optional<Room> optionalRoom = roomRepository.findById(roomCode);

        if(optionalRoom.isEmpty()) {
            throw new IllegalArgumentException("Room code not found");
        }

        Room room = optionalRoom.get();

        if(!"LOBBY".equals(room.getStatus())) {
            throw new IllegalArgumentException("Game in-progress");
        }

        if(!room.getPlayers().contains(userId)) {
            room.getPlayers().add(userId);
        }
        room.getPlayerNames().put(userId, safeName(nickname, userId));
        roomRepository.save(room);

        return room;
    }



}
