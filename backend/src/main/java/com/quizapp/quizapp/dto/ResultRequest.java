package com.quizapp.quizapp.dto;

import com.quizapp.quizapp.models.AnswerDetail;
import com.quizapp.quizapp.models.Result;
import java.util.List;

public class ResultRequest {
    private Result result;
    private List<AnswerDetail> answers;

    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }
    public List<AnswerDetail> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDetail> answers) { this.answers = answers; }
}
