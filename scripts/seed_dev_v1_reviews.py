#!/usr/bin/env python3
"""Seed dummy v1 reviews into the dev Siksha server.

Default mode is dry-run. Add --execute to create test users and reviews.
"""

from __future__ import annotations

import argparse
import base64
import datetime as dt
import json
import mimetypes
import os
import random
import re
import sys
import uuid
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


DEFAULT_BASE_URL = "https://siksha-server-dev.wafflestudio.com"
DEFAULT_IMAGE_PATH = "C:/Users/denni/Downloads/KakaoTalk_20260416_144635055.png"
MEAL_TYPES = ("BR", "LU", "DN")

TASTE_KEYWORDS = [
    "별로예요",
    "아쉬운 맛이에요",
    "무난해요",
    "생각보다 맛있어요",
    "또 먹고 싶어요",
]
PRICE_KEYWORDS = [
    "너무 비싸요",
    "약간 비싸요",
    "합리적이에요",
    "가성비 좋아요",
    "혜자스러워요",
]
COMPOSITION_KEYWORDS = [
    "너무 빈약해요",
    "다소 단조로워요",
    "기본적이에요",
    "알찬 편이에요",
    "조화로워요",
]


@dataclass(frozen=True)
class MenuSeedTarget:
    id: int
    date: str
    meal_type: str
    restaurant_id: int
    restaurant_name: str
    menu_name: str


class ApiClient:
    def __init__(self, base_url: str, timeout: int) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    def url(self, path: str, query: dict[str, str] | None = None) -> str:
        normalized_path = path if path.startswith("/") else f"/{path}"
        url = f"{self.base_url}{normalized_path}"
        if query:
            url = f"{url}?{urlencode(query)}"
        return url

    def request_json(
        self,
        method: str,
        path: str,
        payload: dict[str, Any] | None = None,
        token: str | None = None,
        query: dict[str, str] | None = None,
    ) -> tuple[int, Any]:
        headers = {"Accept": "application/json"}
        body: bytes | None = None
        if payload is not None:
            headers["Content-Type"] = "application/json"
            body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        if token:
            headers["Authorization"] = f"Bearer {token}"

        request = Request(self.url(path, query), data=body, headers=headers, method=method)
        return self._send(request)

    def request_multipart(
        self,
        path: str,
        fields: dict[str, Any],
        files: list[tuple[str, Path]],
        token: str,
    ) -> tuple[int, Any]:
        boundary = f"----siksha-seed-{uuid.uuid4().hex}"
        chunks: list[bytes] = []

        for name, value in fields.items():
            chunks.extend(
                [
                    f"--{boundary}\r\n".encode(),
                    f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode(),
                    str(value).encode("utf-8"),
                    b"\r\n",
                ],
            )

        for field_name, path_obj in files:
            filename = path_obj.name
            content_type = mimetypes.guess_type(filename)[0] or "application/octet-stream"
            chunks.extend(
                [
                    f"--{boundary}\r\n".encode(),
                    (
                        f'Content-Disposition: form-data; name="{field_name}"; '
                        f'filename="{filename}"\r\n'
                    ).encode(),
                    f"Content-Type: {content_type}\r\n\r\n".encode(),
                    path_obj.read_bytes(),
                    b"\r\n",
                ],
            )

        chunks.append(f"--{boundary}--\r\n".encode())
        body = b"".join(chunks)
        headers = {
            "Accept": "application/json",
            "Authorization": f"Bearer {token}",
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        }
        request = Request(self.url("/reviews/images"), data=body, headers=headers, method="POST")
        return self._send(request)

    def _send(self, request: Request) -> tuple[int, Any]:
        try:
            with urlopen(request, timeout=self.timeout) as response:
                return response.status, parse_response_body(response.read())
        except HTTPError as exc:
            return exc.code, parse_response_body(exc.read())
        except URLError as exc:
            raise RuntimeError(f"Request failed: {exc}") from exc


def parse_response_body(raw: bytes) -> Any:
    if not raw:
        return None
    text = raw.decode("utf-8", errors="replace")
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        return text


def normalize_image_path(path: str) -> Path:
    match = re.match(r"^([A-Za-z]):[\\/](.*)$", path)
    if match and os.name != "nt":
        drive = match.group(1).lower()
        rest = match.group(2).replace("\\", "/")
        return Path(f"/mnt/{drive}/{rest}")
    return Path(path).expanduser()


def today_iso() -> str:
    return dt.date.today().isoformat()


def add_days(date_str: str, days: int) -> str:
    base = dt.date.fromisoformat(date_str)
    return (base + dt.timedelta(days=days)).isoformat()


def fetch_menu_targets(
    client: ApiClient,
    start_date: str,
    end_date: str,
    menu_date: str | None,
    menu_limit: int,
) -> list[MenuSeedTarget]:
    status, body = client.request_json(
        "GET",
        "/menus/web",
        query={
            "start_date": start_date,
            "end_date": end_date,
            "except_empty": "true",
        },
    )
    if status != 200:
        raise RuntimeError(f"Failed to fetch menus: HTTP {status} {body}")

    targets: list[MenuSeedTarget] = []
    for date_group in body.get("result", []):
        date = date_group.get("date")
        for meal_type in MEAL_TYPES:
            restaurants = date_group.get(meal_type) or date_group.get(meal_type.lower()) or []
            for restaurant in restaurants:
                restaurant_id = int(restaurant["id"])
                restaurant_name = restaurant.get("name_kr") or restaurant.get("code") or str(restaurant_id)
                for menu in restaurant.get("menus", []) or []:
                    menu_id = menu.get("id")
                    if menu_id is None:
                        continue
                    menu_name = menu.get("name_kr") or menu.get("code") or str(menu_id)
                    targets.append(
                        MenuSeedTarget(
                            id=int(menu_id),
                            date=date,
                            meal_type=meal_type,
                            restaurant_id=restaurant_id,
                            restaurant_name=restaurant_name,
                            menu_name=menu_name,
                        ),
                    )

    if not targets:
        raise RuntimeError(f"No menu targets found between {start_date} and {end_date}")

    target_date = menu_date or max(target.date for target in targets)
    latest_targets = [target for target in targets if target.date == target_date]
    latest_targets.sort(key=lambda target: (target.meal_type, target.restaurant_id, target.id))

    if menu_limit > 0:
        latest_targets = latest_targets[:menu_limit]
    if not latest_targets:
        raise RuntimeError(f"No menu targets found for date {target_date}")

    return latest_targets


def login_test_user(client: ApiClient, identity: str) -> str:
    status, body = client.request_json("POST", "/auth/login/test", {"identity": identity})
    if status != 200:
        raise RuntimeError(f"Login failed for {identity}: HTTP {status} {body}")
    token = body.get("access_token") or body.get("accessToken")
    if not token:
        raise RuntimeError(f"Login response has no access token for {identity}: {body}")
    return token


def user_id_from_jwt(token: str) -> int | None:
    try:
        payload = token.split(".")[1]
        payload += "=" * (-len(payload) % 4)
        decoded = json.loads(base64.urlsafe_b64decode(payload.encode()))
        return decoded.get("userId")
    except Exception:
        return None


def build_review_payload(menu: MenuSeedTarget, user_index: int, review_index: int) -> dict[str, Any]:
    keyword_index = (user_index + review_index) % 5
    score = 3 + ((user_index + review_index) % 3)
    return {
        "menu_id": menu.id,
        "score": score,
        "comment": (
            f"[QA seed] user {user_index + 1:02d} review for {menu.date} "
            f"{menu.restaurant_name} / {menu.menu_name}"
        ),
        "taste": TASTE_KEYWORDS[keyword_index],
        "price": PRICE_KEYWORDS[keyword_index],
        "food_composition": COMPOSITION_KEYWORDS[keyword_index],
    }


def post_review(
    client: ApiClient,
    token: str,
    payload: dict[str, Any],
    image_path: Path | None,
) -> tuple[int, Any]:
    if image_path:
        return client.request_multipart("/reviews/images", payload, [("images", image_path)], token)
    return client.request_json("POST", "/reviews", payload, token=token)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed dummy v1 reviews into Siksha dev.")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--user-count", type=int, default=10)
    parser.add_argument("--reviews-per-user", type=int, default=5)
    parser.add_argument("--identity-prefix", default=f"qa-review-seed-{today_iso()}")
    parser.add_argument("--start-date", default=today_iso())
    parser.add_argument("--days", type=int, default=14)
    parser.add_argument("--menu-date", default=None, help="Specific menu date. Default: latest date in fetched range.")
    parser.add_argument("--menu-limit", type=int, default=0, help="0 means all menus on selected date.")
    parser.add_argument("--image-path", default=DEFAULT_IMAGE_PATH)
    parser.add_argument("--image-every", type=int, default=4, help="Attach image to every Nth review. 0 disables images.")
    parser.add_argument("--timeout", type=int, default=20)
    parser.add_argument("--seed", type=int, default=20260609)
    parser.add_argument("--execute", action="store_true", help="Actually create users and reviews.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    random.seed(args.seed)
    client = ApiClient(args.base_url, args.timeout)
    end_date = add_days(args.start_date, args.days)

    image_path = normalize_image_path(args.image_path)
    if args.image_every > 0 and not image_path.exists():
        raise RuntimeError(f"Image file not found: {image_path}")

    targets = fetch_menu_targets(
        client=client,
        start_date=args.start_date,
        end_date=end_date,
        menu_date=args.menu_date,
        menu_limit=args.menu_limit,
    )
    selected_date = targets[0].date
    per_user_count = min(args.reviews_per_user, len(targets))

    identities = [f"{args.identity_prefix}-{index + 1:02d}" for index in range(args.user_count)]
    print(f"base_url={args.base_url}")
    print(f"selected_menu_date={selected_date}")
    print(f"menu_targets={len(targets)}")
    print(f"user_count={len(identities)} reviews_per_user={per_user_count}")
    print(f"image_path={image_path if args.image_every > 0 else '(disabled)'}")
    print(f"mode={'EXECUTE' if args.execute else 'DRY-RUN'}")
    print()

    for target in targets[:20]:
        print(f"menu {target.id}: {target.date} {target.meal_type} {target.restaurant_name} / {target.menu_name}")
    if len(targets) > 20:
        print(f"... {len(targets) - 20} more menu targets")
    print()

    if not args.execute:
        print("Dry-run only. Re-run with --execute to create test users and reviews.")
        return 0

    created = 0
    skipped = 0
    failures = 0
    review_ordinal = 0

    for user_index, identity in enumerate(identities):
        token = login_test_user(client, identity)
        user_id = user_id_from_jwt(token)
        print(f"user {user_index + 1:02d}: identity={identity} user_id={user_id or '?'}")

        for review_index in range(per_user_count):
            target = targets[(user_index + review_index) % len(targets)]
            payload = build_review_payload(target, user_index, review_index)
            review_ordinal += 1
            should_attach_image = args.image_every > 0 and review_ordinal % args.image_every == 0
            status, body = post_review(client, token, payload, image_path if should_attach_image else None)

            if status in (200, 201):
                created += 1
                marker = "image" if should_attach_image else "plain"
                print(f"  created {marker} review: menu_id={target.id} score={payload['score']}")
            elif status == 409:
                skipped += 1
                print(f"  skipped existing review: menu_id={target.id}")
            else:
                failures += 1
                print(f"  failed review: menu_id={target.id} HTTP {status} {body}", file=sys.stderr)

    print()
    print(f"done created={created} skipped={skipped} failures={failures}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
