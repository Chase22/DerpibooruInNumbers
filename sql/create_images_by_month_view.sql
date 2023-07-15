drop view if exists images_by_month;
drop view if exists images_by_day_in_april;

create view images_by_month as
select count(1), date_trunc('month', created_at) as month
from images
group by month;

create view images_by_day_in_april as
select count(1), date_trunc('day', created_at) as day
from images
where date_trunc('month', created_at) = '2023-04-01 00:00:00'::timestamp
   or date_trunc('month', created_at) = '2023-03-01 00:00:00'::timestamp
group by day;