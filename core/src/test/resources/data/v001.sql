INSERT INTO `restaurant` (`id`, `code`, `name_kr`, `name_en`, `addr`, `lat`, `lng`, `etc`, `created_at`, `updated_at`)
VALUES (1,'302동식당','302동식당',NULL,'서울 관악구 관악로 1 서울대학교 제2공학관',37.44862,126.95232,'{\"operating_hours\": {\"weekdays\": [\"11:30-13:30\", \"17:00-18:30\"], \"saturday\": [], \"holiday\": []}}','2021-01-30 07:01:07','2021-03-02 13:23:15');

INSERT INTO `user` (id, type, identity, etc, created_at, updated_at, nickname, profile_url) VALUES (1, 'KAKAO', '00000001', null, '2021-03-25 16:54:26', '2024-08-31 18:14:07', '수의대식당_목살스테이크_ad56', null);
INSERT INTO `board` (name, description, id, created_at, updated_at, type) VALUES ('자유게시판', '자유다!!', 1, '2024-08-31 19:03:06', '2024-08-31 19:19:12', 1);
INSERT INTO `post` (user_id, board_id, title, content, available, id, created_at, updated_at, etc, anonymous) VALUES (1, 1, '와~', '식샤에 커뮤니티 기능이 생겼어요!', 1, 1, '2024-09-10 23:16:12', '2024-09-10 23:16:12', null, 1);
INSERT INTO `comment` (`user_id`, `post_id`, `content`, `available`, `id`, `created_at`, `updated_at`, `anonymous`) VALUES (1, 1, '와~ 첫 댓글이라니 감격이에요', 1, 1, '2024-09-10 23:17:27', '2024-09-10 23:17:27', 1);
INSERT INTO `comment_like` (user_id, comment_id, is_liked, id, created_at, updated_at) VALUES (1, 1, 1, 1, '2024-09-11 00:06:38', '2024-09-11 00:06:38');
