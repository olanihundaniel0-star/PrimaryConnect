package com.primaryconnect.model;

/**
 * Represents a lesson topic within a subject that may be paired with exercises and media.
 */
public class Topic {
    private int topicId;
    private int subjectId;
    private String classLevel;
    private String title;
    private String mediaPath;

    public Topic() {
    }

    public Topic(int topicId, int subjectId, String classLevel, String title, String mediaPath) {
        this.topicId = topicId;
        this.subjectId = subjectId;
        this.classLevel = classLevel;
        this.title = title;
        this.mediaPath = mediaPath;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
}
