start transaction;

create temporary table cafe_301_restaurants
(
    id int not null primary key
) engine = memory;

insert into cafe_301_restaurants (id)
select id
from restaurant_v2
where name = '카페 301';

create temporary table cafe_301_menus
(
    id bigint not null primary key
) engine = memory;

insert into cafe_301_menus (id)
select menu.id
from menu_v2 menu
join cafe_301_restaurants restaurant on restaurant.id = menu.restaurant_id;

create temporary table cafe_301_meals
(
    id bigint not null primary key
) engine = memory;

insert into cafe_301_meals (id)
select meal.id
from meal_v2 meal
join cafe_301_restaurants restaurant on restaurant.id = meal.restaurant_id;

create temporary table cafe_301_reviews
(
    id bigint not null primary key
) engine = memory;

insert into cafe_301_reviews (id)
select review.id
from review_v2 review
join cafe_301_menus menu on menu.id = review.menu_id;

-- Restaurant custom documents contain the complete restaurant id set and dense
-- per-building orders, so removing a restaurant invalidates existing documents.
delete from restaurant_custom_v2;

delete restaurant_like
from restaurant_like_v2 restaurant_like
join cafe_301_restaurants restaurant on restaurant.id = restaurant_like.restaurant_id;

delete meal_menu
from meal_menu_v2 meal_menu
join cafe_301_meals meal on meal.id = meal_menu.meal_id;

delete menu_like
from menu_like_v2 menu_like
join cafe_301_menus menu on menu.id = menu_like.menu_id;

delete menu_alarm
from menu_alarm_v2 menu_alarm
join cafe_301_menus menu on menu.id = menu_alarm.menu_id;

delete review_like
from review_like_v2 review_like
join cafe_301_reviews review on review.id = review_like.review_id;

delete keyword_review
from keyword_review_v2 keyword_review
join cafe_301_reviews review on review.id = keyword_review.review_id;

delete review
from review_v2 review
join cafe_301_reviews cafe_review on cafe_review.id = review.id;

delete meal
from meal_v2 meal
join cafe_301_meals cafe_meal on cafe_meal.id = meal.id;

delete menu
from menu_v2 menu
join cafe_301_menus cafe_menu on cafe_menu.id = menu.id;

delete restaurant
from restaurant_v2 restaurant
join cafe_301_restaurants cafe_restaurant on cafe_restaurant.id = restaurant.id;

drop temporary table cafe_301_reviews;
drop temporary table cafe_301_meals;
drop temporary table cafe_301_menus;
drop temporary table cafe_301_restaurants;

commit;
