# QuizArena — Real-Time Multiplayer Quiz Competition Application

## Technology Stack (Java Only)
| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| GUI | Java Swing |
| Database | MySQL + JDBC |
| Networking | Java Socket Programming |
| Analytics | JFreeChart |
| Build | Maven |

---

## Project Structure

```
QuizApp/
├── pom.xml                            ← Maven build file
├── sql
│   └── schema.sql                     ← Database schema
└── src/main/java/com/quizapp/
    ├── Main.java                      ← Entry point
    ├── database/
    │   ├── DatabaseConfig.java        ← DB credentials & server port
    │   ├── DatabaseManager.java       ← JDBC connection + auto table creation
    │   ├── UserDAO.java               ← User login/register
    │   ├── ClassDAO.java              ← Class CRUD + student enrollment
    │   ├── QuizDAO.java               ← Quiz & question CRUD
    │   └── ResultDAO.java             ← Results + analytics queries
    ├── models/
    │   ├── User.java
    │   ├── ClassRoom.java
    │   ├── Quiz.java
    │   ├── Question.java
    │   ├── Result.java
    │   └── AnswerDetail.java
    ├── server/
    │   ├── QuizServer.java            ← Socket server (multi-client)
    │   ├── ClientHandler.java         ← Per-student thread handler
    │   └── QuizSession.java           ← Live quiz session state
    ├── client/
    │   └── QuizClient.java            ← Student socket client
    └── gui/
        ├── common/
        │   ├── LoginFrame.java        ← Login & Registration
        │   ├── UITheme.java           ← Colors, fonts, component factory
        │   └── AnalyticsPanel.java    ← JFreeChart chart builders
        ├── teacher/
        │   ├── TeacherDashboard.java  ← Teacher main window
        │   └── QuizEditorFrame.java   ← Add/delete quiz questions
        └── student/
            ├── StudentDashboard.java  ← Student main window
            └── QuizAttemptFrame.java  ← Live quiz interface + timer
```

---

## Setup Instructions

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 2. Database Setup
```sql
-- Run schema.sql in MySQL:
mysql -u root -p < sql/schema.sql
```

Or paste the contents of `sql/schema.sql` into MySQL Workbench.

### 3. Configure Database Credentials
Edit `src/main/java/com/quizapp/database/DatabaseConfig.java`:
```java
public static final String DB_URL      = "jdbc:mysql://localhost:3306/quizapp...";
public static final String DB_USER     = "root";       // your MySQL user
public static final String DB_PASSWORD = "password";   // your MySQL password
```

### 4. Build
```bash
cd QuizApp
mvn clean package -q
```
This creates: `target/QuizArena-Client.jar`

---

## Running the Application

### Option A — With Real-Time Socket Multiplayer

**Terminal 1: Start the Quiz Server**
```bash
java -cp target/QuizArena-Client.jar com.quizapp.Main server
```

**Terminal 2+: Start GUI Clients** (one per student/teacher)
```bash
java -jar target/QuizArena-Client.jar
```

### Option B — GUI Only (Direct DB Mode)
If the socket server is not running, the app automatically falls back to direct database mode. All features work except real-time live server coordination.

```bash
java -jar target/QuizArena-Client.jar
```

---

## Demo Accounts (from schema.sql seed data)
| Role    | Email               | Password |
|---------|---------------------|----------|
| Teacher | teacher@quiz.com    | pass123  |
| Student | student@quiz.com    | pass123  |

---

## Features

### Teacher
- Register / Login
- Create multiple classes with auto-generated join codes
- Invite students by sharing the class code
- Create quizzes with custom time limits
- Add multiple-choice questions (4 options per question)
- Start / Stop quizzes (makes them live for students)
- Delete classes and quizzes
- View analytics: bar charts, line charts, pie charts
- See student rankings, average scores, highest scores

### Student
- Register / Login
- Join classes using class code
- View available quizzes per class
- Attempt live quizzes with countdown timer
- Real-time socket connection to quiz server
- Auto-submit when timer expires
- View score after submission
- View personal analytics: performance trend, correct/incorrect pie chart, quiz score history

### Real-Time Multiplayer (Socket)
- Multiple students connect simultaneously to the quiz server
- Each student gets their own connection thread (CachedThreadPool)
- Server calculates individual results and saves them to DB
- Fallback to direct DB mode if server not running

---

## Socket Protocol (Text-based, newline-delimited)

```
Client → Server:  JOIN:<quizId>:<studentId>:<name>
Server → Client:  QUIZ_INFO:<title>:<count>:<timeSec>
Server → Client:  QUESTION:<idx>:<total>:<text>|<opt1>|<opt2>|<opt3>|<opt4>
Server → Client:  READY
Client → Server:  SUBMIT:<studentId>:<quizId>:<timeTaken>:<ans1,ans2,...>
Server → Client:  RESULT:<score>:<total>:<percentage>
```

---

## Database Tables

| Table | Description |
|-------|-------------|
| `users` | Teachers and students with roles |
| `classes` | Classes created by teachers |
| `student_class` | Student-class enrollment mapping |
| `quizzes` | Quizzes belonging to classes |
| `questions` | MCQ questions with 4 options |
| `results` | Student quiz attempt results |
| `answer_details` | Per-question answer tracking |
