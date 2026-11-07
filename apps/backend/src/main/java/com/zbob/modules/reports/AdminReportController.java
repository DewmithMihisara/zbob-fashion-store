package com.zbob.modules.reports;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private final ReportService reports;

    public AdminReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/sales")
    SalesReport sales(@RequestParam(defaultValue = "30") int days) {
        return reports.sales(days);
    }
}
