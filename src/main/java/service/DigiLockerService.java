package service;

import java.util.List;
import java.util.Map;

import dao.DigiLockerDAO;

public class DigiLockerService {

    private final DigiLockerDAO dao =
            new DigiLockerDAO();

    /*
     * Prepare a published result for DigiLocker.
     */
    public boolean prepareDocument(
            int resultId,
            int studentId,
            int semester,
            String verificationCode) {

        if(resultId <= 0 ||
                studentId <= 0 ||
                semester < 1 ||
                semester > 8) {

            return false;
        }

        if(verificationCode == null ||
                verificationCode.trim().isEmpty()) {

            return false;
        }

        return dao.prepareDocument(
                resultId,
                studentId,
                semester,
                verificationCode.trim());
    }

    /*
     * Get DigiLocker documents belonging
     * to a student.
     */
    public List<Map<String,Object>>
            getStudentDocuments(
                    int studentId) {

        if(studentId <= 0) {

            return List.of();
        }

        return dao.getStudentDocuments(
                studentId);
    }

    /*
     * Get all published results that can be
     * prepared for DigiLocker.
     */
    public List<Map<String,Object>>
            getPublishedResultsForPreparation() {

        return dao.getPublishedResultsForPreparation();
    }

    /*
     * Get status of one result.
     */
    public String getDocumentStatus(
            int resultId) {

        if(resultId <= 0) {

            return null;
        }

        return dao.getDocumentStatus(
                resultId);
    }

    /*
     * Change READY -> SUBMITTED.
     */
    public boolean markSubmitted(
            int resultId) {

        if(resultId <= 0) {

            return false;
        }

        return dao.markSubmitted(
                resultId);
    }

    /*
     * Change SUBMITTED/PUBLISHED -> AVAILABLE.
     */
    public boolean markAvailable(
            int resultId) {

        if(resultId <= 0) {

            return false;
        }

        return dao.markAvailable(
                resultId);
    }
    public List<Map<String,Object>> getDocumentsForExport() {

        return dao.getDocumentsForExport();
    }
}