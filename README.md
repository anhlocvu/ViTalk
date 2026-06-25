# ViTalk - Trình đọc màn hình Android tối ưu cho người Việt

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](#)

**ViTalk** là một trình đọc màn hình (Screen Reader) mã nguồn mở dành cho hệ điều hành Android, được phát triển kế thừa và tùy biến sâu từ dự án mã nguồn gốc **Google TalkBack**. Dự án được xây dựng nhằm mang lại trải nghiệm hỗ trợ tiếp cận (Accessibility) chuyên nghiệp, tối ưu hóa tối đa và cá nhân hóa sâu sắc dành riêng cho người khiếm thị Việt Nam.

Dự án được phát triển bởi **Technology Entertainment**, dưới sự dẫn dắt của nhà sáng lập **Lc_Boy (Vũ Anh Lộc)**.

---

## 🌟 Các tính năng nổi bật của ViTalk

So với TalkBack gốc của Google, ViTalk mang lại những cải tiến vượt trội được thiết kế riêng theo nhu cầu thực tế của người dùng khiếm thị Việt Nam:

1. **Giao diện tối tương phản cao (`ViTalkLauncherActivity`):**
   * Xuất hiện trực tiếp ngoài màn hình ứng dụng dưới dạng biểu tượng độc lập (không còn là dịch vụ ẩn trong cài đặt hệ thống).
   * Giao diện tối giản với các tone màu tối sâu (`#121212`, `#1E1E1E`) kết hợp text màu trắng và điểm nhấn xanh lá cây tương phản cao.
   * Các nút bấm điều hướng siêu lớn (chiều cao 56dp+, padding rộng) giúp việc rê quét và nhấp đúp dễ dàng.
   * Loại bỏ hoàn toàn các biểu tượng cảm xúc (emoji) trong văn bản giao diện để tránh việc công cụ đọc (TTS) phát âm thừa thãi.

2. **Tiếng Việt làm ngôn ngữ mặc định:**
   * Việt hóa sâu toàn bộ các nhãn nút và cài đặt trợ năng bằng ngôn ngữ thuần Việt thân thiện (ví dụ: đổi *"Khám phá bằng cách chạm"* thành *"Rê ngón tay đọc màn hình"*, *"Nhấn một lần để kích hoạt"* thành *"Kích hoạt bằng một chạm"*).
   * Thiết lập tiếng Việt làm mặc định của tài nguyên hệ thống, tự động hiển thị và đọc to tiếng Việt chuẩn trên mọi thiết bị ngay cả khi ngôn ngữ hệ thống là tiếng Anh.

3. **Vô hiệu hóa triệt để màn hình hướng dẫn cử chỉ rườm rà (Tutorial & Onboarding):**
   * Loại bỏ hoàn toàn màn hình hướng dẫn cử chỉ vuốt tự khởi động khi người dùng bật dịch vụ ViTalk lần đầu tiên. Tránh việc người khiếm thị bị kẹt ở màn hình hướng dẫn bắt buộc nhấn *"tiếp theo"* và *"hoàn tất"*.

4. **Tối ưu hóa tốc độ đọc văn bản:**
   * Mặc định tắt tính năng thông báo định dạng văn bản (đậm, nghiêng, gạch chân) và không tự động thay đổi độ cao tông giọng khi gặp chữ định dạng, giúp việc nghe văn bản đọc liên tục, mượt mà và tập trung hơn.

5. **Hoạt động hoàn toàn độc lập:**
   * Thay đổi Package Name mặc định thành `com.technologyentertainment.vitalk`. Ứng dụng có thể cài đặt song song trực tiếp với TalkBack gốc của Google mà không gây xung đột hệ thống.

---

## 🛠️ Hướng dẫn cách biên dịch và phát triển

Dự án sử dụng bộ Gradle Wrapper để biên dịch trực tiếp trên môi trường Windows hoặc Linux thông qua CLI.

### Yêu cầu hệ thống:
* **Java Development Kit (JDK):** Yêu cầu JDK 17 hoặc 21.
* **Android SDK:** Đã cấu hình biến môi trường `ANDROID_HOME`.

### Các bước biên dịch:
1. Mở cửa sổ dòng lệnh (PowerShell hoặc Command Prompt) tại thư mục chứa mã nguồn (`source/`).
2. Chạy lệnh dọn dẹp và biên dịch tệp APK dạng debug cho điện thoại:
   ```powershell
   .\gradlew.bat assemblePhoneDebug
   ```
3. Sau khi quá trình biên dịch báo `BUILD SUCCESSFUL`, tệp APK hoàn chỉnh sẽ được xuất ra tại:
   `source/build/outputs/apk/phone/debug/Vitalk-phone-debug.apk`

---

## 🎨 Hướng dẫn thay thế Logo, Icon và Âm thanh phản hồi

Để thay thế biểu tượng ứng dụng và các tệp âm thanh hiệu ứng (earcons) của riêng bạn nhằm tạo nên một bản build mang thương hiệu cá nhân độc quyền, vui lòng tham khảo tài liệu hướng dẫn chi tiết tại:
👉 **[INSTRUCTIONS_BRANDING.md](/INSTRUCTIONS_BRANDING.md)**

---

## 📄 Giấy phép (License)

Dự án ViTalk được phân phối dưới giấy phép **Apache License 2.0**. Mọi quyền kế thừa và mã nguồn gốc thuộc về **Google LLC** và các nhà đóng góp mã nguồn mở Android.

---

## 🤝 Liên hệ & Đóng góp

* **Đơn vị phát triển:** Technology Entertainment
* **Trưởng dự án:** Lc_Boy (Vũ Anh Lộc)
* **Email:** [lcboy.entertainment@gmail.com](mailto:lcboy.entertainment@gmail.com)

*ViTalk - Trình đọc màn hình thân thiện và tối ưu nhất cho người khiếm thị Việt Nam!*
