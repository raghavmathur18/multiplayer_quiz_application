package com.quizapp.quizapp.repositories;

import com.quizapp.quizapp.models.AnswerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerDetailRepository extends JpaRepository<AnswerDetail, Integer> {
    List<AnswerDetail> findByResultId(Integer resultId);
}
