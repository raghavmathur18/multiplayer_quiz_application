package com.quizapp.quizapp.controllers;

import com.quizapp.quizapp.dto.QuizRequest;
import com.quizapp.quizapp.models.Question;
import com.quizapp.quizapp.models.Quiz;
import com.quizapp.quizapp.repositories.ClassRoomRepository;
import com.quizapp.quizapp.repositories.QuestionRepository;
import com.quizapp.quizapp.repositories.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin("*")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private ClassRoomRepository classRoomRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizRequest request) {
        Quiz quiz = request.getQuiz();
        var classOpt = classRoomRepository.findById(quiz.getClassRoom().getId());
        if (classOpt.isEmpty()) return ResponseEntity.badRequest().body("Class not found");
        
        quiz.setClassRoom(classOpt.get());
        Quiz savedQuiz = quizRepository.save(quiz);

        for (Question q : request.getQuestions()) {
            q.setQuiz(savedQuiz);
            questionRepository.save(q);
        }
        return ResponseEntity.ok(savedQuiz);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<Quiz>> getQuizzesByClass(@PathVariable Integer classId) {
        return ResponseEntity.ok(quizRepository.findByClassRoomId(classId));
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByQuiz(@PathVariable Integer quizId) {
        return ResponseEntity.ok(questionRepository.findByQuizId(quizId));
    }
}
