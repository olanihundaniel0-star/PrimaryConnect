package com.primaryconnect.model;

/**
 * Represents a pupil record stored by the system.
 */
public class Pupil {
    private int pupilId;
    private String name;
    private String classLevel;
    private String guardianContact;

    public Pupil() {
    }

    public Pupil(int pupilId, String name, String classLevel, String guardianContact) {
        this.pupilId = pupilId;
        this.name = name;
        this.classLevel = classLevel;
        this.guardianContact = guardianContact;
    }

    public int getPupilId() {
        return pupilId;
    }

    public void setPupilId(int pupilId) {
        this.pupilId = pupilId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public String getGuardianContact() {
        return guardianContact;
    }

    public void setGuardianContact(String guardianContact) {
        this.guardianContact = guardianContact;
    }
}
