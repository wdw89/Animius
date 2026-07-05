# AGENTS.md

## Project overview

Animius is a multi-source Android anime streaming app. Kotlin/Jetpack Compose, MVVM + Clean Architecture. 4 Gradle modules.

## Module structure

| Module | Package | Purpose | Compose? |
|---|---|---|---|
| `:app` | `com.lanlinju.animius` | Main app — UI, data, DI, all features | Yes |
| `:video-player` | `com.lanlinju.videoplayer` | ExoPlayer/Media3 video player wrapper | Yes |
| `:download` | `com.lanlinju.download` | HTTP/M3U8 download engine (OkHttp + Retrofit) | No |
| `:danmaku` | `com.anime.danmaku` | Danmaku (bullet comment) rendering engine | Yes |

Dependency graph: `app` → `video-player`, `app` → `download`, `app` → `danmaku`. No cross-deps between library modules.

## Build

```bash
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (needs signing env vars)
```

- **Gradle 8.14**, AGP 8.11.0, Kotlin 2.2.0, JVM target 17
- Version catalog: `gradle/libs.versions.toml` (single source of truth for versions)
- compileSdk 36, minSdk 26, targetSdk 36

## Tests

```bash
./gradlew test                       # all JVM unit tests
./gradlew :app:test                  # app module unit tests only
./gradlew :danmaku:test              # danmaku module unit tests only
./gradlew connectedAndroidTest       # instrumented tests (needs device/emulator)
```

- JUnit 4 throughout. Some tests hit real network (source parsers, Dandanplay client).
- DAO tests use Room in-memory DB (instrumented only).
- **Tests are NOT run in CI** — CI only runs `assembleDebug`/`assembleRelease`.
- No lint/format/static analysis tools configured (no detekt, ktlint, spotless, or .editorconfig).

## Code generation

KSP is used for:
- **Hilt** (`com.google.dagger:hilt-compiler`) — DI boilerplate
- **Room** (`androidx.room:room-compiler`) — DAO implementations

Room schemas are exported to `app/schemas/` (required for migration testing).

## Secrets / env vars

Build-time environment variables (no .env files in repo):
- `DANDANPLAY_APP_ID` / `DANDANPLAY_APP_SECRET` — DanDanPlay danmaku API credentials (injected into `BuildConfig`)
- `KEY_STORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` — APK signing (release only)

Debug builds work without any env vars set.

## Architecture quick reference

```
app/src/main/java/com/lanlinju/animius/
├── data/
│   ├── local/         # Room DB (entities, DAOs, relations)
│   ├── remote/
│   │   ├── api/       # AnimeApi interface → AnimeApiImpl
│   │   ├── dto/       # AnimeBean, EpisodeBean, VideoBean, etc.
│   │   ├── parse/     # 11 AnimeSource implementations (Jsoup scrapers)
│   │   └── dandanplay/# DanDanPlay API client
│   └── repository/    # Repository implementations
├── domain/            # Models, repository interfaces, use cases
├── presentation/      # Compose screens, components, navigation, theme
├── di/                # Hilt modules (AppModule, ApiModule)
└── util/              # SourceHolder, Preferences, extensions
```

**Source system**: 11 anime site scrapers implement `AnimeSource` interface (`getHomeData`, `getAnimeDetail`, `getVideoData`, `getSearchData`, `getWeekData`). `SourceHolder` singleton manages active source at runtime.

**Navigation**: Type-safe sealed `Screen` class with Kotlinx Serialization routes in `presentation/navigation/`.

**Crash handler**: Runs in separate `:error_handler` process via `CrashActivity`. Global uncaught exception handler in `MainActivity`.

## Key gotchas

- Source parsers use Jsoup to scrape live websites — they break when sites change markup. Tests for these exist but rely on local HTML fixtures or real network calls.
- `AnimeApplication.getInstance()` exposes a static context — used by `SourceHolder` and `Preferences` outside DI.
- Release builds have `isMinifyEnabled = true` + `isShrinkResources = true` with ProGuard. `SourceMode` enum is kept explicitly in `proguard-rules.pro`.
- `download` module is pure Kotlin (no Compose). `video-player` and `danmaku` are Compose libraries.
