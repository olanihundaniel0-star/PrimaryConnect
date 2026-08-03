# COS202 Computer Programming II — Group 11

PrimaryConnect is an offline, console-based school management system for a Nigerian primary school. It is intended to run independently on each staff member's laptop, with data reconciled by hand between machines when needed, rather than through a shared network service.

## Features

- Role-based console access for Proprietor, Teacher, and Pupil
- Curriculum, exercises, and media lessons for Mathematics, English Studies, and Basic Science at Primary 5 in the current demo scope; the schema is already structured for later expansion across P1–P6 and a four-subject curriculum
- Attendance tracking rolled up daily → weekly → monthly → termly → session
- End-of-term grading using the 40% test + 60% exam formula, A–F grade assignment, class ranking, and plain-text reporting
- Media playback through the Java Desktop API
- Optional Yoruba, Igbo, and Hausa prompts at login, navigation, and exercise feedback only; this is not a full UI translation layer
- Two-device data sync through USB export/import with duplicate detection; there is no network layer, and each laptop operates as an independent instance

## Architecture

The application is organized in four layers: UI → Service → Data → SQLite. The dependency flow is one-way, with menu classes calling services, services calling DAOs and the database layer, and SQLite acting as the local persistence store. The topology is intentionally offline and multi-device: each laptop hosts its own copy of the database and the project files, and records are moved between machines by USB export/import rather than by a central server.

## Project structure

```text
PrimaryConnect/
├── pom.xml
├── .gitignore
├── src/main/java/com/primaryconnect/
│   ├── Main.java
│   ├── ui/
│   │   ├── ProprietorMenu.java
│   │   ├── TeacherMenu.java
│   │   ├── PupilMenu.java
│   │   └── SessionContext.java
│   ├── service/
│   │   ├── AttendanceEngine.java
│   │   ├── GradingEngine.java
│   │   ├── ExerciseEngine.java
│   │   ├── MediaLauncher.java
│   │   ├── LanguageEngine.java
│   │   ├── SyncExporter.java
│   │   └── SyncImporter.java
│   ├── data/
│   │   ├── DatabaseManager.java
│   │   ├── PupilDAO.java
│   │   ├── ScoreDAO.java
│   │   ├── AttendanceDAO.java
│   │   └── ExerciseDAO.java
│   ├── model/
│   │   ├── Pupil.java, Teacher.java, Subject.java, Topic.java
│   │   └── Exercise.java, Score.java, AttendanceRecord.java
│   └── util/
│       ├── LanguageBundleLoader.java
│       └── Validators.java
├── src/main/resources/
│   ├── db/schema.sql
│   ├── i18n/language_strings.csv
│   └── seed/
│       ├── curriculum_seed.csv
│       ├── exercises_seed.csv
│       └── sample_records_seed.csv
├── src/test/java/com/primaryconnect/
│   ├── service/
│   └── data/
├── content/
│   ├── curriculum/{mathematics,english-studies,basic-science}/
│   ├── media/{mathematics,english-studies,basic-science}/
│   ├── exercises/
│   └── language/
└── docs/
    └── implementation-plan.md
```

## Branching

The repository branch strategy is:

- main (protected) ← development (protected) ← feature/* (six technical branches)
- content/* (three non-technical branches)

All pull requests require review before merging.

## Getting started

1. Clone the repository and switch to the development branch.
2. Run `mvn install` from the project root to compile the application and run the test stubs.
3. Load the SQL schema and seed CSVs from the resources folder into the local SQLite database before first use.
4. Run the entry point with `mvn exec:java -Dexec.mainClass=com.primaryconnect.Main` or by launching Main from the IDE.

## Team

Group 11

Osemwegie Fortunatus Oseahuwen

Balogun Aisha Abiodun

Abdul-raheem Ahmad Oluwapelumi

Mayowa Daniel Tomisin

Omololu Tofunmi

Nkpogone Barile Michael

Olabamiji Muhammad Fathii

Obimba Samuel

Olanihun Daniel Oluwanifemi

Aluko Ifeoluwa Deborah

Benjamin Oluwatobiloba Benjamim


Code review: Copilot