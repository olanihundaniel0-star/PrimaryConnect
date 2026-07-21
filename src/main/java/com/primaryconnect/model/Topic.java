package com.primaryconnect.model;

/**
 * Represents a lesson topic within a subject that may be paired with exercises and media.
 */
public class Topic {
    private int topicId;
    private int subjectId;
    private String classLevel;
    private String title;
    private String term;
    private Integer week;
    private String learningObjectives;
    private String contents;
    private String teacherActivities;
    private String learnerActivities;
    private String teachingMaterials;
    private String assessment;
    private String mediaPath;

    public Topic() {
    }

    public Topic(
            int topicId,
            int subjectId,
            String classLevel,
            String title,
            String term,
            Integer week,
            String learningObjectives,
            String contents,
            String teacherActivities,
            String learnerActivities,
            String teachingMaterials,
            String assessment,
            String mediaPath
    ) {
        this.topicId = topicId;
        this.subjectId = subjectId;
        this.classLevel = classLevel;
        this.title = title;
        this.term = term;
        this.week = week;
        this.learningObjectives = learningObjectives;
        this.contents = contents;
        this.teacherActivities = teacherActivities;
        this.learnerActivities = learnerActivities;
        this.teachingMaterials = teachingMaterials;
        this.assessment = assessment;
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

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTeacherActivities() {
        return teacherActivities;
    }

    public void setTeacherActivities(String teacherActivities) {
        this.teacherActivities = teacherActivities;
    }

    public String getLearnerActivities() {
        return learnerActivities;
    }

    public void setLearnerActivities(String learnerActivities) {
        this.learnerActivities = learnerActivities;
    }

    public String getTeachingMaterials() {
        return teachingMaterials;
    }

    public void setTeachingMaterials(String teachingMaterials) {
        this.teachingMaterials = teachingMaterials;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
}
