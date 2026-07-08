package com.example.ptmanager.service;

import com.example.ptmanager.model.Schedule;
import com.example.ptmanager.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.List;
import java.io.UnsupportedEncodingException;

@Service
public class EmailReminderService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ScheduleRepository scheduleRepo;

    // Chạy lúc 6h sáng mỗi ngày
    // @Scheduled(cron = "0 0 6 * * ?")
    public void sendDailyReminder() {
        sendReminderNow();
    }

    public void sendReminderNow() {
        LocalDate today = LocalDate.now();
        List<Schedule> todaySchedules = scheduleRepo.findByDateAndStatusNot(today, "cancelled");

        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 8px;'>");
        html.append("<h2 style='color:#b30404; text-align:center;'>📅 LỊCH LÊN LỚP HÔM NAY (").append(today).append(")</h2>");
        
        if (todaySchedules.isEmpty()) {
             html.append("<p style='text-align:center; color:#555;'>Hôm nay bạn không có lịch dạy nào. Hãy nghỉ ngơi thật tốt nhé!</p>");
        } else {
             html.append("<p>Chào Hoàng, hôm nay bạn có <strong>").append(todaySchedules.size()).append(" ca dạy</strong>. Vui lòng xem chi tiết bên dưới:</p>");
             html.append("<table border='1' cellpadding='10' cellspacing='0' style='border-collapse:collapse; width:100%; border-color:#eee;'>");
             html.append("<tr style='background-color:#b30404; color:white;'><th align='left'>Học viên</th><th align='center'>Khung giờ</th><th align='center'>Trạng thái</th></tr>");
     
             for (Schedule sch : todaySchedules) {
                 String stuName = sch.getStudent() != null ? sch.getStudent().getName() : "Không rõ";
                 html.append("<tr>");
                 html.append("<td><strong>").append(stuName).append("</strong></td>");
                 html.append("<td align='center'>").append(sch.getStartTime()).append(" - ").append(sch.getEndTime()).append("</td>");
                 html.append("<td align='center'>").append(sch.getStatus().equals("pending") ? "🕒 Sắp tới" : "✅ Đã xong").append("</td>");
                 html.append("</tr>");
             }
             html.append("</table>");
        }
        
        html.append("<p style='margin-top: 20px; font-style: italic; color:#777; text-align:center;'>Hệ thống quản lý PT Manager - Tin nhắn tự động</p>");
        html.append("</div>");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("huyhoangphan2410@gmail.com", "PT Manager System");
            helper.setTo("huyhoangphan2410@gmail.com");
            helper.setSubject("[PT Manager] Nhắc Lịch Dạy Hôm Nay (" + today + ")");
            helper.setText(html.toString(), true);
            
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}
