package model;

public class AcademicDetails {

    private int acadId;
    private int studId;
    private String depart;
    private int admissionYear;
    private int semester;

    public AcademicDetails() {
    }

    public AcademicDetails(int acadId, int studId, String depart,
                           int admissionYear, int semester) {

        this.acadId = acadId;
        this.studId = studId;
        this.depart = depart;
        this.admissionYear = admissionYear;
        this.semester = semester;
    }

    public int getAcadId() {
        return acadId;
    }

    public void setAcadId(int acadId) {
        this.acadId = acadId;
    }

    public int getStudId() {
        return studId;
    }

    public void setStudId(int studId) {
        this.studId = studId;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public void setAdmissionYear(int admissionYear) {
        this.admissionYear = admissionYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }
}