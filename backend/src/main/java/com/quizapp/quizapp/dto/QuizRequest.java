package com.quizapp.quizapp.dto;

import com.quizapp.quizapp.models.Question;
import com.quizapp.quizapp.models.Quiz;
import java.util.List;

public class QuizRequest {
    private Quiz quiz;
    private List<Question> questions;

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
