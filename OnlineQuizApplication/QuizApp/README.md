# Online Quiz Application

**Project ID:** 65HIBKJS
A desktop Java application (Swing + SQLite/JDBC) that lets administrators build
multiple-choice quizzes and lets users take them, get instant feedback, track
their history, and compete on a leaderboard.

---

## Features

- **User authentication** — create an account or log in; passwords are never
  stored in plain text (see *Security* below).
- **Quiz management (admin)** — create, edit, and delete quizzes; add, edit,
  and delete multiple-choice questions (single- or multi-answer), each with a
  difficulty label and an optional time limit.
- **Quiz taking** — questions are presented one at a time in random order;
  each answer is checked immediately with correct/incorrect feedback before
  moving to the next question.
- **Scoring & progress tracking** — final score is shown at the end of every
  quiz and every attempt is saved; users can review their full attempt
  history (score, percentage, date) at any time.
- **Leaderboard** — overall leaderboard across all quizzes, or filtered to a
  single quiz, ranked by average percentage score.
- **Data persistence** — all data (users, quizzes, questions, attempts) is
  stored in a local SQLite database via JDBC (plain `PreparedStatement`
  CRUD, no ORM).
- **Error handling & validation** — empty fields, duplicate usernames, wrong
  passwords, and empty quiz submissions are all caught and reported to the
  user with a message dialog instead of crashing.
- **Security** — passwords are hashed with PBKDF2WithHmacSHA256 (65,536
  iterations) using a unique random salt per user; only the hash and salt are
  stored, never the password itself.

### Optional/stretch features implemented
- Timer-based quizzes (admin sets a per-quiz time limit; auto-submits when
  time runs out).
- Random question order per attempt.
- Difficulty levels (Easy / Medium / Hard) shown when browsing quizzes.
- Multi-select questions (more than one correct answer) as well as classic
  single-answer questions.

---

## Requirements

- Java 17 or newer (JDK, not just JRE, if you want to rebuild from source).
- A desktop environment capable of running Swing GUI apps (Linux, macOS, or
  Windows all work; on headless Linux servers you'd need a virtual display
  such as Xvfb).
- No external services or internet connection are required at runtime — the
  SQLite JDBC driver is bundled in `lib/`.

## Project Structure

```
QuizApp/
├── src/com/quizapp/
│   ├── Main.java                 Entry point
│   ├── model/                    Plain data classes (User, Quiz, Question, QuizAttempt)
│   ├── db/DatabaseManager.java   SQLite connection + schema creation
│   ├── service/                  Business logic (AuthService, QuizService, ScoreService)
│   ├── util/PasswordUtil.java    PBKDF2 password hashing/salting
│   └── ui/                       All Swing windows/dialogs
├── lib/                          Bundled JDBC driver + slf4j (no external download needed)
├── data/                         SQLite database file is created here on first run
├── build.sh                      Compiles the project into out/
└── run.sh                        Builds (if needed) and launches the app
```

## Setup & Running

```bash
cd QuizApp
./build.sh   # compiles everything into out/
./run.sh     # launches the application
```

If you prefer to run the commands by hand:

```bash
find src -name "*.java" > sources.txt
javac -cp "lib/sqlite-jdbc-3.44.1.0.jar:lib/slf4j-api-1.7.32.jar" -d out @sources.txt
java -cp "out:lib/sqlite-jdbc-3.44.1.0.jar:lib/slf4j-api-1.7.32.jar:lib/slf4j-nop-1.7.32.jar" com.quizapp.Main
```

On first launch the app automatically creates `data/quizapp.db` and seeds a
default administrator account:

- **username:** `admin`
- **password:** `admin123`

(Change this password or create your own admin account after first login —
registration lets you check "Register as administrator".)

## User Guide

1. **Log in** with an existing account, or click **Create Account** to
   register (check "Register as administrator" if you want quiz-management
   rights).
2. From the **Main Menu**:
   - **Take a Quiz** — browse quizzes with questions, select one, and start.
     Answer each question and click **Submit Answer** to see immediate
     feedback, then **Next Question**. At the end you'll see your score and
     it's saved automatically.
   - **My Quiz History** — table of every quiz you've taken, with score,
     percentage, and date.
   - **Leaderboard** — top scorers overall or per-quiz.
   - **Manage Quizzes (Admin)** — only visible to admin accounts. Add/edit/
     delete quizzes on the left, and add/edit/delete that quiz's questions on
     the right. Each question supports 2+ options, marking one or more as
     correct, and an optional "multiple correct answers" mode.

## Database Schema (SQLite)

- `users(id, username, password_hash, salt, is_admin)`
- `quizzes(id, title, description, difficulty, time_limit_seconds, created_by)`
- `questions(id, quiz_id, text, options, correct_indices, multiple_answer)`
- `attempts(id, user_id, quiz_id, score, total_questions, attempt_date)`

Options and correct answer indices are stored as delimited strings
(`||`-joined options, comma-joined indices) to keep the schema simple while
still supporting an arbitrary number of options per question.

## Assumptions & Limitations

- This is a single-user desktop app per running instance — it's not a
  client/server system, so "logging in" only separates data by user record
  in the shared local database, it doesn't provide network multi-user
  access. Running two instances against the same `data/quizapp.db` file
  works but isn't heavily concurrency-tested.
- There's no password-reset flow; if a user forgets their password an
  administrator would need to reset it directly in the database.
- Difficulty level and time limit are informational/enforced client-side
  only — there's no server-side anti-cheat.
- The leaderboard's "quiz-specific" view uses each user's *best* attempt;
  the "overall" view uses the *average* of all attempts, since these felt
  like the most meaningful ranking for each context.

## Testing Notes

The service layer (`AuthService`, `QuizService`, `ScoreService`,
`PasswordUtil`) was exercised with an end-to-end smoke test covering:
registration, duplicate-username rejection, login success/failure, password
hash verification, quiz/question creation, editing, deletion (including
cascading question deletes when a quiz is deleted), attempt recording, and
leaderboard/history aggregation — all passing. All Swing windows and dialogs
were also verified to construct and render correctly under a virtual X
display.
