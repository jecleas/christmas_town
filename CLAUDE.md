# Christmas Town (Android)

A cute chibi pixel-art game made for Lexi (a kid) and her dad: you're an elf working for Santa. Kotlin + Jetpack Compose, one Gradle module.

- `assets/sprites.txt`: ALL the pixel art. Each sprite is `@name` followed by rows of letters; each letter is a color from the `=x RRGGBB` palette at the top, `.` is see-through. Capital letters are recolored per elf (S skin, Z skin shadow, H hair, J hair shadow, O outfit, Q outfit shadow, T hat, E eyes). Elves are built by layering `elf_base` + outfit + accessory + hair + hat (`elf_back` is the same elf seen from behind).
- `Sprites.kt`: loads that file and draws sprites with sharp pixels (`Px`, `ElfPx`).
- `Data.kt`: game content (crops, cookies, toys, food, furniture, reindeer, levels, jobs, kids for Santa's list, 16 elf friends incl. shopkeeper Tinsel and banker Mr. Silverbell, friends' houses via `homeOf`, movies).
- Top-level `val`s in `Data.kt` that use `Place` must be `by lazy` (the `Place` enum's init touches `DataKt`, so an eager list of `Place`s crashes at startup).
- `GameState.kt`: all rules. Everything saved lives in the `Save` data class (kotlinx.serialization → SharedPreferences). Add new fields with defaults so old saves still load.
- Screens: `Ui.kt` (stage scaling, top bar, nav, pop-ups), `HomeScreen.kt`, `WorkScreens.kt`, `SantaScreens.kt`, `TownScreens.kt`, `PartyScreens.kt` (Party Place, Level 7: juice bar, balloon pop + memory games, friends, Christmas movies: pick a movie, bring up to 3 friends, choose seats, buy tickets ($3–$6 each) and snacks, then watch from your seats), `BankScreen.kt` (Mr. Silverbell's bank: save money, take it out, +$5 per $100 each night), `TownMap.kt` (the Town map, opened from the 🏘️ button at the bottom of the main screen: every friend's house to visit (talk, share treats, eat together, hide & seek) plus shortcuts to the Shop, Bank, Party, Park and Friends; invite up to 3 friends to your house). Lexi wants every place to keep its own icon on the main screen. The Party Place also has a dance floor where you meet new elves. Each workplace has a workmate friend (`workmates` in `Data.kt`, `WorkmateBar` in `WorkScreens.kt`): tips, a cheer + 1 heart when you finish that job, and a little help once a day. Tapping a friend (workmate 💬 Talk, a guest at your house, Talk at a friend's house) opens `TalkSheet.kt`: pick what to say, serve/share treats (cookie, tea, cocoa), invite them over.

Rules Lexi asked for: start with $1,000; every 2 jobs you finish pays $2,000 and finishing the sleigh deliveries pays $1,000 extra (finishing ALL the jobs makes the day count toward levels); no work = no pay; 5 levels unlock toys/recipes/reindeer/deliveries; Christmas Eve finale after 10 workdays.

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

## Music & sounds
- All audio is 8-bit and made by `tools/make_sounds.py` (pure Python, no installs): `python3 tools/make_sounds.py <out-folder>` writes WAVs. Songs are public-domain carols (Jingle Bells, Deck the Halls, We Wish You a Merry Christmas, Silent Night, Joy to the World, O Christmas Tree, Up on the Housetop, Jolly Old St Nicholas, Good King Wenceslas, The Twelve Days of Christmas) so there are no licensing issues.
- In the app: `res/raw/music_*.m4a` (compress WAV → AAC with `afconvert -f m4af -d aac in.wav out.m4a`, where the 22 kHz files reject custom bitrates; on Linux use `ffmpeg -i in.wav -c:a aac -b:a 40k out.m4a`) and `res/raw/sfx_*.wav`.
- `Audio.kt`: `Audio.play(Sfx.X)` for effects (SoundPool), `Audio.music(Music.X)` loops and cross-fades songs. `GameApp` in `Ui.kt` picks the song for the current place; a mini-game can call `GameSong(game, Music.X)` to play its own song while it's on screen. Music/sound switches are saved in `Save` and shown on the main screen. Audio pauses in `onPause`.
- If the user adds their own audio files (e.g. from Pixabay/Kenney, CC0), put them in `res/raw` with lowercase_underscore names and point the `Music`/`Sfx` enums at them.
