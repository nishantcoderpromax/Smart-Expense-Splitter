package com.splitter.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.splitter.entity.Expense;
import com.splitter.exception.ApiException;
import com.splitter.repository.ExpenseRepository;
import com.splitter.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String exportCsv(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        StringBuilder csv = new StringBuilder("Date,Description,Amount,Category,Paid By\n");
        for (Expense e : expenses) {
            csv.append(e.getCreatedAt().format(DATE_FORMAT)).append(",")
                    .append(escapeCsv(e.getDescription())).append(",")
                    .append(e.getAmount()).append(",")
                    .append(e.getCategory() != null ? e.getCategory().getName() : "Uncategorized").append(",")
                    .append(escapeCsv(e.getPaidBy().getName())).append("\n");
        }
        return csv.toString();
    }

    public byte[] exportPdf(Long groupId) {
        String groupName = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"))
                .getName();
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Expense Report: " + groupName, new Font(Font.HELVETICA, 18, Font.BOLD)));
            document.add(new Paragraph(" ")); // spacer

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            for (String header : new String[]{"Date", "Description", "Amount", "Category", "Paid By"}) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, new Font(Font.HELVETICA, 11, Font.BOLD)));
                table.addCell(cell);
            }

            BigDecimal total = BigDecimal.ZERO;
            for (Expense e : expenses) {
                table.addCell(e.getCreatedAt().format(DATE_FORMAT));
                table.addCell(e.getDescription());
                table.addCell(e.getAmount().toString());
                table.addCell(e.getCategory() != null ? e.getCategory().getName() : "Uncategorized");
                table.addCell(e.getPaidBy().getName());
                total = total.add(e.getAmount());
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total: " + total, new Font(Font.HELVETICA, 13, Font.BOLD)));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
