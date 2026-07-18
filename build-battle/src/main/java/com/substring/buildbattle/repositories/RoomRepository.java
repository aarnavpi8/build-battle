package com.substring.buildbattle.repositories;

import com.substring.buildbattle.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    boolean existsById(String id);
}
