package com.example.ptmanager.service;

import com.example.ptmanager.model.Schedule;
import com.example.ptmanager.repository.ScheduleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {
    
    @Autowired
    private ScheduleRepository scheduleRepo;

    public byte[] exportAllSchedules() throws IOException {
        List<Schedule> schedules = scheduleRepo.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lich_Su_Ca_Day");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Ca học", "Tên Học Viên", "Ngày Thực Hiện", "Khung Giờ", "Trạng Thái", "Doanh Thu (VNĐ)"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowIdx = 1;
            for (Schedule sch : schedules) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(sch.getId());
                row.createCell(1).setCellValue(sch.getStudent() != null ? sch.getStudent().getName() : "Không rõ");
                row.createCell(2).setCellValue(sch.getDate().toString());
                row.createCell(3).setCellValue(sch.getStartTime() + " - " + sch.getEndTime());
                
                String st = sch.getStatus();
                String statusStr = st.equals("completed") ? "Đã Tập" : (st.equals("cancelled") ? "Khách Hủy" : "Tương Lai");
                row.createCell(4).setCellValue(statusStr);
                
                int rev = ("completed".equals(st)) ? 300000 : 0;
                row.createCell(5).setCellValue(rev);
            }

            // Auto fit column
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
