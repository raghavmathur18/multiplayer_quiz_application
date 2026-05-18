package com.quizapp.quizapp.controllers;

import com.quizapp.quizapp.dto.ResultRequest;
import com.quizapp.quizapp.models.AnswerDetail;
import com.quizapp.quizapp.models.Result;
import com.quizapp.quizapp.repositories.AnswerDetailRepository;
import com.quizapp.quizapp.repositories.QuizRepository;
import com.quizapp.quizapp.repositories.ResultRepository;
import com.quizapp.quizapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin("*")
public class ResultController {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private AnswerDetailRepository answerDetailRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuizRepository quizRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody ResultRequest request) {
        Result result = request.getResult();
        
        var studentOpt = userRepository.findById(result.getStudent().getId());
        var quizOpt = quizRepository.findById(result.getQuiz().getId());
        
        if (studentOpt.isEmpty() || quizOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student or Quiz not found");
        }
        
        result.setStudent(studentOpt.get());
        result.setQuiz(quizOpt.get());
        
        Result savedResult = resultRepository.save(result);

        if (request.getAnswers() != null) {
            for (AnswerDetail ans : request.getAnswers()) {
                ans.setResult(savedResult);
                answerDetailRepository.save(ans);
            }
        }
        return ResponseEntity.ok(savedResult);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Result>> getResultsByStudent(@PathVariable Integer studentId) {
        return ResponseEntity.ok(resultRepository.findByStudentId(studentId));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<Result>> getResultsByQuiz(@PathVariable Integer quizId) {
        return ResponseEntity.ok(resultRepository.findByQuizId(quizId));
    }
}
