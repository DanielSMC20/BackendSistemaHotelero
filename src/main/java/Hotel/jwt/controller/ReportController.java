package Hotel.jwt.controller;

import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.dto.report.DailyRevenueItem;
import Hotel.jwt.dto.report.OccupancyItem;
import Hotel.jwt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    private final ReportService service;

    @GetMapping("/revenue")
    public ApiResponse<List<DailyRevenueItem>> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return ApiResponse.ok(service.revenueByDay(start, end));
    }

    @GetMapping("/occupancy")
    public ApiResponse<List<OccupancyItem>> occupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return ApiResponse.ok(service.occupancyByDay(start, end));
    }
}
