package com.mahmoud.maalflow.modules.shared.constants;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class AppConstants {

    //  Default values
    public static final Long DEFAULT_POOL_ID = 1L;
    public static final Long SYSTEM_USER_ID = 1L;

    //  docs
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    public static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    public static final List<String> ALLOWED_PDF_EXTENSIONS = List.of("pdf");
    public static final List<String> ALLOWED_DOC_EXTENSIONS = List.of("doc", "docx", "xls", "xlsx", "txt");


    // Date formatting
    public static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    // Pagination
    public static final int DEFAULT_HISTORY_PAGE_SIZE = 20;
    public static final int MAX_HISTORY_PAGE_SIZE = 100;

    // Business rules
    public static final BigDecimal ROUNDING_UNIT = BigDecimal.valueOf(50);
    public static final BigDecimal MINIMUM_INSTALLMENT = BigDecimal.valueOf(50);
    public static final BigDecimal DEFAULT_MANAGEMENT_FEE_PERCENTAGE = BigDecimal.valueOf(0.30); // 30%
    public static final int DEFAULT_REMINDER_DAYS = 5;
    public static final int MAX_REMINDER_ATTEMPTS = 5;
    public static final BigDecimal MIN_PURCHASE_PRICE = BigDecimal.valueOf(100);




    private AppConstants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Constants class");
    }

}
