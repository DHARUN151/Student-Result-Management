package model;

public class Subject {

    private int subId;
    private String subCode;
    private String subName;
    private int semester;
    private int credits;

    public Subject() {
    }

    public Subject(int subId, String subCode, String subName,
                   int semester, int credits) {

        this.subId = subId;
        this.subCode = subCode;
        this.subName = subName;
        this.semester = semester;
        this.credits = credits;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}