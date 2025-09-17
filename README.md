# Spring Boot Microservices – Project Flow & Guide

## 1) Kiến trúc tổng quan
- **discovery-server**: Eureka Server (8761) – service registry.
- **api-gateway**: Spring Cloud Gateway (8080) – định tuyến HTTP.
- **user-service**: Quản lý người dùng, đăng ký/đăng nhập, cấp JWT. DB: H2.
- **order-service**: Quản lý đơn hàng, gọi sang user-service để lấy thông tin người dùng. DB: H2.

Liên lạc nội bộ dùng service-id qua Eureka: `lb://user-service`, `lb://order-service`.

## 2) Luồng chính
### 2.1 Đăng ký & Đăng nhập (user-service)
1. Client gọi `POST /api/auth/register` qua Gateway → `user-service` tạo user, mã hóa mật khẩu.
2. Client gọi `POST /api/auth/login` → `user-service` trả JWT (JJWT 0.12.x). Token chứa claim `userId` và `sub` là email.

### 2.2 Tạo đơn hàng (order-service)
1. Client gọi `POST /api/orders` (qua Gateway) kèm `Authorization: Bearer <token>`.
2. `order-service` có `JwtAuthFilter` đọc JWT, đưa `userId` vào `SecurityContext`.
3. `OrderController` lấy `userId` từ `SecurityContext`, gắn vào `Order` trước khi lưu DB.
4. Trả về bản ghi order đã tạo.

### 2.3 Lấy chi tiết đơn hàng (order-service)
1. Client gọi `GET /api/orders/{orderId}` (kèm JWT).
2. `order-service` đọc `Order` theo `orderId`. Nếu `userId != null`, gọi `user-service` để lấy `UserDTO`.
3. Trả về `OrderResponse` gồm `orderId`, `product`, `price`, `user` (có thể null nếu user không tồn tại).

## 3) Bảo mật & Cấu hình
### 3.1 JWT
- Ký & xác thực bằng JJWT 0.12.x.
- `user-service` sinh token ở `JwtService`.
- `order-service` xác thực ở `JwtAuthFilter` (parser.verifyWith(...).parseSignedClaims(...)).

### 3.2 Security rules
- `user-service`: cho phép public `POST /api/auth/**`, cho phép public `GET /api/users/**` hỗ trợ service-to-service (giảm ràng buộc khi demo). Các endpoint khác yêu cầu auth.
- `order-service`: yêu cầu JWT cho `/api/orders/**`.

## 4) Gateway routes (application.properties)
- `/api/users/**` → `lb://user-service`
- `/api/auth/**` → `lb://user-service`
- `/api/orders/**` → `lb://order-service`
- Eureka: `http://localhost:8761/eureka/`

## 5) Thứ tự chạy & Kiểm tra nhanh
1. discovery-server → user-service → order-service → api-gateway.
2. Mở `http://localhost:8761` để thấy service đã register.
3. Postman flow (qua Gateway `http://localhost:8080`):
   - Đăng ký: `POST /api/auth/register` (email, password, name)
   - Đăng nhập: `POST /api/auth/login` → lấy `token`
   - Tạo order: `POST /api/orders` kèm header `Authorization: Bearer <token>`
   - Danh sách: `GET /api/orders` (JWT)
   - Chi tiết: `GET /api/orders/{orderId}` (JWT)

## 6) Lưu ý dữ liệu & đồng bộ
- H2 mặc định in-memory: dữ liệu mất khi service restart. Nếu bạn tạo order với `userId` từ token cũ nhưng đã restart `user-service`, user có thể không còn → `user=null` khi xem chi tiết.
- Giải pháp: chuyển H2 sang file hoặc MySQL (cả 2 service), hoặc đảm bảo register/login → tạo order → xem chi tiết liền mạch, không restart.

## 7) Xử lý lỗi điển hình
- 401/403: Thiếu/nhầm JWT. Gọi login lại và gửi `Authorization: Bearer <token>`.
- 404 ở `GET /api/orders/{id}`: Sai `orderId`. Lấy từ `POST /api/orders` hoặc `GET /api/orders`.
- 500 khi xem chi tiết đơn: `user-service` không tìm thấy user theo `userId` của order. Tạo user + login lại → tạo order mới.

## 8) Nâng cấp gợi ý
- Thêm Feign Interceptor gửi JWT từ `order-service` sang `user-service` thay vì mở public `GET /api/users/**`.
- Trả 404 khi user không tồn tại (không trả `user=null`) để phân biệt lỗi dữ liệu.
- Chuyển DB sang MySQL, cấu hình Flyway cho schema.
- Thêm tests (integration với Testcontainers), logging/observability.

---
Mọi thắc mắc hoặc cần bật/tắt rule bảo mật, cập nhật DB persistence, hoặc thêm interceptor JWT giữa các service, xem tiếp mục 8 hoặc liên hệ maintainer.


