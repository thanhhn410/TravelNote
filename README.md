# TravelNote

Ứng dụng Android ghi chi tiêu du lịch theo từng chuyến đi, quy đổi tiền tệ theo tỷ giá Vietcombank.

## Tính năng

- Quản lý theo **chuyến đi**, không gom theo tháng
- 50 đơn vị tiền tệ, tỷ giá lấy trực tiếp từ bảng tỷ giá chuyển khoản Vietcombank
- Nạp tiền (top-up) cho từng chuyến, theo dõi số còn lại
- 8 danh mục chi tiêu: di chuyển, ăn uống, lưu trú, vé và vui chơi, mua sắm, quà cáp, sức khỏe và bảo hiểm, khác
- Xuất Excel (.xlsx) gồm 3 sheet: tổng quan, chi tiêu, nạp tiền
- Sao lưu và phục hồi toàn bộ dữ liệu bằng file JSON để chuyển sang máy khác
- Dark mode, hỗ trợ Material You trên Android 12+
- Không quảng cáo, không tài khoản, dữ liệu nằm hoàn toàn trên máy

## Yêu cầu

- Android 8.0 (API 26) trở lên
- JDK 17 và Android SDK nếu build tại máy

## Cách 1: Build APK bằng GitHub Actions (không cần cài gì)

1. Tạo repo mới trên GitHub, push toàn bộ thư mục này lên nhánh `main`
2. Vào tab **Actions**, workflow `Build APK` sẽ tự chạy
3. Khi chạy xong, tải file trong mục **Artifacts** → `TravelNote-apk`
4. Giải nén, copy APK sang điện thoại và cài (bật "Cài đặt ứng dụng không rõ nguồn gốc")

## Cách 2: Build tại máy

```bash
# Mở thư mục bằng Android Studio rồi bấm Run, hoặc dùng dòng lệnh:
gradle wrapper --gradle-version 8.9
./gradlew assembleRelease
# APK nằm ở app/build/outputs/apk/release/
```

## Ghi chú kỹ thuật

- Dữ liệu lưu bằng SQLite thuần (không dùng Room) để giảm dung lượng và thời gian build
- File Excel được ghi trực tiếp theo chuẩn OOXML thay vì dùng Apache POI, giúp APK giữ ở mức khoảng 6–8 MB
- Tỷ giá: các đồng Vietcombank niêm yết lấy trực tiếp; các đồng còn lại quy đổi chéo qua USD và chỉ mang tính tham khảo
- Bản release được ký bằng debug key để cài trực tiếp. Nếu muốn phát hành chính thức, cần tạo keystore riêng và cập nhật `signingConfigs`
