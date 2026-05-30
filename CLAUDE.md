# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Native Android app (Kotlin + Jetpack Compose) companion for [CertForge](http://github.com/your-org/certforge) — a multi-certification exam study platform. Currently configured for AZ-104 (Azure Administrator). Syncs progress bidirectionally with the web app at `../certforge-web` when on the home WiFi network.

All 10 implementation phases from the original PRD are fully implemented.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Database:** Room (SQLite) — 10 entities, destructive migration (data is re-downloadable)
- **Network:** Retrofit + OkHttp for HTTP sync, kotlinx-serialization
- **DI:** Hilt
- **Background:** WorkManager for periodic sync (2-hour interval)
- **QR:** CameraX + ML Kit Barcode Scanning
- **Security:** EncryptedSharedPreferences for API token storage

## Architecture

```
UI (Jetpack Compose)
    ↕
Repository Layer (SyncRepository + ContentRepository)
    ↕
Room Database (SQLite — offline cache)
    ↔ HTTP sync (certId= query param) ↔
Web App (Next.js API Routes → sql.js/SQLite)
```

### Key changes from original PRD

- **Rebranded to CertForge** — app name, User-Agent header, Room DB filename, SharedPreferences names
- **Multi-cert support** — API calls pass `certId=az-104` query param; `certifications` table + `cert_id` column on domains
- **DB renamed** from `az104_study.db` to `certforge.db` (destructive migration handles this)
- **New endpoint** `GET /api/certifications` available on SyncApi
- **Certification selector** in Settings (shows current cert; multi-cert dropdown when web app seeds >1)

### Data Flow

- **First-time sync:** Pair device via QR → POST `/api/devices/confirm` to get `apiToken` → full download by certId
- **Regular sync (auto on home WiFi / manual):** Check manifest (with certId) → compare hashes → download changes → upload/merge progress
- **Offline:** All reads from Room cache. Quiz sessions and chapter completions saved locally, queued for next sync

### Key constraints

- Quiz sessions: append-only, deduped by `clientId` (local UUID)
- Chapter progress: last-write-wins by `completedAt` timestamp
- Static data (questions, guides): server is source of truth, client overwrites local copy
- Sync no more than once per 5 minutes (client-side rate limit)
- Article HTML cached on-demand (keep last 10, evict oldest)

## API Endpoints (Web App)

All sync endpoints accept `?certId=` query param (defaults to `"az-104"` on server). Auth: `Authorization: Bearer <apiToken>` header.

| Method | Route | Purpose |
|--------|-------|---------|
| GET | `/api/certifications` | List available certifications |
| POST | `/api/devices/pair` | Generate one-time setup token |
| POST | `/api/devices/confirm` | Exchange setup token for permanent `apiToken` |
| DELETE | `/api/devices/[token]` | Revoke a device |
| GET | `/api/devices?profileId=` | List paired devices |
| GET | `/api/sync/manifest` | Version hashes for all data types |
| GET | `/api/sync/domains` | All domains + chapters (full sync) |
| GET | `/api/sync/questions[?since=]` | Practice questions (incremental by `updatedAt`) |
| GET | `/api/sync/study-guides[?since=]` | AI study guides (incremental) |
| GET | `/api/sync/articles` | Article metadata (full sync) |
| GET | `/api/articles/[articleId]` | Full HTML content for an article (cache on-demand) |
| POST | `/api/sync/progress` | Upload quiz sessions + chapter progress |
| GET | `/api/sync/progress?profileId=[&since=]` | Download progress from server |
| GET | `/api/profiles` | List profiles |
| POST | `/api/profiles/verify` | Verify profile PIN |

Sync endpoints use `profileId` query param for device identity; Android app also includes `apiToken` header.

## Room Data Model

10 tables: `certifications`, `domains` (with `cert_id` column), `chapters`, `questions`, `study_guides`, `articles`, `quiz_sessions`, `question_attempts`, `chapter_progress`, `sync_metadata`.

## Net-new files created during rebrand

- `CertificationEntity.kt` — Room entity for certifications
- `CertificationDao.kt` — DAO with upsertAll, observeAll, getByCode, getById, deleteAll

## Sister Project

The web app lives at `../certforge-web` (Next.js 15, TypeScript, SQLite/sql.js, Drizzle ORM, Tailwind CSS + shadcn/ui). It has been renamed from "AZ-104 Study App" to **CertForge** and supports multiple certifications. Seed the web app's database before testing Android sync.

## HANDOVER Notes (from HANDOVER-APP.md)

The web app rebranding (AZ-104 Study → CertForge) requires the Android app to:
1. Update base URL from `azure-study.localdomain` to `YOUR_SERVER.local` (user-configured)
2. Pass `?certId=az-104` to all sync/study endpoints *(Done)*
3. QR payload URL points to `YOUR_SERVER.local` (handled automatically by user's config)
4. User-Agent header set to `CertForge-Companion/1.0` *(Done)*
5. DB schema: add `certifications` table + `cert_id` column *(Done)*
6. Branding: update app name to "CertForge" *(Done)*

What has NOT changed: quiz engine, profile system, device pairing flow, sync protocol, chapter progress, analytics/achievements.
