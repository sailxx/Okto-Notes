<div align="center">

# ● okto notes

### Лучшее приложение для заметок и дневника на Android

Пиши мысли, веди дневник, отмечай настроение — быстро, красиво и без лишнего.

<br>

[![Скачать APK](https://img.shields.io/badge/Скачать_APK-v1.1-C8F560?style=for-the-badge&logo=android&logoColor=111111&labelColor=141414)](https://github.com/sailxx/Okto-Notes/releases/latest)

![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=141414)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=141414)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white&labelColor=141414)

![Офлайн](https://img.shields.io/badge/работает-офлайн-EDEDED?style=for-the-badge&labelColor=262626)
![Без рекламы](https://img.shields.io/badge/реклама-нет-EDEDED?style=for-the-badge&labelColor=262626)
![Без аккаунтов](https://img.shields.io/badge/аккаунт-не_нужен-EDEDED?style=for-the-badge&labelColor=262626)
![Размер](https://img.shields.io/badge/размер-1.8_МБ-EDEDED?style=for-the-badge&labelColor=262626)

</div>

---

## ✨ Почему Okto Notes — лучшее приложение для заметок

| | |
|---|---|
| ⚡ **Мгновенно** | Открыл — пишешь. Автосохранение на каждой букве, никаких кнопок «Сохранить». |
| 📔 **Дневник** | Записи по дням, оценка настроения, серия дней подряд и тепловая карта за 4 недели. |
| 🎨 **Три темы** | Цветная, графитовая Okto и **своя** — собери тему из любого цвета. |
| 🔒 **Приватно** | Всё хранится только на телефоне. Ни облака, ни аккаунтов, ни трекеров. |
| 🪶 **Легко** | 1.8 МБ, без интернета, без рекламы. |

## 🎨 Темы

<table>
<tr>
<td width="33%" valign="top">

### Цветная
Основная тема. Мягкие пастельные карточки в две колонки, недельная полоса дневника и большая кнопка «Новая».

</td>
<td width="33%" valign="top">

### Okto
Графит в стиле [Okto](https://sailxx.github.io/Okto/): табло с LCD-цифрами, объёмные клавиши, моноширинные подписи и часы в шапке.

</td>
<td width="33%" valign="top">

### Своя
Выбери оформление (Цветное или Okto), светлый или тёмный режим и акцент из 12 цветов — или подбери оттенок ползунком.

</td>
</tr>
</table>

## 📝 Возможности

- **Заметки** — заголовок, текст, 7 цветных меток, избранное (долгое нажатие на карточку), поиск.
- **Дневник** — запись на каждый день, настроение по шкале 1–5, серия без пропусков, график настроения за неделю.
- **Редактор** — быстрые вставки: `• список`, `☐ задача`, текущее время; счётчик слов.
- **Отмена удаления** — случайно удалил? Кнопка «Вернуть» спасёт.
- **Шрифты** — Nunito, Golos Text и JetBrains Mono встроены и поддерживают кириллицу (лицензия SIL Open Font License 1.1).

## 📲 Установка

1. Скачай `OktoNotes.apk` со страницы [релизов](https://github.com/sailxx/Okto-Notes/releases/latest).
2. Открой файл на телефоне и разреши установку из неизвестных источников.
3. Готово — пиши первую заметку ✍️

## 🛠 Сборка из исходников

Нужны JDK 17+ и Android SDK (platform 35).

```bash
./gradlew assembleRelease
```

APK появится в `app/build/outputs/apk/release/`.

<details>
<summary><b>Структура проекта</b></summary>

```
app/src/main/java/com/okto/notes/
├── MainActivity.kt        — точка входа, применение темы
├── OktoViewModel.kt       — состояние, автосохранение, серия дней
├── data/
│   ├── Entry.kt           — модель заметки/записи дневника
│   ├── Store.kt           — хранение в JSON во внутренней памяти
│   └── ThemeSettings.kt   — выбранная тема
└── ui/
    ├── Theme.kt           — палитры, генерация своей темы, шрифты
    ├── Components.kt      — клавиши, табло, навигация
    ├── NotesTab.kt        — экран заметок
    ├── DiaryTab.kt        — экран дневника
    ├── EditorScreen.kt    — редактор
    └── SettingsScreen.kt  — настройки и конструктор темы
```

</details>

---

<div align="center">

Сделано с ❤️ для тех, кто любит записывать

</div>
