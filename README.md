# مدیریت کافی نت (CAFE MANAGER)

اپلیکیشن Android برای مدیریت کافی‌نت؛ مناسب همه کافی‌نت‌داران. Kotlin + Jetpack Compose + Room، فارسی و RTL، آفلاین، بدون هزینه.

## وضعیت: مرحله ۱
هسته برنامه، Setup Wizard، قفل PIN، داشبورد، مدیریت مشتریان، Activity Log، حالت تاریک/روشن، تنظیمات کافی‌نت.

## ساخت APK
- **GitHub Actions:** با هر Push به شاخه `main` فایل Debug APK ساخته می‌شود (Actions ← آخرین اجرا ← Artifacts).
- **محلی:** نیازمند JDK 17 و Android SDK؛ دستور `gradle :app:assembleDebug` (یا بعد از `gradle wrapper` از `./gradlew`).

## Release و Keystore
امضای Release از متغیرهای محیطی `KEYSTORE_PATH`، `KEYSTORE_PASSWORD`، `KEY_ALIAS` و `KEY_PASSWORD` خوانده می‌شود.
Keystore را هرگز در GitHub قرار ندهید و از آن یک Backup امن نگه دارید؛ با گم‌شدن آن، نسخه‌های بعدی با همان امضا قابل انتشار نیستند.
