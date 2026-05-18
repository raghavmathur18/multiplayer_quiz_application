package com.quizapp.quizapp.repositories;

import com.quizapp.quizapp.models.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Integer> {
    Optional<ClassRoom> findByClassCode(String classCode);
    List<ClassRoom> findByTeacherId(Integer teacherId);
    List<ClassRoom> findByStudentsId(Integer studentId);
}
