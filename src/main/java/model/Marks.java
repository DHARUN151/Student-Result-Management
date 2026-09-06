package model;

public class Marks {

    private int markId;
    private int studId;
    private int subId;
    private int internalMark;
    private int externalMark;
    private int totalMark;
    private String grade;
    private String status;

    public Marks() {
    }

    public Marks(int markId, int studId, int subId,
                 int internalMark, int externalMark,
                 int totalMark, String grade, String status) {

        this.markId = markId;
        this.studId = studId;
        this.subId = subId;
        this.internalMark = internalMark;
        this.externalMark = externalMark;
        this.totalMark = totalMark;
        this.grade = grade;
        this.status = status;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getStudId() {
        return studId;
    }

    public void setStudId(int studId) {
        this.studId = studId;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    public int getInternalMark() {
        return internalMark;
    }

    public void setInternalMark(int internalMark) {
        this.internalMark = internalMark;
    }

    public int getExternalMark() {
        return externalMark;
    }

    public void setExternalMark(int externalMark) {
        this.externalMark = externalMark;
    }

    public int getTotalMark() {
        return totalMark;
    }

    public void setTotalMark(int totalMark) {
        this.totalMark = totalMark;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}