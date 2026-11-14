# Christmas Town (Android)

A cute chibi pixel-art game made for Lexi (a kid) and her dad: you're an elf working for Santa. Kotlin + Jetpack Compose, one Gradle module.

- `assets/sprites.txt`: ALL the pixel art. Each sprite is `@name` followed by rows of letters; each letter is a color from the `=x RRGGBB` palette at the top, `.` is see-through. Capital letters are recolored per elf (S skin, Z skin shadow, H hair, J hair shadow, O outfit, Q outfit shadow, T hat, E eyes). Elves are built by layering `elf_base` + outfit + accessory + hair + hat.
- `Sprites.kt`: loads that file and draws sprites with sharp pixels (`Px`, `ElfPx`).
- `Data.kt`: game content (crops, cookies, toys, food, furniture, reindeer, levels, jobs, kids for Santa's list, elf friends).
- `GameState.kt`: all rules. Everything saved lives in the `Save` data class (kotlinx.serialization → SharedPreferences). Add new fields with defaults so old saves still load.
- Screens: `Ui.kt` (stage scaling, top bar, nav, pop-ups), `HomeScreen.kt`, `WorkScreens.kt`, `SantaScreens.kt`, `TownScreens.kt`.

Rules Lexi asked for: start with $1,000; finishing ALL of Santa's daily jobs pays $1,000; no work = no pay; 5 levels unlock toys/recipes/reindeer/deliveries; Christmas Eve finale after 10 workdays.

## Building
```sh
export JAVA_HOME=$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home
./gradlew assembleRelease      # → app/build/outputs/apk/release/app-release.apk
```
- Release builds are shrunk with R8 and signed with this Mac's debug key; `proguard-rules.pro` keeps the save classes.
- Bump `versionCode` for every APK you hand out so it installs as an update over the old one (same `applicationId`).
- Preview the pixel art without building: render `sprites.txt` with a small Python/PIL script (see git history) and look at the sheet.
- See `../lexi_apk/CLAUDE.md` for the Android SDK setup and how to install on Lexi's Redmi 14C (WhatsApp as a Document).

## Clean up when you're done
**Always shut down emulators/simulators and background build daemons when you've finished.**
- `adb emu kill`, `./gradlew --stop`
- iOS Simulator: `xcrun simctl shutdown all && osascript -e 'quit app "Simulator"'`
