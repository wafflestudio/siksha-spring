insert into menu_alias_v2 (alias, menu_name)
select distinct fixed_menu.name, fixed_menu.name
from (
    select '셀프라면' name union all
    select '밥' union all
    select '김치' union all
    select '고등어 소금구이' union all
    select '철판주꾸미 볶음' union all
    select '떡갈비 구이' union all
    select '도가니탕' union all
    select '7선 산채비빔밥' union all
    select '소불고기뚝배기' union all
    select '비빔냉면' union all
    select '물냉면' union all
    select '초계국수' union all
    select '솥밥(추가)' union all
    select '닭가슴살큐브샐러드' union all
    select '헬스팩' union all
    select '즉석떡국' union all
    select '즉석라면' union all
    select '삼각김밥' union all
    select '두유' union all
    select '파인애플스틱' union all
    select '짜장면' union all
    select '짬뽕' union all
    select '고기소보로 볶음밥' union all
    select '곱빼기메뉴' union all
    select '콤비메뉴' union all
    select '순두부짬뽕' union all
    select '부대짬뽕' union all
    select '돈가스비빔면' union all
    select '탄탄비빔면' union all
    select '중화제육덮밥' union all
    select '마라탕' union all
    select '호구세트' union all
    select '치킨탕수육 (大)' union all
    select '우삼겹짬뽕' union all
    select '공기밥' union all
    select '탄산음료' union all
    select '계란후라이' union all
    select '찐만두' union all
    select '찹쌀 팥 도나쓰' union all
    select '미니탕수육' union all
    select '냉모밀' union all
    select '등심까츠' union all
    select '새우튀김카레우동' union all
    select '특 등심 왕돈까스' union all
    select '새우튀김옛날 등심돈까스' union all
    select '치즈돈까스' union all
    select '고구마 치즈돈까스' union all
    select '새우튀김 카레라이스' union all
    select '카레돈가츠' union all
    select '돈가스 카레우동' union all
    select '눈꽃치즈 돈까스' union all
    select '소세지 카레라이스' union all
    select '옥수수 감자고로케 2P' union all
    select '숯불양념치킨덮밥' union all
    select '치킨마요' union all
    select '고기든든' union all
    select '제육덮밥' union all
    select '참치마요' union all
    select '스팸마요' union all
    select '볶음 김치덮밥' union all
    select '참치마요(R)' union all
    select '스팸마요(R)' union all
    select '제육한접시' union all
    select '고기한접시' union all
    select '스팸' union all
    select '비빔 쌀국수' union all
    select '포포 냉쌀국수' union all
    select '포포 냉짤국수' union all
    select '얼큰 해물 쌀국수' union all
    select '포포 쌀국수' union all
    select '우삼겹 쌀국수' union all
    select '마라우삼겹 쌀국수' union all
    select '직화구이 쌀국수' union all
    select '포포 분짜' union all
    select '직화구이' union all
    select '야채춘권(3p)' union all
    select '반꿔이' union all
    select '순두부찌개' union all
    select '우삽겹순두부찌개' union all
    select '우삼겹순두부찌개' union all
    select '우삼겹된장찌개' union all
    select '바지락된장찌개' union all
    select '우삽겹된장찌개' union all
    select '김치찌개' union all
    select '돼지김치찌개' union all
    select '참치김치찌개' union all
    select '스팸김치찌개' union all
    select '키친101 비빔밥(야채)' union all
    select '육회 비빔밥(S)' union all
    select '육회 비빔밥(L)' union all
    select '연어 비빔밥(S)' union all
    select '연어 비빔밥(L)' union all
    select '돈육불고기 비빔밥' union all
    select '장어비빔밥' union all
    select '훈제오리 샐러드' union all
    select '닭가슴살 샐러드' union all
    select '연두부튀김 샐러드' union all
    select '연어샐러드' union all
    select '닭가슴살포케' union all
    select '훈제오리포케' union all
    select '훈제연어포케' union all
    select '연두부튀김포케' union all
    select '연어샐러드포케'
) fixed_menu
where not exists (
    select 1
    from menu_alias_v2 menu_alias
    where menu_alias.alias = fixed_menu.name
);
