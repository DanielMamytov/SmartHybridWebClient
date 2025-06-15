##  Возможности

- **JS ↔ Kotlin связь**
    - Вызов Android-методов из JavaScript (`showToast`, `log`, `notifyUploadSuccess`)
    - Инжекция JS-скриптов из Kotlin через `evaluateJavascript`

-  **Загрузка файлов из WebView**
    - `<input type="file">` открывает системный диалог выбора
    - Имя выбранного файла отправляется обратно в JS

-  **Offline fallback**
    - Если нет интернета, отображается `offline.html` из `assets`
    - При восстановлении соединения WebView автоматически обновляется

-  **GitHub OAuth авторизация**
    - Перехват редиректа на `redirect_uri`
    - Извлечение OAuth-кода для дальнейшего обмена на токен

-  **Live-проверка подключения**
    - Используется `ConnectivityManager.registerDefaultNetworkCallback`
    - Автоматическая перезагрузка, если сеть восстановлена

-  **Поддержка "назад" через OnBackPressedDispatcher**
    - Навигация по истории WebView без deprecated методов
