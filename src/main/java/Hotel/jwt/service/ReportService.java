package Hotel.jwt.service;

import Hotel.jwt.dto.report.DailyRevenueItem;
import Hotel.jwt.dto.report.OccupancyItem;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<DailyRevenueItem> revenueByDay(LocalDate start, LocalDate end);
    List<OccupancyItem> occupancyByDay(LocalDate start, LocalDate end);
    List<DailyRevenueItem> revenueByMonth(int year, int month);
}