package com.mahmoud.maalflow.modules.shared.settings.service;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.maalflow.modules.installments.customer.entity.Customer;
import com.mahmoud.maalflow.modules.installments.customer.repo.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel Export Service (Requirement #5).
 * Generates Excel files for customer accounts, payment summaries, collection routes.
 *
 * REQUIRES adding Apache POI to pom.xml:
 * <dependency>
 *     <groupId>org.apache.poi</groupId>
 *     <artifactId>poi-ooxml</artifactId>
 *     <version>5.2.5</version>
 * </dependency>
 *
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/shared/settings/service/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Export all customers with their contract summaries to Excel.
     */
    public byte[] exportCustomerAccounts() throws IOException {
        log.info("Exporting customer accounts to Excel");
        List<Customer> customers = customerRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Customer Accounts");
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Header row
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "الاسم", "الهاتف", "العنوان", "الرقم القومي",
                    "عدد العقود", "إجمالي المبلغ", "إجمالي المدفوع", "المتبقي", "الحالة"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Customer customer : customers) {
                List<Contract> contracts = contractRepository.findByCustomerIdList(customer.getId());
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getPhone() != null ? customer.getPhone() : "");
                row.createCell(3).setCellValue(customer.getAddress() != null ? customer.getAddress() : "");
                row.createCell(4).setCellValue(customer.getNationalId() != null ? customer.getNationalId() : "");
                row.createCell(5).setCellValue(contracts.size());

                BigDecimal totalAmount = contracts.stream()
                        .map(Contract::getFinalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaid = contracts.stream()
                        .map(c -> c.getFinalPrice().subtract(c.getRemainingAmount()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalRemaining = contracts.stream()
                        .map(Contract::getRemainingAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                row.createCell(6).setCellValue(totalAmount.doubleValue());
                row.createCell(7).setCellValue(totalPaid.doubleValue());
                row.createCell(8).setCellValue(totalRemaining.doubleValue());
                row.createCell(9).setCellValue(customer.isActive() ? "نشط" : "غير نشط");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Exported {} customers to Excel", customers.size());
            return out.toByteArray();
        }
    }

    /**
     * Export unpaid installments sorted by address (Requirement #6).
     */
    public byte[] exportUnpaidByAddress() throws IOException {
        log.info("Exporting unpaid installments by address");

        // Get active contracts with unpaid schedules, ordered by customer address
        List<Contract> activeContracts = contractRepository.findActiveContractsWithUnpaidSchedules();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Unpaid By Address");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);
            String[] cols = {"العنوان", "اسم الزبون", "الهاتف", "رقم العقد",
                    "رقم القسط", "المبلغ", "تاريخ الاستحقاق", "المدفوع", "المتبقي"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Contract contract : activeContracts) {
                for (InstallmentSchedule schedule : contract.getInstallmentSchedules()) {
                    if (schedule.getStatus() != com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus.PAID) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(contract.getCustomer().getAddress() != null ? contract.getCustomer().getAddress() : "");
                        row.createCell(1).setCellValue(contract.getCustomer().getName());
                        row.createCell(2).setCellValue(contract.getCustomer().getPhone() != null ? contract.getCustomer().getPhone() : "");
                        row.createCell(3).setCellValue(contract.getContractNumber());
                        row.createCell(4).setCellValue(schedule.getSequenceNumber());
                        row.createCell(5).setCellValue(schedule.getAmount().doubleValue());
                        row.createCell(6).setCellValue(schedule.getDueDate().format(DATE_FMT));
                        BigDecimal paid = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
                        row.createCell(7).setCellValue(paid.doubleValue());
                        row.createCell(8).setCellValue(schedule.getAmount().subtract(paid).doubleValue());
                    }
                }
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}

