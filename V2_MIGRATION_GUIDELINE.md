# Siksha V2 API Migration Guideline

## Goal

The goal of the V2 migration is to expose crawler-v2 data through APIs that match the new V2 database model.

The core V2 flow is:

```text
crawler v2 -> /v2/crawler/meals -> v2 tables -> v2 client APIs
```

V1 APIs must remain available for the existing app. V2 APIs should be added alongside V1 and should use V2 tables only.

## Scope

### In Scope

The primary migration target is the domain directly affected by the new restaurant, meal, menu, and review schema.

- Restaurants
- Building and restaurant customs
- Menus
- Menu likes
- Menu alarms, only if V2 alarm support is required
- Reviews
- Review likes
- Keyword reviews
- Crawler meal sync

### Out of Scope For Now

The following domains are not directly affected by the V2 restaurant/menu schema and should remain on V1 for now.

- Auth
- User profile
- User device
- Community boards
- Community posts
- Community comments
- VOC
- Ping
- App version
- Festival date APIs, unless the client requires `/v2` prefix consistency

If the client later needs all endpoints under `/v2`, thin alias controllers can be added separately. That should not block the core menu/review migration.

## Current V2 State

### Existing V2 APIs

```text
POST  /v2/crawler/meals
GET   /v2/restaurants/web
GET   /v2/restaurants/personal
PATCH /v2/restaurants/like/{restaurantId}
GET   /v2/customs/buildings
PATCH /v2/customs/buildings
GET   /v2/customs/buildings/{buildingNumber}/restaurants
PATCH /v2/customs/buildings/{buildingNumber}/restaurants
GET   /v2/menus
GET   /v2/menus/web
GET   /v2/menus/{menuId}
GET   /v2/menus/{menuId}/web
POST  /v2/menus/{menuId}/like
POST  /v2/menus/{menuId}/unlike
GET   /v2/menus/me
```

### Existing V2 Tables

```text
building_v2
restaurant_v2
building_custom_v2
restaurant_custom_v2
restaurant_like_v2
menu_v2
menu_alias_v2
meal_v2
meal_menu_v2
review_v2
keyword_review_v2
menu_like_v2
review_like_v2
```

### Main Structural Difference From V1

V1 `menu` rows represent date/type/restaurant-specific menu items.

```text
menu = restaurant + date + meal type + menu code/name + price
```

V2 splits that concept into normalized menus and meal instances.

```text
menu_v2 = normalized representative menu
meal_v2 = restaurant + date + meal type + price + no_meat
meal_menu_v2 = meal_v2 + menu_v2 + original_name
```

Because of this, V2 menu APIs must not be implemented as simple table-name replacements. They must reassemble response data from `meal_v2`, `meal_menu_v2`, `menu_v2`, `restaurant_v2`, and `building_v2`.

## API Naming Rules

Use the `/v2` prefix for V2 client APIs.

Unauthenticated APIs should use the `/web` postfix.

```text
GET /v2/restaurants/web
GET /v2/menus/web
GET /v2/reviews/web
```

Authenticated or personalized APIs should avoid `/web` and should use the authenticated request context.

Custom APIs are grouped under `/v2/customs`.

```text
GET   /v2/customs/buildings
PATCH /v2/customs/buildings
GET   /v2/customs/buildings/{buildingNumber}/restaurants
PATCH /v2/customs/buildings/{buildingNumber}/restaurants
```

## Custom Snapshot Rule

V2 custom data is stored as one JSON document per user.

```text
building_custom_v2.user_id PK
building_custom_v2.customs JSON

restaurant_custom_v2.user_id PK
restaurant_custom_v2.customs JSON
```

The JSON format is a full snapshot, not a sparse override.

```json
{
  "items": {
    "1": { "order": 1, "visible": true },
    "2": { "order": 2, "visible": false }
  }
}
```

Rules:

- If a user has no custom row, use default ordering and `visible=true`.
- If a user has a custom row, every target item must be present.
- Each item must include both `order` and `visible`.
- `order` must be dense within the relevant scope.
- Missing, duplicated, or malformed custom data must return an error.

## Migration Order

### 1. Menu V2 Read APIs

Implement menu read APIs first because they are the main consumer-facing surface for crawler-v2 meal data.

Target APIs:

```text
GET /v2/menus
GET /v2/menus/web
GET /v2/menus/{menuId}
GET /v2/menus/{menuId}/web
```

Recommended files:

```text
api/controller/v2/MenuV2Controller.kt
core/domain/main/menu/dto/MenuV2Dtos.kt
core/domain/main/menu/repository/MenuV2QueryRepository.kt or MenuV2Repository extensions
core/domain/main/menu/service/MenuV2Service.kt
```

Implementation notes:

- Build list responses from `meal_v2`, `meal_menu_v2`, `menu_v2`, `restaurant_v2`, and `building_v2`.
- Preserve the existing date/type grouping shape only if the V2 client still expects it.
- Consider grouping by building first if the V2 client aligns with the restaurant grouped response.
- Keep `except_empty` if the client still needs empty restaurant slots.
- For unauthenticated `/web`, `is_liked=false` and personalized filtering should not apply.
- For authenticated APIs, apply `menu_like_v2` and custom visibility/order where relevant.

Decision:

- `GET /v2/menus/{menuId}` uses `menu_v2.id` because likes and reviews reference normalized menus.
- Date/type/price context is returned as a `meals` list on the detail response.

### 2. Menu Like V2

After read APIs are stable, move menu-like behavior to `menu_like_v2`.

Target APIs:

```text
POST /v2/menus/{menuId}/like
POST /v2/menus/{menuId}/unlike
GET  /v2/menus/me
```

Implementation notes:

- `menu_like_v2` should reference `menu_v2.id`.
- Like count should aggregate by `menu_v2.id`.
- Unlike should update only V2 like state.
- `GET /v2/menus/me` should return liked V2 menus in the client-expected V2 shape.

Decision:

- Keep `menu_like_v2.is_liked` for now because the column already exists in the V2 schema.
- Like upserts a row with `is_liked=1`; unlike updates the row to `is_liked=0`.

### 3. Menu Alarm V2 Decision

V2 currently has no `menu_alarm_v2` table. Do not migrate menu alarm APIs until the schema is decided.

Existing V1 APIs:

```text
POST /menus/{menu_id}/alarm/on
POST /menus/{menu_id}/alarm/off
POST /menus/alarm/on
POST /menus/alarm/off
```

Recommended V2 schema if alarms are required:

```text
menu_alarm_v2
- user_id
- menu_id -> menu_v2.id
- created_at
- updated_at
- unique(user_id, menu_id)
```

Target APIs, if implemented:

```text
POST /v2/menus/{menuId}/alarm/on
POST /v2/menus/{menuId}/alarm/off
POST /v2/menus/alarm/on
POST /v2/menus/alarm/off
```

Implementation notes:

- Scheduler code must be updated separately to read `menu_alarm_v2`.
- Alarm semantics should be based on normalized `menu_v2`, not a single date-specific meal row.

### 4. Review V2 Entities And Repositories

V2 review tables exist, but Kotlin domain code is not yet implemented.

Add:

```text
ReviewV2
KeywordReviewV2
ReviewLikeV2
ReviewV2Repository
KeywordReviewV2Repository
ReviewLikeV2Repository
ReviewV2Service
ReviewV2Controller
```

Tables:

```text
review_v2
keyword_review_v2
review_like_v2
```

Implementation notes:

- `review_v2.menu_id` references `menu_v2.id`.
- Review aggregation should be based on normalized menu identity.
- Existing image upload flow can be reused.
- Existing keyword mapping utilities can be reused.
- Existing review response DTOs may be copied first, then simplified for V2.

Recommended schema check:

- Add a unique constraint for one review per user per normalized menu if that rule should remain.

```text
unique(menu_id, user_id)
```

### 5. Review V2 APIs

Target APIs:

```text
POST   /v2/reviews
POST   /v2/reviews/images
GET    /v2/reviews
GET    /v2/reviews/web
GET    /v2/reviews/{reviewId}
GET    /v2/reviews/{reviewId}/web
PATCH  /v2/reviews/{reviewId}
DELETE /v2/reviews/{reviewId}
POST   /v2/reviews/{reviewId}/like
DELETE /v2/reviews/{reviewId}/like
GET    /v2/reviews/me
GET    /v2/reviews/dist
GET    /v2/reviews/keyword/dist
GET    /v2/reviews/filter
GET    /v2/reviews/filter/web
```

Implementation notes:

- Request `menu_id` should mean `menu_v2.id` unless a separate identifier is introduced.
- Review list APIs should query `review_v2` by `menu_v2.id`.
- Like count should query `review_like_v2`.
- Score and keyword distribution should query `review_v2` and `keyword_review_v2`.
- `GET /v2/reviews/me` should group reviews by V2 restaurant/building if the client needs grouped display.

### 6. Connect Menu Score And Review Count To Review V2

Once Review V2 exists, Menu V2 read APIs should compute:

```text
score = AVG(review_v2.score)
review_cnt = COUNT(review_v2.id)
```

The aggregation should be by `menu_v2.id`.

If old V1 review data must be visible in V2 during migration, define an explicit migration or compatibility strategy instead of silently mixing V1 and V2 tables.

### 7. Keep V1 Stable

Do not remove or rewrite V1 behavior during V2 migration.

Rules:

- V1 controllers continue to use V1 services and V1 tables.
- V2 controllers use V2 services and V2 tables.
- Shared infrastructure, auth, image upload, validators, and utilities may stay common.
- Avoid changing V1 response shape while implementing V2.

## Testing Guideline

For each V2 migration step, add focused tests around the changed domain.

Menu V2 tests:

- Date range query returns meals grouped by date/type.
- Web query marks all menus as not liked.
- Auth query applies `menu_like_v2`.
- Empty restaurant handling follows `except_empty`.
- Response ordering follows building/restaurant default and custom order rules where applicable.

Menu Like V2 tests:

- Like creates V2 like state.
- Unlike removes or disables V2 like state.
- Like count updates in detail/list response.
- V1 `menu_like` is not touched.

Menu Alarm V2 tests, if implemented:

- Alarm on/off writes only V2 alarm state.
- Duplicate alarm returns the expected error.
- Alarm all respects liked V2 menus.
- Scheduler reads V2 alarm data.

Review V2 tests:

- Review create writes `review_v2` and `keyword_review_v2`.
- Image review stores image metadata and review etc JSON.
- Duplicate review is rejected if unique rule is adopted.
- Review like writes `review_like_v2`.
- Review list/detail/dist read from V2 tables.
- V1 review tables are not touched.

## Rollout Guideline

1. Add V2 APIs alongside V1.
2. Keep V1 app traffic on V1 endpoints.
3. Point V2 client screens to V2 endpoints one domain at a time.
4. Validate dev DB with crawler-v2 data before enabling client usage.
5. Avoid backfilling or deleting V1 data until V2 client behavior is verified.
6. If V2 review/menu-like data needs QA seed data, generate it only against V2 tables.

## Suggested Next Task

Start with Menu V2 read APIs.

First implementation target:

```text
GET /v2/menus/web
GET /v2/menus
```

This gives the client a way to consume crawler-v2 meal data before likes, alarms, and reviews are moved.
