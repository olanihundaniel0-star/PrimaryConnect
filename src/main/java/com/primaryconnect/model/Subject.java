package com.primaryconnect.model;

/**
 * Represents a school subject that can contain lessons, exercises, and media resources.
 */
public class Subject {
    private int subjectId;
    private String name;

    public Subject() {
    }

    public Subject(int subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
