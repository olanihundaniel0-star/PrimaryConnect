/*
 * PrimaryConnect database schema (single source of truth).
 * All structural DB changes must be made here first.
 * Any modification requires review from the file owner.
 */
PRAGMA foreign_keys = ON;

CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('PROPRIETOR', 'TEACHER', 'PUPIL')),
    linked_id INTEGER
);

CREATE TABLE pupils (
    pupil_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    class_level TEXT NOT NULL,
    guardian_contact TEXT
);

CREATE TABLE teachers (
    teacher_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    subject_specialty TEXT
);

CREATE TABLE subjects (
    subject_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE topics (
    topic_id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    class_level TEXT NOT NULL,
    title TEXT NOT NULL,
    media_path TEXT,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
);

CREATE TABLE exercises (
    exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id INTEGER NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('MULTIPLE_CHOICE', 'FILL_IN_BLANK')),
    question_text TEXT NOT NULL,
    options TEXT,
    correct_answer TEXT NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES topics(topic_id)
);

CREATE TABLE scores (
    score_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pupil_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    session TEXT NOT NULL,
    term TEXT NOT NULL,
    test_score REAL,
    exam_score REAL,
    final_score REAL,
    grade TEXT CHECK (grade IN ('A', 'B', 'C', 'D', 'E', 'F')),
    UNIQUE (pupil_id, subject_id, session, term),
    FOREIGN KEY (pupil_id) REFERENCES pupils(pupil_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
);

CREATE TABLE attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pupil_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    UNIQUE (pupil_id, date),
    FOREIGN KEY (pupil_id) REFERENCES pupils(pupil_id)
);

CREATE TABLE language_strings (
    key TEXT PRIMARY KEY,
    english TEXT NOT NULL,
    yoruba TEXT,
    igbo TEXT,
    hausa TEXT
);

CREATE TABLE sync_log (
    sync_id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    record_count INTEGER,
    status TEXT CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL'))
);
