package com.quizapp.quizapp.repositories;

import com.quizapp.quizapp.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByClassRoomId(Integer classRoomId);
}
