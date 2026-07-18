package com.substring.buildbattle.repositories;

import com.substring.buildbattle.entities.Drawing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DrawingRepository extends MongoRepository<Drawing, String> {

    List<Drawing> findByRoomId(String roomId);

    Optional<Drawing> findByRoomIdAndUserId(String roomId, String userId);
}
