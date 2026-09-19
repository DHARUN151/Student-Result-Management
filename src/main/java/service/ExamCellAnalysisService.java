package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dao.ExamCellAnalysisDAO;

public class ExamCellAnalysisService {

    private ExamCellAnalysisDAO dao=new ExamCellAnalysisDAO();

    public List<Map<String,Object>> getDepartments() {
        return dao.getDepartments();
    }

    public Map<String,Object> getAnalysis(int departmentId) {

        Map<String,Object> result=new HashMap<>();

        List<Double> values=
                dao.getDepartmentPercentages(departmentId);

        String departmentName=
                dao.getDepartmentName(departmentId);

        result.put("departmentName",departmentName);
        result.put("totalStudents",values.size());

        if(values.isEmpty()) {

            result.put("mean",0.0);
            result.put("standardDeviation",0.0);
            result.put("highest",0.0);
            result.put("lowest",0.0);
            result.put("passCount",0);
            result.put("failCount",0);
            result.put("passPercentage",0.0);
            result.put(
                    "students",
                    new ArrayList<Map<String,Object>>());

            result.put(
                    "distribution",
                    new int[]{0,0,0,0,0});

            return result;
        }

        double sum=0;
        double highest=values.get(0);
        double lowest=values.get(0);

        int passCount=0;

        for(double value:values) {

            sum+=value;

            if(value>highest) {
                highest=value;
            }

            if(value<lowest) {
                lowest=value;
            }

            if(value>=40) {
                passCount++;
            }
        }

        double mean=sum/values.size();

        double variance=0;

        for(double value:values) {
            variance+=Math.pow(value-mean,2);
        }

        double standardDeviation=
                Math.sqrt(variance/values.size());

        List<Map<String,Object>> students=
                new ArrayList<>();

        int[] distribution=
                new int[]{0,0,0,0,0};

        for(double value:values) {

            double z=0;

            if(standardDeviation>0) {
                z=(value-mean)/standardDeviation;
            }

            String classification;

            if(z<-2) {

                classification="Very Low";
                distribution[0]++;

            } else if(z<-1) {

                classification="Below Average";
                distribution[1]++;

            } else if(z<=1) {

                classification="Average";
                distribution[2]++;

            } else if(z<=2) {

                classification="Above Average";
                distribution[3]++;

            } else {

                classification="High Performer";
                distribution[4]++;
            }

            Map<String,Object> student=
                    new HashMap<>();

            student.put("percentage",value);
            student.put("zScore",z);
            student.put("classification",classification);

            students.add(student);
        }

        double passPercentage=
                (passCount*100.0)/values.size();

        result.put("mean",mean);
        result.put(
                "standardDeviation",
                standardDeviation);

        result.put("highest",highest);
        result.put("lowest",lowest);

        result.put("passCount",passCount);
        result.put(
                "failCount",
                values.size()-passCount);

        result.put(
                "passPercentage",
                passPercentage);

        result.put("students",students);
        result.put("distribution",distribution);
        result.put("percentages",values);

        return result;
    }

    public boolean approveDepartmentResults(
            int departmentId,
            int userId,
            String remarks) {

        if(departmentId<=0 || userId<=0) {
            return false;
        }

        if(remarks==null) {
            remarks="";
        }

        remarks=remarks.trim();

        if(remarks.length()>500) {
            remarks=remarks.substring(0,500);
        }

        return dao.approveDepartmentResults(
                departmentId,
                userId,
                remarks);
    }

    public List<Map<String,Object>> getApprovedDepartments() {
        return dao.getApprovedDepartments();
    }

    /*
     * Phase 8:
     * Get publication completeness status.
     */
    public Map<String,Integer> getPublicationCompleteness(
            int departmentId) {

        if(departmentId<=0) {

            Map<String,Integer> result=
                    new HashMap<>();

            result.put("incompleteGroups",0);
            result.put("missingMarks",0);

            return result;
        }

        return dao.getPublicationCompleteness(
                departmentId);
    }

    /*
     * Phase 8:
     * Publish only when all required marks
     * have been entered.
     */
    public boolean publishDepartmentResults(
            int departmentId,
            int userId,
            String remarks) {

        if(departmentId<=0 || userId<=0) {
            return false;
        }

        if(remarks==null) {
            remarks="";
        }

        remarks=remarks.trim();

        if(remarks.length()>500) {
            remarks=remarks.substring(0,500);
        }

        return dao.publishDepartmentResults(
                departmentId,
                userId,
                remarks);
    }
    public List<Map<String,Object>> getPublishedResultsForDepartment(
            int departmentId) {

        if(departmentId<=0) {
            return new ArrayList<>();
        }

        return dao.getPublishedResultsForDepartment(
                departmentId);
    }
}