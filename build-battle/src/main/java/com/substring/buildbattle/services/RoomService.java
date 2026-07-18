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

    public Room createRoom(String hostId) {
        String roomId = generateRoomCode();

        Room room = new Room();
        room.setId(roomId);
        room.setHostId(hostId);
        room.getPlayers().add(hostId);
        room.setStatus("LOBBY");

        roomRepository.save(room);
        return room;
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

    public Room joinRoom(String roomCode, String userId) {
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
            roomRepository.save(room);
        }

        return room;
    }



}
