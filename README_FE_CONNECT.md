README — Frontend integration & quick start

Mục đích
- Hướng dẫn nhanh cho Frontend (Android native / other clients) để kết nối tới backend Spring Boot, lấy token JWT và gọi các API chính.

Yêu cầu trước khi bắt đầu
- Backend đang chạy (dev local): http://localhost:8081
  - Khởi động bằng: `.\mvnw.cmd spring-boot:run` hoặc `java -jar target/backend-0.0.1-SNAPSHOT.jar`
- Có tài khoản test (DataInitializer tạo tự động khi backend khởi động):
  - admin / Admin@123  (ROLE_ADMIN, ROLE_USER)
  - billing / Billing@123 (ROLE_BILLING_STAFF, ROLE_USER)
  - tenant1 / Tenant@123 (ROLE_USER) — liên kết tới `NguoiThue` và phòng mẫu nếu DataInitializer chạy

Authentication (JWT)
- Login:
  - POST /api/auth/login
  - Body JSON: { "username": "admin", "password": "Admin@123" }
  - Response: { "accessToken": "...", "tokenType": "Bearer" }
- Thêm header cho các request bảo mật:
  - Authorization: Bearer <accessToken>
  
  - Lưu ý cho các action yêu cầu quyền admin:
    - Trước khi gọi các endpoint có chú thích (ROLE_ADMIN) bạn phải đăng nhập bằng tài khoản admin và thêm header Authorization: Bearer <accessToken>.
    - Trong Postman bạn có thể import `Local.postman_environment.json` và chạy request "Auth -> Login" để tự động lưu `accessToken` vào environment; collection đã cấu hình bearer auth sử dụng biến `{{accessToken}}`.

Important notes
- Nếu chạy Android emulator: use 10.0.2.2 instead of localhost (http://10.0.2.2:8081)
- FE không nên truy cập DB trực tiếp. Nếu team cần inspect DB, dùng DBeaver + kết nối Aiven (DBaaS) hoặc mysql CLI.

Các endpoint chính (tóm tắt)
- Auth
  - POST /api/auth/login
  - GET  /api/test/whoami
- Phòng (rooms)
  - GET  /api/phongs
  - POST /api/phongs (ROLE_ADMIN)
  - GET  /api/phongs/{id}
  - PUT  /api/phongs/{id}
  - DELETE /api/phongs/{id} (ROLE_ADMIN)
- Người thuê (tenants / nguoithue)
  - GET /api/tenants
  - POST /api/tenants (ROLE_ADMIN)
  - GET /api/tenants/{id}
  - PUT /api/tenants/{id}
- Sự cố (incidents / suco)
  - POST /api/incidents
  - GET  /api/incidents
  - POST /api/incidents/{id}/attachments (multipart/form-data, field: file)
    - Allowed content types: image/png, image/jpeg, image/jpg, image/gif, application/pdf
    - Max file size: 5 MB
- Khách vào/ra (guest entries)
  - POST /api/guest-entries
  - GET  /api/guest-entries
- Reports
  - GET /api/reports/revenue?groupBy=day|month&from=YYYY-MM-DD&to=YYYY-MM-DD
  - GET /api/reports/top-rooms?limit=10

- Billing async generation
  - POST /api/billing/generate-async?ky=YYYY-MM (starts invoice generation in background; returns 202 Accepted)

Billing rules & due dates (business rules)

- Payment window: tenants are expected to pay rent starting on day 4 of each month. The billing system treats day 4 as the normal payment start date for the current billing period.

- Grace / late fee: if payment is not received by day 8 of the month (grace date = 8th), a fixed late fee of 100000 VND per day will be applied for each day past the 8th. Example: payment on day 10 → 2 * 100000 VND late fee.

- Server-side rounding / penalty behavior:
  - There is also a simple penalty calculation endpoint that may apply a one-time percentage penalty (10%) when invoked via `calculatePenalty` — this is separate from the daily fixed late-fee logic.

- Warning status (optional): one day before the payment start/end the server can mark invoices as `WARNING` to show a '1 day left' notice. This is optional — FE can implement the countdown/notification client-side.

- Next-month billing: rent for the next month follows the same cycle (starts due on the 4th of next month).

Implementation notes / what FE should expect

- The backend currently supports both synchronous and asynchronous invoice generation. Typical flow:
  - Admin/staff triggers generation (sync): POST /api/billing/generate?ky=YYYY-MM — returns generated invoices.
  - Admin/staff triggers generation (async): POST /api/billing/generate-async?ky=YYYY-MM — returns 202 Accepted and runs in background.

- Draft invoices can be created or requested for a tenant (used for manual review): POST /api/billing/draft with JSON body {"tenantId": <id>, "thang": <month>, "nam": <year>}.

- Invoice details and payment history:
  - GET /api/billing/invoices?nam=YYYY&thang=M — list invoices (admins/staff can pass tenantId to filter).
  - GET /api/billing/invoices/{id} — get invoice details (FE: this endpoint returns invoice DTO; billing controller attempts to attach payment history when possible).
  - GET /api/billing/invoices/{id}/payments — list payments for an invoice.

+- Payment QR / simulate (useful for testing):
  - Create QR for invoice (admin/staff): POST /api/billing/qr/create
    - Body example:
      {
        "invoiceId": 123,
        "amount": 100000
      }
  - Simulate QR payment (test only): POST /api/billing/payments/qr/simulate
    - Body example:
      {
        "qrCode": "<paste-qrcode>",
        "externalTxnId": "txn-12345",
        "paidAmount": 100000
      }

Notes about recent small backend cleanup

- A minor internal cleanup removed an unused lookup in the billing service (no API surface change). FE behaviour and endpoints remain the same.

Ví dụ request (có thể dùng Postman / Retrofit)

- Login request body:
  {
    "username": "admin",
    "password": "Admin@123"
  }

- Create incident (SuCo) example body:
  {
    "tieuDe": "Rò nước phòng P-101",
    "moTa": "Nước rỉ từ trần",
    "ngayXayRa": "2026-04-20T10:00:00",
    "phongId": 1,
    "nguoiBao": "Le Thi A",
    "trangThai": "OPEN"
  }

- Upload attachment (multipart form):
  curl -X POST "http://localhost:8081/api/incidents/123/attachments" \
    -H "Authorization: Bearer <accessToken>" \
    -F "file=@/path/to/image.jpg"

- Create guest entry example body (use fields: ten, cmnd, sdt, phongId, loai, ghiChu):
  {
    "ten": "Nguyen Van A",
    "cmnd": "0123456789",
    "sdt": "0987123456",
    "phongId": 1,
    "loai": "IN",
    "ghiChu": "Khách thăm"
  }

Retrofit & Android notes

- Use Retrofit2 + Gson converter.
- Add an OkHttp interceptor to inject the Authorization header.
- For emulator use base URL http://10.0.2.2:8081

DB / Aiven (lưu ý)
- FE không cần kết nối DB.
- Nếu muốn inspect DB, BE/QA/DBA có thể cung cấp Aiven credentials (host/port/user/password + SSL) và dùng DBeaver.

Debug checklist (quick)
- Backend running on port 8081?
- Login returns accessToken?
- GET /api/phongs returns list with at least one room?
- tenant1 exists and is linked to a `NguoiThue` record with `sophong` set?

Nếu FE cần Postman collection, OpenAPI/Swagger hoặc ví dụ code Android cụ thể, xem thư mục `postman/` và `android_example/` hoặc yêu cầu mình export thêm.

