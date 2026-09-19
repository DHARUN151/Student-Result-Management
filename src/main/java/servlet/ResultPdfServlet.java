package servlet;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.StudentResultPdfService;

@WebServlet("/ResultPdfServlet")
public class ResultPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentResultPdfService service =
            new StudentResultPdfService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                !"STUDENT".equals(session.getAttribute("role"))) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        Object studentIdObject =
                session.getAttribute("studentId");

        if (studentIdObject == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        int studentId;

        try {

            studentId =
                    Integer.parseInt(
                            studentIdObject.toString());

        } catch (Exception e) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");

            return;
        }

        int semester = 0;

        String semesterParameter =
                request.getParameter("semester");

        if (semesterParameter != null &&
                !semesterParameter.trim().isEmpty()) {

            try {

                semester =
                        Integer.parseInt(
                                semesterParameter);

            } catch (NumberFormatException e) {

                semester = 0;
            }
        }

        /*
         * If no semester is supplied,
         * use the latest published semester.
         */
        if (semester == 0) {

            semester =
                    service.getLatestPublishedSemester(
                            studentId);
        }

        if (semester < 1 || semester > 8) {

            sendError(
                    response,
                    "No valid published semester was found.");

            return;
        }

        Map<String, Object> gradeSheet;

        try {

            gradeSheet =
                    service.getGradeSheet(
                            studentId,
                            semester);

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    response,
                    "Unable to retrieve result data.");

            return;
        }

        if (gradeSheet == null ||
                gradeSheet.isEmpty()) {

            sendError(
                    response,
                    "No published result was found for Semester "
                            + semester + ".");

            return;
        }

        String status =
                value(
                        gradeSheet,
                        "status");

        if (!"PUBLISHED".equalsIgnoreCase(status)) {

            sendError(
                    response,
                    "This result has not been published yet.");

            return;
        }

        try {

            /*
             * Generate the complete PDF first.
             */
            byte[] pdf =
                    generatePdf(
                            gradeSheet);

            if (pdf == null ||
                    pdf.length == 0) {

                throw new Exception(
                        "Generated PDF is empty.");
            }

            response.reset();

            response.setContentType(
                    "application/pdf");

            response.setContentLength(
                    pdf.length);

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"GradeSheet_Semester_"
                            + semester
                            + ".pdf\"");

            response.setHeader(
                    "Cache-Control",
                    "private, no-store, no-cache, must-revalidate");

            response.getOutputStream()
                    .write(pdf);

            response.getOutputStream()
                    .flush();

        } catch (Exception e) {

            e.printStackTrace();

            if (!response.isCommitted()) {

                sendError(
                        response,
                        "Error while generating PDF: "
                                + e.getMessage());
            }
        }
    }

    private byte[] generatePdf(
            Map<String, Object> gradeSheet)
            throws Exception {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        30,
                        30,
                        30,
                        35);

        try {

            PdfWriter.getInstance(
                    document,
                    outputStream);

            document.open();

            addHeader(
                    document,
                    gradeSheet);

            addStudentInformation(
                    document,
                    gradeSheet);

            addSubjectTable(
                    document,
                    gradeSheet);

            addSummary(
                    document,
                    gradeSheet);

            addFooter(
                    document,
                    gradeSheet);

        } finally {

            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream.toByteArray();
    }

    private void addHeader(
            Document document,
            Map<String, Object> gradeSheet)
            throws Exception {

        PdfPTable headerTable =
                new PdfPTable(3);

        headerTable.setWidthPercentage(100);

        headerTable.setWidths(
                new float[]{
                        1.2f,
                        5.6f,
                        1.2f
                });

        /*
         * LEFT COLLEGE LOGO
         */
        PdfPCell leftLogo =
                new PdfPCell();

        leftLogo.setBorder(
                Rectangle.NO_BORDER);

        leftLogo.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        leftLogo.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        addOptionalImage(
                leftLogo,
                "/images/college-logo.png",
                55,
                55);

        headerTable.addCell(
                leftLogo);

        /*
         * COLLEGE INFORMATION
         */
        PdfPCell titleCell =
                new PdfPCell();

        titleCell.setBorder(
                Rectangle.NO_BORDER);

        titleCell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        Font collegeFont =
                new Font(
                        Font.HELVETICA,
                        14,
                        Font.BOLD,
                        Color.BLACK);

        Font normalFont =
                new Font(
                        Font.HELVETICA,
                        7.5f,
                        Font.NORMAL,
                        Color.BLACK);

        Font examFont =
                new Font(
                        Font.HELVETICA,
                        10,
                        Font.BOLD,
                        Color.BLACK);

        Font gradeFont =
                new Font(
                        Font.HELVETICA,
                        12,
                        Font.BOLD,
                        Color.BLACK);

        Paragraph collegeName =
                new Paragraph(
                        "M. KUMARASAMY COLLEGE OF ENGINEERING",
                        collegeFont);

        collegeName.setAlignment(
                Element.ALIGN_CENTER);

        titleCell.addElement(
                collegeName);

        Paragraph autonomous =
                new Paragraph(
                        "AUTONOMOUS",
                        normalFont);

        autonomous.setAlignment(
                Element.ALIGN_CENTER);

        titleCell.addElement(
                autonomous);

        Paragraph address =
                new Paragraph(
                        "Thalavapalayam, Karur - 639 113, Tamilnadu",
                        normalFont);

        address.setAlignment(
                Element.ALIGN_CENTER);

        titleCell.addElement(
                address);

        Paragraph accreditation =
                new Paragraph(
                        "NAAC A+ | NBA ACCREDITED | ISO CERTIFIED",
                        normalFont);

        accreditation.setAlignment(
                Element.ALIGN_CENTER);

        titleCell.addElement(
                accreditation);

        Paragraph examination =
                new Paragraph(
                        "B.E. DEGREE EXAMINATIONS",
                        examFont);

        examination.setAlignment(
                Element.ALIGN_CENTER);

        examination.setSpacingBefore(5);

        titleCell.addElement(
                examination);

        Paragraph gradeSheetTitle =
                new Paragraph(
                        "GRADE SHEET",
                        gradeFont);

        gradeSheetTitle.setAlignment(
                Element.ALIGN_CENTER);

        titleCell.addElement(
                gradeSheetTitle);

        headerTable.addCell(
                titleCell);

        /*
         * RIGHT UNIVERSITY LOGO
         */
        PdfPCell rightLogo =
                new PdfPCell();

        rightLogo.setBorder(
                Rectangle.NO_BORDER);

        rightLogo.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        rightLogo.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        addOptionalImage(
                rightLogo,
                "/images/university-logo.png",
                55,
                55);

        headerTable.addCell(
                rightLogo);

        document.add(
                headerTable);

        Paragraph spacing =
                new Paragraph(" ");

        spacing.setSpacingAfter(2);

        document.add(
                spacing);
    }

    private void addStudentInformation(
            Document document,
            Map<String, Object> gradeSheet)
            throws Exception {

        PdfPTable table =
                new PdfPTable(4);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        1.7f,
                        2.3f,
                        1.7f,
                        2.3f
                });

        Font labelFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.BOLD,
                        Color.BLACK);

        Font valueFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.NORMAL,
                        Color.BLACK);

        addInfoCell(
                table,
                "NAME OF THE CANDIDATE",
                value(
                        gradeSheet,
                        "name"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "REGISTER NO.",
                value(
                        gradeSheet,
                        "regNum"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "DATE OF BIRTH",
                formatDate(
                        gradeSheet.get("dob")),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "GENDER",
                value(
                        gradeSheet,
                        "gender"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "PROGRAMME",
                value(
                        gradeSheet,
                        "programName"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "DEPARTMENT",
                value(
                        gradeSheet,
                        "departmentName"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "ACADEMIC YEAR",
                value(
                        gradeSheet,
                        "yearName"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "SEMESTER",
                value(
                        gradeSheet,
                        "semester"),
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "EXAMINATION",
                value(
                        gradeSheet,
                        "examName"),
                labelFont,
                valueFont);

        /*
         * Use publicationDate if available.
         * Otherwise show current date.
         */
        String publicationDate =
                formatDate(
                        gradeSheet.get(
                                "publicationDate"));

        if (publicationDate.isEmpty()) {

            publicationDate =
                    new SimpleDateFormat(
                            "dd-MM-yyyy")
                            .format(
                                    new Date());
        }

        addInfoCell(
                table,
                "DATE OF PUBLICATION",
                publicationDate,
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "REGULATIONS",
                "2023",
                labelFont,
                valueFont);

        addInfoCell(
                table,
                "RESULT STATUS",
                "PUBLISHED",
                labelFont,
                valueFont);

        document.add(
                table);

        Paragraph spacing =
                new Paragraph(" ");

        spacing.setSpacingAfter(2);

        document.add(
                spacing);
    }

    private void addInfoCell(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                labelFont));

        labelCell.setPadding(4);

        labelCell.setBackgroundColor(
                new Color(
                        230,
                        230,
                        230));

        labelCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        table.addCell(
                labelCell);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                value,
                                valueFont));

        valueCell.setPadding(4);

        valueCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        table.addCell(
                valueCell);
    }

    private void addSubjectTable(
            Document document,
            Map<String, Object> gradeSheet)
            throws Exception {

        PdfPTable table =
                new PdfPTable(9);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        0.55f,
                        1.0f,
                        2.7f,
                        0.6f,
                        0.75f,
                        0.75f,
                        0.75f,
                        0.65f,
                        0.85f
                });

        Font headerFont =
                new Font(
                        Font.HELVETICA,
                        6.2f,
                        Font.BOLD,
                        Color.WHITE);

        String[] headers = {
                "SEM",
                "COURSE CODE",
                "COURSE TITLE",
                "CREDIT",
                "INTERNAL",
                "EXTERNAL",
                "TOTAL",
                "GRADE",
                "RESULT"
        };

        for (String header : headers) {

            PdfPCell cell =
                    new PdfPCell(
                            new Phrase(
                                    header,
                                    headerFont));

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER);

            cell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE);

            cell.setPadding(4);

            cell.setBackgroundColor(
                    new Color(
                            45,
                            45,
                            45));

            table.addCell(
                    cell);
        }

        Font cellFont =
                new Font(
                        Font.HELVETICA,
                        6.3f,
                        Font.NORMAL,
                        Color.BLACK);

        Object subjectsObject =
                gradeSheet.get(
                        "subjects");

        if (subjectsObject instanceof List<?>) {

            List<?> subjects =
                    (List<?>) subjectsObject;

            for (Object subjectObject : subjects) {

                if (!(subjectObject instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> subject =
                        (Map<String, Object>)
                                subjectObject;

                addSubjectCell(
                        table,
                        value(
                                gradeSheet,
                                "semester"),
                        cellFont);

                addSubjectCellLeft(
                        table,
                        value(
                                subject,
                                "subjectCode"),
                        cellFont);

                addSubjectCellLeft(
                        table,
                        value(
                                subject,
                                "subjectName"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "credits"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "internalMark"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "externalMark"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "totalMark"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "grade"),
                        cellFont);

                addSubjectCell(
                        table,
                        value(
                                subject,
                                "resultOutcome"),
                        cellFont);
            }
        }

        document.add(
                table);

        Paragraph spacing =
                new Paragraph(" ");

        spacing.setSpacingAfter(2);

        document.add(
                spacing);
    }

    private void addSubjectCell(
            PdfPTable table,
            String value,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                value,
                                font));

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        cell.setPadding(4);

        table.addCell(
                cell);
    }

    private void addSubjectCellLeft(
            PdfPTable table,
            String value,
            Font font) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                value,
                                font));

        cell.setHorizontalAlignment(
                Element.ALIGN_LEFT);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE);

        cell.setPadding(4);

        table.addCell(
                cell);
    }

    private void addSummary(
            Document document,
            Map<String, Object> gradeSheet)
            throws Exception {

        PdfPTable table =
                new PdfPTable(6);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        1.3f,
                        1.2f,
                        1.3f,
                        1.2f,
                        1.3f,
                        1.2f
                });

        addSummaryRow(
                table,
                "TOTAL MARKS",
                value(
                        gradeSheet,
                        "totalMarks"),
                "MAXIMUM MARKS",
                value(
                        gradeSheet,
                        "maximumMarks"),
                "PERCENTAGE",
                value(
                        gradeSheet,
                        "percentage"));

        addSummaryRow(
                table,
                "SEMESTER GPA",
                value(
                        gradeSheet,
                        "semesterGpa"),
                "CGPA",
                value(
                        gradeSheet,
                        "overallCgpa"),
                "RESULT CLASS",
                value(
                        gradeSheet,
                        "resultClass"));

        document.add(
                table);

        /*
         * IMPORTANT:
         * The Paragraph variable is named gradeScale,
         * not gradeSheet.
         */
        Paragraph gradeScale =
                new Paragraph();

        Font boldFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.BOLD,
                        Color.BLACK);

        Font normalFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.NORMAL,
                        Color.BLACK);

        gradeScale.add(
                new Phrase(
                        "Grade Scale: ",
                        boldFont));

        gradeScale.add(
                new Phrase(
                        "O=10, A+=9, A=8, B+=7, B=6, C=5, U=0",
                        normalFont));

        gradeScale.setSpacingBefore(6);

        document.add(
                gradeScale);
    }

    private void addSummaryRow(
            PdfPTable table,
            String label1,
            String value1,
            String label2,
            String value2,
            String label3,
            String value3) {

        Font labelFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.BOLD,
                        Color.BLACK);

        Font valueFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.NORMAL,
                        Color.BLACK);

        addSummaryCell(
                table,
                label1,
                labelFont,
                true);

        addSummaryCell(
                table,
                value1,
                valueFont,
                false);

        addSummaryCell(
                table,
                label2,
                labelFont,
                true);

        addSummaryCell(
                table,
                value2,
                valueFont,
                false);

        addSummaryCell(
                table,
                label3,
                labelFont,
                true);

        addSummaryCell(
                table,
                value3,
                valueFont,
                false);
    }

    private void addSummaryCell(
            PdfPTable table,
            String value,
            Font font,
            boolean label) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                value,
                                font));

        cell.setPadding(4);

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        if (label) {

            cell.setBackgroundColor(
                    new Color(
                            230,
                            230,
                            230));
        }

        table.addCell(
                cell);
    }

    private void addFooter(
            Document document,
            Map<String, Object> gradeSheet)
            throws Exception {

        Paragraph spacing =
                new Paragraph(" ");

        spacing.setSpacingBefore(8);

        document.add(
                spacing);

        PdfPTable footerTable =
                new PdfPTable(3);

        footerTable.setWidthPercentage(100);

        footerTable.setWidths(
                new float[]{
                        2.5f,
                        2.5f,
                        2.5f
                });

        /*
         * COLLEGE SEAL
         */
        PdfPCell sealCell =
                new PdfPCell();

        sealCell.setBorder(
                Rectangle.NO_BORDER);

        sealCell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        addOptionalImage(
                sealCell,
                "/images/college-seal.png",
                55,
                55);

        Font sealFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.NORMAL,
                        Color.BLACK);

        Paragraph sealText =
                new Paragraph(
                        "COLLEGE SEAL",
                        sealFont);

        sealText.setAlignment(
                Element.ALIGN_CENTER);

        sealCell.addElement(
                sealText);

        footerTable.addCell(
                sealCell);

        /*
         * CENTER INFORMATION
         */
        PdfPCell centerCell =
                new PdfPCell();

        centerCell.setBorder(
                Rectangle.NO_BORDER);

        centerCell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        Font footerFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.NORMAL,
                        Color.BLACK);

        Paragraph generated =
                new Paragraph(
                        "This is a computer generated grade sheet.",
                        footerFont);

        generated.setAlignment(
                Element.ALIGN_CENTER);

        centerCell.addElement(
                generated);

        Paragraph alteration =
                new Paragraph(
                        "No manual alteration is permitted.",
                        footerFont);

        alteration.setAlignment(
                Element.ALIGN_CENTER);

        centerCell.addElement(
                alteration);

        footerTable.addCell(
                centerCell);

        /*
         * CONTROLLER SIGNATURE
         */
        PdfPCell signatureCell =
                new PdfPCell();

        signatureCell.setBorder(
                Rectangle.NO_BORDER);

        signatureCell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        addOptionalImage(
                signatureCell,
                "/images/controller-signature.png",
                100,
                45);

        Font signatureFont =
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.BOLD,
                        Color.BLACK);

        Paragraph signature =
                new Paragraph(
                        "CONTROLLER OF EXAMINATIONS",
                        signatureFont);

        signature.setAlignment(
                Element.ALIGN_CENTER);

        signatureCell.addElement(
                signature);

        footerTable.addCell(
                signatureCell);

        document.add(
                footerTable);
    }

    private void addOptionalImage(
            PdfPCell cell,
            String imagePath,
            float width,
            float height) {

        try {

            InputStream inputStream =
                    getServletContext()
                            .getResourceAsStream(
                                    imagePath);

            if (inputStream == null) {

                System.out.println(
                        "Image not found: "
                                + imagePath);

                return;
            }

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[4096];

            int bytesRead;

            while ((bytesRead =
                    inputStream.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        bytesRead);
            }

            inputStream.close();

            Image image =
                    Image.getInstance(
                            output.toByteArray());

            image.scaleToFit(
                    width,
                    height);

            image.setAlignment(
                    Element.ALIGN_CENTER);

            cell.addElement(
                    image);

        } catch (Exception e) {

            System.out.println(
                    "Unable to load image: "
                            + imagePath);

            System.out.println(
                    e.getMessage());
        }
    }

    private String formatDate(
            Object dateObject) {

        if (dateObject == null) {
            return "";
        }

        try {

            if (dateObject instanceof Date) {

                return new SimpleDateFormat(
                        "dd-MM-yyyy")
                        .format(
                                (Date) dateObject);
            }

            return dateObject.toString();

        } catch (Exception e) {

            return dateObject.toString();
        }
    }

    private String value(
            Map<String, Object> map,
            String key) {

        if (map == null) {
            return "";
        }

        Object object =
                map.get(key);

        if (object == null) {
            return "";
        }

        return String.valueOf(object);
    }

    private void sendError(
            HttpServletResponse response,
            String message)
            throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.reset();

        response.setContentType(
                "text/html;charset=UTF-8");

        response.getWriter().println(
                "<!DOCTYPE html>");

        response.getWriter().println(
                "<html>");

        response.getWriter().println(
                "<head>");

        response.getWriter().println(
                "<title>PDF Error</title>");

        response.getWriter().println(
                "</head>");

        response.getWriter().println(
                "<body>");

        response.getWriter().println(
                "<h2>Unable to download result PDF</h2>");

        response.getWriter().println(
                "<p>"
                        + escapeHtml(message)
                        + "</p>");

        response.getWriter().println(
                "<p><a href='javascript:history.back()'>Go Back</a></p>");

        response.getWriter().println(
                "</body>");

        response.getWriter().println(
                "</html>");
    }

    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}