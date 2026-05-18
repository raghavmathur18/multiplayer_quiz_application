package com.quizapp.quizapp.controllers;

import com.quizapp.quizapp.models.ClassRoom;
import com.quizapp.quizapp.models.User;
import com.quizapp.quizapp.repositories.ClassRoomRepository;
import com.quizapp.quizapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin("*")
public class ClassController {

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestBody ClassRoom classRoom) {
        // Generate a random class code
        classRoom.setClassCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        ClassRoom saved = classRoomRepository.save(classRoom);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ClassRoom>> getClassesByTeacher(@PathVariable Integer teacherId) {
        return ResponseEntity.ok(classRoomRepository.findByTeacherId(teacherId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ClassRoom>> getClassesByStudent(@PathVariable Integer studentId) {
        return ResponseEntity.ok(classRoomRepository.findByStudentsId(studentId));
    }

    @PostMapping("/join/{studentId}/{classCode}")
    public ResponseEntity<?> joinClass(@PathVariable Integer studentId, @PathVariable String classCode) {
        var classOpt = classRoomRepository.findByClassCode(classCode);
        if (classOpt.isEmpty()) return ResponseEntity.badRequest().body("Class not found");
        var userOpt = userRepository.findById(studentId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        ClassRoom classRoom = classOpt.get();
        classRoom.getStudents().add(userOpt.get());
        classRoomRepository.save(classRoom);
        return ResponseEntity.ok(classRoom);
    }
}
