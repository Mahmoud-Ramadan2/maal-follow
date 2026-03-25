package com.mahmoud.maalflow.modules.shared.settings.controller;

import com.mahmoud.maalflow.modules.shared.settings.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Excel Export Controller (Requirement #5).
 * Copy to: src/main/java/com/mahmoud/maalflow/modules/shared/settings/controller/
 */
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExcelExportController {

    private final ExcelExportService exportService;

    @GetMapping("/customers")
    public ResponseEntity<byte[]> exportCustomers() throws Exception {
        byte[] excelData = exportService.exportCustomerAccounts();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @GetMapping("/unpaid-by-address")
    public ResponseEntity<byte[]> exportUnpaidByAddress() throws Exception {
        byte[] excelData = exportService.exportUnpaidByAddress();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unpaid-by-address.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}

