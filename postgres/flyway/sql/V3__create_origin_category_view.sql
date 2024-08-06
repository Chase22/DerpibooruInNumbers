create MATERIALIZED VIEW origin_category as
select id,
       split_part(name, ':', 1) as origin_category,
       split_part(name, ':', 2) as origin
from tags
where category = 'origin'