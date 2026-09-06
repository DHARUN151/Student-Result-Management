package model;

public class StudentAddress {

    private int addId;
    private int studId;
    private String doorNo;
    private String street;
    private String city;
    private String district;
    private String state;
    private String pincode;

    public StudentAddress() {
    }

    public StudentAddress(int addId, int studId, String doorNo, String street, String city, String district,
                          String state, String pincode) {

        this.addId = addId;
        this.studId = studId;
        this.doorNo = doorNo;
        this.street = street;
        this.city = city;
        this.district = district;
        this.state = state;
        this.pincode = pincode;
    }

    public int getAddId() {
        return addId;
    }

    public void setAddId(int addId) {
        this.addId = addId;
    }

    public int getStudId() {
        return studId;
    }

    public void setStudId(int studId) {
        this.studId = studId;
    }

    public String getDoorNo() {
        return doorNo;
    }

    public void setDoorNo(String doorNo) {
        this.doorNo = doorNo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}