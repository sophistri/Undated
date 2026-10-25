# Undated

**A retro task and calendar organiser to track tasks that both have and lack deadlines.**

Most to-do apps force a due date on everything. Most calendars only know about dates. Undated is both: a Google-Calendar-style schedule for what is dated, and a quiet **Anytime** shelf for what isn't, with events that can carry their own subtasks (an exam with the modules you still have to study, for example).

Built for **Syntax Error 2026** (themeless track). Android, Kotlin, Jetpack Compose.

---

## Features

### Five tabs

| Tab | What it does |
|-----|--------------|
| **HOME** | A schedule like Google Calendar's: a header per day, cards underneath, and a floating `ADD ON "DATE" +` button that follows the day you are scrolled to. A collapsible **Anytime** section holds your undated tasks. |
| **TASKS** | The general list: add, tick, edit and delete tasks through a popup. |
| **CATS** | Everything grouped by category, collapsible with `[+]` / `[-]`. Create and delete your own categories with custom colors. |
| **CAL** | A month grid with dots for tasks (squares) and events (circles). Tap a day to see what is on it. |
| **STATS** | A snapshot of where you stand: completion rate, open tasks by timing (overdue, this week, later, no date), by category and by priority. |

### Tasks

- Due dates are optional. A task can have no date, a single day, or a range such as "second week of October".
- Priority (none, low, medium, high), category, and an optional per-task colour that overrides the category colour.
- Everything is editable: tap a task's title to change it.

### Events with subtasks

- Create your own events with a date range and a category.
- Each event can have subtasks. An *Exam* event can hold *Module 1*, *Module 2*, *Module 3*, with a progress bar and an `x/y` count.
- Event cards collapse to a single line so they take almost no space, and expand to show the subtasks.
- Subtasks live inside their event, so a study plan never floods your general task list.

### Google Calendar import (read-only)

- Reads the calendars already synced to the phone (Google Calendar, including birthdays and holidays) through Android's `CalendarContract`. No Google sign-in, no network access.
- Imported events appear on HOME and CAL with their calendar's colour.
- `+ Event` copies one into the app as your own event, so you can add subtasks, change its category or edit it. The original is then hidden to avoid duplicates, and it reappears automatically if you delete your copy.

### Home-screen widgets

- **Task list widget:** your open tasks at a glance, tap to open the app.
- **Month widget:** the current month with today highlighted, days that have open tasks filled in, and days with events underlined.
- Both refresh whenever you change anything in the app.

### A distinct look

- Retro desktop-window styling.
- Palette: Apple Blossom `#AE4948`, Dark Peach `#D48354`, Lunar Green `#324D41`, on warm cream. Light and dark themes.
- Monospace type, and an original pixel-art sunflower (drawn in code from a 17×17 grid) used for the animated splash screen, the empty states and the launcher icon.

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 (heavily customised) |
| Storage | Room (SQLite) with KSP |
| Widgets | Jetpack Glance |
| Calendar import | `CalendarContract` content provider |
| State | `AndroidViewModel` exposing `StateFlow`s that come straight from Room `Flow`s |
| Min SDK | 26 (Android 8.0) |

---

## Getting started

1. Clone the repo and open it in Android Studio (a recent version).
2. Let Gradle sync finish.
3. Connect an Android phone with USB debugging on (or use an emulator) and press **Run**.

**To use the widgets:** long-press the home screen, choose **Widgets**, and pick **Undated**.

**To see Google events:** open HOME or CAL, tap **IMPORT EVENTS**, and allow calendar access. Your calendar app must be signed in and synced. For birthdays and holidays, turn those calendars on in the Google Calendar app first.

---

## Permissions and privacy

- Only `READ_CALENDAR`, and only when you tap the import button.
- Calendar data is read-only and never copied unless you press `+ Event`.
- There is **no** `INTERNET` permission: no accounts, no network calls, no analytics. Everything stays on the device.
