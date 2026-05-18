package com.quizapp.quizapp.repositories;

import com.quizapp.quizapp.models.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Integer> {
    List<Result> findByQuizId(Integer quizId);
    List<Result> findByStudentId(Integer studentId);
    Optional<Result> findByQuizIdAndStudentId(Integer quizId, Integer studentId);
}
