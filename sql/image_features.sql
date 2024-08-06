create or replace view data_features_images as
select *
from images;

select id,
       image_width,
       image_height,
       extract(month from created_at::timestamp) as created_month,
       extract(year from created_at::timestamp)  as created_year,
       upvotes,
       downvotes,
       score
from images;

