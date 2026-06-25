## ViTalk, trình đọc màn hình android tối ưu cho người việt

hãy cùng tôi làm một trình đọc màn hình tối ưu cho người việt nhé, chúngta sẽ dùng nền tảng androi cli

lưu ý, kích hoạt những skill về androi, cái nào liên quan đến androi, kích hoạt hết. đặt biệt là mobile-design và mobile-developer có thể cho phép làm giao diện và phát triển tốt hơn, 

tôi đã đặt thư mục talkback trong thư mục này.
hãy đọc nó vì nó là ý tưởng vàng để bạn có thể code trình đọc màn hình
nếu muốn khởi tạo dự án thì nên khởi tạo vào thư mục con, như: Vitalk, như thế, nó sẽ có hai thư mục: talkback dùng để đọc và tham khảo cách viết trình đọc màn hình. còn Vitalk là thư mục phát triển

---

# Nhật ký phát triển (Development Logs)

### Phiên làm việc: 24/06/2026 - Chuyển sang phát triển dựa trên TalkBack gốc

Theo thống nhất và yêu cầu từ người dùng, chúng ta đã chuyển đổi chiến lược phát triển sang **Hướng 1**: Kế thừa toàn bộ mã nguồn trình đọc màn hình gốc **TalkBack của Google** để có ngay 100% tính năng hỗ trợ tiếp cận chuyên nghiệp (điều hướng cử chỉ chuẩn, tự động cuộn trang, nhấp đúp hệ thống và chữ nổi Braille) và thực hiện Việt hóa, tùy biến thương hiệu thành **ViTalk**.

Các hạng mục đã hoàn thành trong phiên làm việc:

1. **Đồng bộ và thiết lập thư mục phát triển `Vitalk`:**
   * Sử dụng Git để sao chép sạch sẽ toàn bộ dự án từ thư mục `talkback` gốc sang thư mục phát triển `Vitalk` nhằm tránh lỗi lock file trên Windows.
   * Khởi tạo bộ Gradle Wrapper mới (`gradlew`, `gradlew.bat` và thư mục `gradle`) cho dự án `Vitalk` bằng cách sao chép từ một template Android Compose trống được tạo tạm thời qua công cụ `android-cli`.

2. **Việt hóa đồng loạt và đổi thương hiệu (Branding):**
   * Sử dụng tập lệnh Python để quét và thay thế tự động toàn bộ từ khóa `"TalkBack"` thành `"ViTalk"` (và `"talkback"` thành `"vitalk"`) trong các tệp tài nguyên chuỗi tiếng Việt (`res/values-vi/strings.xml`) và tài nguyên mặc định (`res/values/strings.xml`).
   * **Thiết lập tiếng Việt làm mặc định:** Sao chép và ghi đè toàn bộ tài nguyên tiếng Việt đã được sửa thương hiệu vào file tài nguyên mặc định (`res/values/strings.xml`) của cả hai mô-đun `talkback` và `utils`. Việc này đảm bảo dù thiết bị thiết lập ngôn ngữ nào, ViTalk vẫn tự động hiển thị và đọc to tiếng Việt chuẩn cho người dùng khiếm thị Việt Nam.

3. **Kiểm tra biên dịch thử nghiệm:**
   * Kích hoạt tiến trình biên dịch thử nghiệm ứng dụng mới tạo bằng Gradle wrapper (`.\gradlew.bat assembleDebug`) để đảm bảo dự án chạy ổn định và xuất ra tệp APK ViTalk hoàn chỉnh.

### Phiên làm việc: 24/06/2026 (Tiếp tục) - Khắc phục lỗi build NDK và Resource, biên dịch thành công APK

1. **Khắc phục lỗi khoảng trắng đường dẫn của NDK:**
   * Lỗi: Do đường dẫn dự án chứa khoảng trắng (`D:\android app\ViTalk\`), bộ công cụ Android NDK build (Make) bị lỗi không nhận diện được file cấu hình `Android.mk`.
   * Giải pháp: Tạo thư mục phát triển trung gian không chứa khoảng trắng là `D:\ViTalkDevelop`, sử dụng `robocopy` để đồng bộ mã nguồn sang đó và thực hiện build tại đây.

2. **Sửa lỗi biên dịch tài nguyên (Resource Compilation Error):**
   * Lỗi: Script đổi tên trước đó đã vô tình thay đổi cả tên khóa tài nguyên (Resource ID) trong XML (ví dụ: đổi `shortcut_talkback_breakout` thành `shortcut_vitalk_breakout`), làm đứt gãy các tham chiếu mặc định trong mã nguồn Java/Kotlin và các ngôn ngữ khác, dẫn đến lỗi `:processPhoneDebugResources FAILED`.
   * Giải pháp: 
     * Khôi phục (git restore) các tệp strings gốc để đưa về trạng thái sạch.
     * Viết và thực thi một script Python Rebrand thông minh mới: chỉ thay thế chuỗi hiển thị (value) từ `"TalkBack"` thành `"ViTalk"` và `"talkback"` thành `"vitalk"`, giữ nguyên tuyệt đối các Resource ID (key) và các tham chiếu dạng `@string/talkback_...` để đảm bảo tương thích mã nguồn.
     * Đồng thời gộp (merge) thông minh các bản dịch tiếng Việt vào file tài nguyên mặc định (`values/strings.xml`) để giữ tiếng Việt làm mặc định nhưng vẫn có fallback an toàn cho các key chưa dịch, tránh lỗi resource không tìm thấy.

3. **Biên dịch thành công APK ViTalk:**
   * Chạy thành công lệnh Gradle build (`.\gradlew.bat assembleDebug`) trong `D:\ViTalkDevelop`.
   * Xuất ra file APK điện thoại hoàn chỉnh: `ViTalkDevelop-phone-debug.apk`.
   * Copy file APK thành phẩm về thư mục gốc của dự án tại [ViTalk-debug.apk](file:///D:/android%20app/ViTalk/ViTalk-debug.apk) để người dùng cài đặt và thử nghiệm thực tế.

### Phiên làm việc: 25/06/2026 - Tùy biến sâu, đổi package name doanh nghiệp và tích hợp giao diện tối độc lập (Launcher Activity)

1. **Thay đổi Nhận diện & Package Name doanh nghiệp:**
   * Cập nhật `talkbackApplicationId` trong `shared.gradle` thành `com.technologyentertainment.vitalk` theo đúng tên công ty **Technology Entertainment** của người dùng. Việc này giúp ứng dụng hoạt động hoàn toàn độc lập, cài đặt song song và không bị xung đột với TalkBack gốc của Google.
   * Cập nhật giá trị chuỗi hiển thị mặc định `talkback_title` từ `ViTalk_TfP` thành `ViTalk` để đảm bảo tên hiển thị của ứng dụng trên hệ thống Android chuẩn xác nhất.

2. **Xây dựng Giao diện Tối độc lập (ViTalk Launcher Activity):**
   * **Thiết kế màu sắc tối giản & tương phản cao:** Định nghĩa tài nguyên màu mới `colors_vitalk.xml` gồm các tone màu tối huyền bí như nền đen sâu (`#121212`), thẻ xám đậm (`#1E1E1E`), cùng text màu trắng và điểm nhấn xanh lá cây (`#4CAF50`) tương phản rõ nét để tối ưu hóa khả năng tiếp cận.
   * **Bố cục giao diện:** Tạo layout `activity_vitalk_launcher.xml` gồm 4 mục điều hướng chính có kích thước nút bấm lớn (cao 56dp+, padding rộng rãi):
     - *Trạng thái ViTalk* (Hiển thị Bật/Tắt dịch vụ trợ năng, nhấp vào sẽ chuyển sang cài đặt Trợ năng hệ thống).
     - *Cài đặt Giọng nói* (Chuyển sang cài đặt Text-to-Speech hệ thống).
     - *Cài đặt ViTalk Nâng cao* (Mở trang cấu hình chi tiết gốc).
     - *Giới thiệu ViTalk* (Mở hộp thoại dialog giới thiệu thông tin nhà phát triển Vũ Anh Lộc và công ty Technology Entertainment).
   * **Triển khai Java logic:** Viết code class `ViTalkLauncherActivity.java` điều hướng các Intent chuẩn xác, quản lý động trạng thái dịch vụ trợ năng ViTalk trong `onResume()` để cập nhật trực quan khi người dùng quay lại từ màn hình cài đặt hệ thống.
   * **Đăng ký Launcher chính:** Đăng ký activity mới làm điểm khởi động (`LAUNCHER`) trong `AndroidManifest.xml` giúp ViTalk xuất hiện trực tiếp ngoài màn hình chính dưới dạng biểu tượng ứng dụng thay vì là dịch vụ ẩn.
   * **Tích hợp sâu cài đặt trợ năng:** Thay đổi thuộc tính `android:settingsActivity` của service trợ năng ViTalk thành `ViTalkLauncherActivity` mới trên 7 tệp XML cấu hình `accessibilityservice.xml`. Điều này đảm bảo dù người dùng mở cài đặt ViTalk từ Cài đặt trợ năng của hệ thống thì vẫn được chuyển về giao diện tối đồng bộ.

3. **Việt hóa sâu các nhãn cài đặt (Preferences Translation):**
   * Thay thế đồng loạt các nhãn dịch tiếng Việt gốc của Google trong `strings.xml` sang cách diễn đạt thuần Việt, thân thiện và chuyên nghiệp hơn (ví dụ: đổi "Khám phá bằng cách chạm" thành "Rê ngón tay đọc màn hình", "Nhấn một lần để kích hoạt" thành "Kích hoạt bằng một chạm", v.v.) giúp giao diện ViTalk mang phong cách đặc trưng và khác biệt hoàn toàn với TalkBack gốc.

4. **Biên dịch thành công APK ViTalk hoàn chỉnh:**
   * **Khắc phục lỗi nhãn gói trên trình cài đặt (Package Installer):** Thêm thuộc tính `android:label="@string/talkback_title"` (giá trị **ViTalk**) và `android:icon="@drawable/icon"` vào thẻ `<application>` trong tệp `AndroidManifest.xml` của cả mô-đun chính và mô-đun con `talkback`. Việc này giúp trình cài đặt Android hiển thị tên ứng dụng rõ ràng là **ViTalk** và biểu tượng app trong quá trình cài đặt thay vì hiển thị tên gói kỹ thuật `com.technologyentertainment.vitalk`.
   * Chạy thành công tiến trình Gradle build (`.\gradlew.bat assemblePhoneDebug`) để xuất ra tệp APK hoàn chỉnh tại [ViTalk-debug.apk](file:///D:/android_app/ViTalk/ViTalk-debug.apk).

5. **Tinh chỉnh sâu và khắc phục các vấn đề trải nghiệm người dùng khiếm thị:**
   * **Loại bỏ hoàn toàn biểu tượng cảm xúc (emoji) trên giao diện chính:** Gỡ bỏ các ký tự biểu tượng (`⚡`, `🗣`, `⚙`, `ℹ`) ở các tiêu đề nút bấm trong tệp layout `activity_vitalk_launcher.xml`. Việc này giúp các công cụ đọc màn hình (TTS) không đọc kèm từ khóa "biểu tượng cảm xúc: ..." gây mất thời gian và khó chịu cho người khiếm thị khi rê quét qua các nút.
   * **Thay thế mô tả dịch vụ trợ năng hệ thống:** Đổi mô tả hướng dẫn cử chỉ gốc của Google thành khẩu hiệu chính thức của ứng dụng: *"ViTalk, trình đọc màn hình thân thiện với người Việt"* trên cả hai tệp tài nguyên mặc định (`values/strings.xml`) và tài nguyên tiếng Việt (`values-vi/strings.xml`) cho các khóa `talkback_service_description`, `talkback_service_html_description` và `talkback_service_summary`.
   * **Việt hóa thông báo bật/tắt của hệ thống phản hồi (Compositor):** Chỉnh sửa tệp tài nguyên `strings_compositor.xml` (cả bản mặc định và bản tiếng Việt), đổi các chuỗi thông báo phát âm trạng thái dịch vụ từ "TalkBack on / Bật TalkBack" thành **"ViTalk đã bật"**, và từ "TalkBack off / TalkBack tắt" thành **"ViTalk đã tắt"**.
   * **Tắt hoàn toàn hướng dẫn sử dụng tự khởi động (Tutorial/Onboarding):** Chỉnh sửa class Java `TalkBackService.java`, sửa đổi phương thức `shouldShowTutorial()` để luôn trả về `false`. Việc này giúp chặn đứng màn hình hướng dẫn vuốt cử chỉ tự động hiện ra gây phiền toái khi người dùng bật dịch vụ ViTalk lần đầu tiên.

### Phiên làm việc: 25/06/2026 (Tiếp tục) - Khắc phục hoàn toàn mô tả hệ thống, vô hiệu hóa triệt để hướng dẫn sử dụng và định dạng giọng đọc

1. **Khắc phục lỗi cú pháp trong `OnboardingInitiator.java`:**
   * Sửa lỗi cú pháp của hàm `showOnboardingIfNecessary` bị ngắt quãng từ phiên trước, trả về `false` ngay lập tức để bỏ qua hoàn toàn màn hình giới thiệu tính năng mới (Onboarding) và đảm bảo dự án biên dịch ổn định.

2. **Vô hiệu hóa triệt để hướng dẫn sử dụng (Tutorial):**
   * Làm rỗng hoàn toàn thân hàm `showTutorial()` trong tệp `TalkBackService.java` để ngăn chặn tuyệt đối dịch vụ tự động kích hoạt màn hình hướng dẫn cử chỉ.

3. **Mặc định tắt tính năng thông báo định dạng văn bản (Text Formatting):**
   * Đổi giá trị mặc định của `pref_formatting_inline_default` trong `donottranslate.xml` thành `false`.
   * Đặt tất cả các khóa con mặc định của định dạng chữ gồm: `bold`, `italic`, `underline`, `strikethrough`, `text_color_name`, `text_size` và `font_family` thành `false` nhằm tắt hoàn toàn tính năng thay đổi độ cao tông giọng đọc hoặc phát âm các mốc định dạng ("bắt đầu đậm", "kết thúc nghiêng") khi đọc văn bản, giúp quá trình nghe của người khiếm thị được liên tục và mượt mà nhất.

4. **Loại bỏ chữ "ViTalk" lặp lại trên giao diện chính:**
   * Sửa đổi tệp layout `activity_vitalk_launcher.xml` để rút gọn nhãn nút bấm:
     - Đổi "KÍCH HOẠT VITALK" thành "TRẠNG THÁI DỊCH VỤ".
     - Đổi "CẤU HÌNH VITALK NÂNG CAO" thành "CÀI ĐẶT NÂNG CAO".
     - Đổi "GIỚI THIỆU VITALK" thành "GIỚI THIỆU".
     - Cập nhật mô tả nút kích hoạt ngắn gọn: "Nhấn để mở Cài đặt Trợ năng hệ thống".

5. **Khắc phục triệt để lỗi hiển thị mô tả mặc định gốc chứa chữ "TalkBack":**
   * **Nguyên nhân:** Trên các phiên bản Android mới (11+), Google đã tách cấu hình trợ năng cho Phone thành các tệp XML riêng (`xml-v30/accessibilityservice.xml`, `xml-v31/accessibilityservice.xml`, `xml-v33/accessibilityservice.xml`) nhưng lại bỏ quên thuộc tính `android:description`. Điều này khiến Android OS tự động chèn phần hướng dẫn cử chỉ mặc định của hệ thống có chứa từ "TalkBack" (như "Cách dùng TalkBack", "Vuốt sang phải hoặc sang trái...") dưới tên dịch vụ.
   * **Giải pháp:** Thêm thuộc tính `android:description="@string/talkback_service_description"` vào cả 3 tệp cấu hình XML nói trên để ép hệ thống Android sử dụng chuỗi Việt hóa thương hiệu `"ViTalk, trình đọc màn hình thân thiện với người Việt"`.

6. **Việt hóa thông báo bật/tắt ở mức mặc định (Fallback):**
   * Chỉnh sửa file tài nguyên mặc định `strings_compositor.xml` (fallback), đổi các chuỗi `talkback_on` và `talkback_disabled` thành tiếng Việt ("ViTalk đã bật" / "ViTalk đã tắt") để đồng bộ trải nghiệm phản hồi giọng nói kể cả khi hệ thống gặp lỗi tải gói ngôn ngữ.

7. **Biên dịch và xuất APK hoàn chỉnh:**
   * Chạy Gradle biên dịch thành công APK hoàn chỉnh.
   * Người dùng tự thực hiện đổi icon và chạy build thành công bản APK mới vào lúc 11:54 ngày 25/06/2026.
   * Sao chép APK thành phẩm mới nhất về thư mục gốc tại [ViTalk-debug.apk](file:///D:/android_app/ViTalk/ViTalk-debug.apk) để người dùng tiến hành cài đặt thử nghiệm thực tế.

8. **Cung cấp tài liệu hướng dẫn thay thế Logo, Icon và Âm thanh:**
   * Tạo tệp hướng dẫn chi tiết [INSTRUCTIONS_BRANDING.md](file:///D:/android_app/ViTalk/INSTRUCTIONS_BRANDING.md) hướng dẫn cách thay đổi tệp icon vector, đổi màu nền icon, thay thế bằng ảnh PNG thông thường và danh sách 16 tệp âm thanh phản hồi dạng `.ogg` trong thư mục tài nguyên raw để người dùng chủ động tùy biến.

9. **Chuẩn bị cấu trúc thư mục đăng lên GitHub:**
   * Đổi tên thư mục phát triển chính từ `Vitalk` sang `source`.
   * Cấu hình tệp `.gitignore` ở thư mục gốc để tự động bỏ qua thư mục tham khảo `talkback/` khi đăng lên GitHub, đồng thời cập nhật các đường dẫn bỏ qua của thư mục `source/` mới.
   * Cập nhật các đường dẫn tham chiếu trong tệp `README.md` gốc để trỏ chính xác vào thư mục `source/` mới.


