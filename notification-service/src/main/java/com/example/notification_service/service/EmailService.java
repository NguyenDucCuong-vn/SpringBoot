package com.example.notification_service.service;

import com.example.notification_service.event.OrderPlacedEvent;
import jakarta.mail.MessagingException;     
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderEmail(OrderPlacedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("hello@demomailtrap.co");
            helper.setTo("cuongnd30803@gmail.com");
            helper.setSubject("🛒 Đơn hàng mới #" + event.getOrderId());

            String body = "<p>Xin chào userId <b>" + event.getUserId() + "</b>,</p>" +
                    "<p>Đơn hàng #" + event.getOrderId() + " vừa được tạo thành công!</p>" +
                    "<p>Giá tiền: <b>$" + event.getPrice() + "</b></p>";

            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("📧 Gửi email thành công!");

        } catch (MessagingException e) {
            System.err.println("❌ Gửi email thất bại: " + e.getMessage());
        }
    }
}