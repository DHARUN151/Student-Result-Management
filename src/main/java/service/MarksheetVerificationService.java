package service;

import java.security.SecureRandom;

import dao.MarksheetVerificationDAO;

public class MarksheetVerificationService {

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final MarksheetVerificationDAO dao =
            new MarksheetVerificationDAO();

    public String generateVerificationCode(
            int resultId,
            int semester) {

        int randomNumber =
                100000 + secureRandom.nextInt(900000);

        return "MKCE-SEM"
                + semester
                + "-"
                + resultId
                + "-"
                + randomNumber;
    }

    public String createOrGetVerificationCode(
            int resultId,
            int semester) {

        if (resultId <= 0 ||
                semester < 1 ||
                semester > 8) {

            return null;
        }

        String existingCode =
                dao.getVerificationCode(resultId);

        if (existingCode != null &&
                !existingCode.trim().isEmpty()) {

            return existingCode;
        }

        String verificationCode;

        do {

            verificationCode =
                    generateVerificationCode(
                            resultId,
                            semester);

        } while (
                dao.verificationCodeExists(
                        verificationCode));

        boolean created =
                dao.createVerification(
                        resultId,
                        verificationCode);

        if (created) {
            return verificationCode;
        }

        return null;
    }

    public String getVerificationCode(
            int resultId) {

        if (resultId <= 0) {
            return null;
        }

        return dao.getVerificationCode(
                resultId);
    }

    public boolean verifyMarksheet(
            String verificationCode) {

        if (verificationCode == null ||
                verificationCode.trim().isEmpty()) {

            return false;
        }

        return dao.verifyPublishedMarksheet(
                verificationCode.trim());
    }
}