package service;

import dao.MissingMarksDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MissingMarksService {

    private MissingMarksDAO dao=new MissingMarksDAO();

    public List<Map<String,Object>> getFacultyMissingMarks(int userId) {

        if(userId<=0) {
            return new ArrayList<>();
        }

        return dao.getFacultyMissingMarks(userId);
    }

    public List<Map<String,Object>> getHODMissingMarks(int userId) {

        if(userId<=0) {
            return new ArrayList<>();
        }

        return dao.getHODMissingMarks(userId);
    }
}