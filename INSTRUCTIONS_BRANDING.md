# Hướng dẫn thay thế Logo, Biểu tượng (Icon) và Âm thanh cho ViTalk

Tài liệu này hướng dẫn chi tiết cách tùy biến giao diện hình ảnh và âm thanh phản hồi của ViTalk để ứng dụng có bản sắc riêng, không bị trùng lặp với TalkBack gốc của Google.

---

## 1. Cách thay đổi Biểu tượng ứng dụng (App Icon)

Biểu tượng của ViTalk được cấu hình trong `AndroidManifest.xml` qua thuộc tính `android:icon="@drawable/icon"`. Mặc định, nó sử dụng tính năng **Adaptive Icon** (Biểu tượng thích ứng) của Android 8.0 trở lên.

### Cách 1: Thay thế bằng ảnh PNG thông thường (Đơn giản nhất)
Nếu bạn có một tệp hình ảnh logo định dạng `.png` (kích thước đề xuất là `512x512` pixel):
1. Chuẩn bị tệp ảnh logo của bạn và đặt tên là `icon.png` (lưu ý viết thường toàn bộ).
2. Truy cập thư mục phát triển: `Vitalk/talkback/src/main/res/`.
3. Xóa tệp cấu hình Vector cũ để tránh xung đột:
   * Xóa tệp `drawable-anydpi-v26/icon.xml`.
   * Xóa tệp `drawable-anydpi-v26/talkback_round.xml`.
4. Copy tệp `icon.png` mới của bạn vào thư mục `drawable/` (nếu thư mục này chưa có, hãy tạo mới nó dưới đường dẫn `Vitalk/talkback/src/main/res/drawable/`).
5. *Mẹo nâng cao:* Để biểu tượng hiển thị sắc nét trên các màn hình có mật độ điểm ảnh khác nhau, bạn có thể resize ảnh thành các kích thước tương ứng và bỏ vào các thư mục sau:
   * `drawable-mdpi/icon.png` (48x48 px)
   * `drawable-hdpi/icon.png` (72x72 px)
   * `drawable-xhdpi/icon.png` (96x96 px)
   * `drawable-xxhdpi/icon.png` (144x144 px)
   * `drawable-xxxhdpi/icon.png` (192x192 px)

### Cách 2: Tùy biến Adaptive Icon Vector (Chuẩn Google/Android hiện đại)
Adaptive Icon bao gồm 2 lớp: Nền màu (Background) và Hình vẽ tiêu điểm phía trước (Foreground Vector).
1. **Thay đổi màu nền:**
   * Mở tệp [colors.xml](file:///D:/android_app/ViTalk/Vitalk/talkback/src/main/res/values/colors.xml).
   * Tìm đến khóa `<color name="colorTalkBackIcon">#683CB7</color>` (dòng 76) và đổi mã màu Hex `#683CB7` (màu tím gốc) thành mã màu thương hiệu của bạn (ví dụ: xanh lá `#4CAF50` hoặc đen `#121212`).
2. **Thay đổi hình ảnh tiêu điểm phía trước (Foreground):**
   * Logo Vector được vẽ bằng mã XML trong tệp [talkback_round.xml](file:///D:/android_app/ViTalk/Vitalk/talkback/src/main/res/drawable-anydpi-v26/talkback_round.xml).
   * Bạn có thể thiết kế logo dạng SVG, sử dụng công cụ chuyển đổi SVG sang Android VectorDrawable (tích hợp sẵn trong Android Studio hoặc dùng trang web trực tuyến [Vector Asset Creator](https://inloop.github.io/svg2android/)).
   * Lấy mã XML Vector sinh ra, copy và ghi đè toàn bộ nội dung vào tệp [talkback_round.xml](file:///D:/android_app/ViTalk/Vitalk/talkback/src/main/res/drawable-anydpi-v26/talkback_round.xml).

---

## 2. Cách thay đổi âm thanh phản hồi (Earcons)

Khi bạn rê tay, vuốt cử chỉ hoặc nhấn đúp, ViTalk sẽ phát ra các âm thanh phản hồi dạng hiệu ứng nhạc ngắn. Các âm thanh này nằm trong thư mục tài nguyên raw:
`Vitalk/talkback/src/main/res/raw/`

Có tổng cộng 16 tệp âm thanh định dạng `.ogg`. Để thay đổi chúng:
1. Chuẩn bị các tệp âm thanh thay thế của bạn. Định dạng bắt buộc là **`.ogg`** (bạn có thể dùng GoldWave, Reaper với OSARA, hoặc Audacity để convert từ `.mp3`/`.wav` sang `.ogg`).
2. Đổi tên các tệp âm thanh mới sao cho **trùng khớp hoàn toàn** với các tệp gốc dưới đây:

| Tên tệp âm thanh gốc | Mô tả chức năng phát âm thanh |
| :--- | :--- |
| `focus.ogg` | Khi di chuyển tiêu điểm vào một mục văn bản/hình ảnh thông thường. |
| `focus_actionable.ogg` | Khi di chuyển tiêu điểm vào nút bấm, liên kết hoặc mục có thể nhấn. |
| `long_clicked.ogg` | Âm thanh phản hồi khi bạn thực hiện nhấn giữ lâu (Long Click). |
| `complete.ogg` | Hoàn thành một hành động hệ thống (ví dụ: mở thanh trạng thái). |
| `gesture_begin.ogg` | Bắt đầu vẽ cử chỉ vuốt trên màn hình. |
| `gesture_end.ogg` | Kết thúc cử chỉ vuốt. |
| `scroll_tone.ogg` | Phát ra khi bạn cuộn danh sách (trượt hai ngón tay). |
| `view_entered.ogg` | Khi chuyển sang một màn hình hoặc một cửa sổ ứng dụng mới. |
| `typo.ogg` | Phát ra khi gõ sai chính tả hoặc gặp lỗi nhập liệu. |
| `loading.ogg` | Âm thanh vòng lặp khi đang tải dữ liệu. |
| `chime_up.ogg` | Tăng tông (sử dụng khi tăng giá trị điều khiển hoặc trượt tới). |
| `chime_down.ogg` | Giảm tông (sử dụng khi giảm giá trị điều khiển hoặc trượt lùi). |
| `browse_mode_on_v4_2.ogg` | Khi kích hoạt chế độ duyệt web/chi tiết. |
| `browse_mode_off_v4_2.ogg` | Khi tắt chế độ duyệt web/chi tiết. |
| `tick.ogg` | Âm thanh tích tắc nhẹ phục vụ việc đếm hoặc phản hồi phụ. |
| `volume_beep.ogg` | Âm báo điều chỉnh âm lượng trợ năng. |

3. Copy đè toàn bộ các tệp `.ogg` mới vào thư mục `Vitalk/talkback/src/main/res/raw/`.
4. Biên dịch lại dự án bằng Gradle.

---

## 3. Cách biên dịch sau khi thay đổi

Sau khi đã thay đổi logo hoặc âm thanh, bạn chỉ cần thực hiện biên dịch lại để xuất ra file APK mới:
1. Mở cửa sổ dòng lệnh (PowerShell/CMD) tại thư mục `Vitalk/`.
2. Chạy lệnh build:
   ```powershell
   .\gradlew.bat assemblePhoneDebug
   ```
3. Sau khi màn hình báo `BUILD SUCCESSFUL`, tệp APK hoàn chỉnh nằm ở:
   `Vitalk/build/outputs/apk/phone/debug/Vitalk-phone-debug.apk`
4. Copy file APK này ra điện thoại để cài đặt và tận hưởng bộ giao diện & âm thanh độc quyền của ViTalk!
