CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL,
    language TEXT NOT NULL DEFAULT 'english'
);

CREATE TABLE IF NOT EXISTS pupils (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pupil_id TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    class_level TEXT NOT NULL,
    gender TEXT,
    guardian_name TEXT
);

CREATE TABLE IF NOT EXISTS teachers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    teacher_id TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    subject TEXT,
    class_level TEXT
);

CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    class_level TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS topics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    topic_name TEXT NOT NULL,
    media_path TEXT,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE IF NOT EXISTS exercises (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id INTEGER NOT NULL,
    exercise_type TEXT NOT NULL,
    question_text TEXT NOT NULL,
    options TEXT,
    correct_answer TEXT NOT NULL,
    FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE TABLE IF NOT EXISTS scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pupil_id TEXT NOT NULL,
    subject TEXT NOT NULL,
    term TEXT NOT NULL,
    session TEXT NOT NULL,
    test_score REAL NOT NULL,
    exam_score REAL NOT NULL,
    final_score REAL NOT NULL,
    grade TEXT NOT NULL,
    rank_position INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pupil_id TEXT NOT NULL,
    attendance_date TEXT NOT NULL,
    status TEXT NOT NULL,
    session TEXT NOT NULL,
    term TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS language_strings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT NOT NULL UNIQUE,
    english TEXT NOT NULL,
    yoruba TEXT,
    igbo TEXT,
    hausa TEXT
);

CREATE TABLE IF NOT EXISTS sync_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    source_device TEXT NOT NULL,
    exported_at TEXT NOT NULL,
    imported_at TEXT,
    record_count INTEGER NOT NULL,
    status TEXT NOT NULL
);
