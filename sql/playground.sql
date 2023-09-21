select tags.id, tags.name, tags.slug, taa.id, taa.name, taa.slug
from tags
         join tag_aliases ta on tags.id = ta.tag_id
         join tags taa on ta.target_tag_id = taa.id;

-- Most tag aliases
select ttic.target_tag_id, ttic.count, name
from (select target_tag_id, count(1) as count from tag_aliases group by target_tag_id) as ttic
         join tags on ttic.target_tag_id = tags.id
order by count desc
limit 20;

select ttic.tag_id, ttic.count, name
from (select count(1) as count, tag_id
      from image_taggings
      group by tag_id) as ttic
         join tags on ttic.tag_id = tags.id
order by count desc
limit 50;


select count(1) as count, image_id
from image_taggings
group by image_id
order by count desc
limit 50;

select (select count(1) from image_taggings) - (select count(1) from normalized_taggings);

-- images by character
select tag_id, name, count
from (select count(1) as count, tag_id
      from images
               join normalized_taggings it on images.id = it.image_id
               join tags t on it.tag_id = t.id
      where category = 'character'
      group by tag_id) as tag_count
         join tags on tag_count.tag_id = tags.id
order by count desc;

-- images by rating
select row_number() over (order by count desc) as row_number, name, TO_CHAR(ttic.count, '999,999,999') as count
from (select count(1) as count, tag_id
      from normalized_taggings
      group by tag_id) as ttic
         join tags on ttic.tag_id = tags.id
where category = 'rating'
order by row_number
limit 30;

-- Images with useless source urls

select row_number() over (order by count desc) as row_number, name, TO_CHAR(ttic.count, '999,999,999') as count
from (select count(1) as count, tag_id
      from normalized_taggings
      group by tag_id) as ttic
         join tags on ttic.tag_id = tags.id
order by row_number
limit 30;

SELECT count(1) AS count,
       image_taggings.image_id
FROM image_taggings
GROUP BY image_taggings.image_id
ORDER BY (count(1)) DESC
limit 20;

select image_id, count(1) as count
from tag_changes
group by image_id
order by count desc;

select image_id, count(1) as count
from tag_changes
where image_id not in (select distinct image_id from image_taggings where tag_id = 26707)
group by image_id
order by count desc;

select avg(count)
from (select image_id, count(1) as count
      from tag_changes
      group by image_id
      order by count desc) as iic;

select count(1)
from (select image_id, count(1) as count
      from tag_changes
      group by image_id
      order by count desc) as iic;

select count(1), date_trunc('month', created_at) as month from images group by month;

select count(1), date_trunc('day', created_at) as day from images  where date_trunc('month', created_at) = '2023-04-01 00:00:00'::timestamp or date_trunc('month', created_at) = '2023-03-01 00:00:00'::timestamp group by day

select
    percentile_disc(0.999) within group ( order by count ) as Percent999,
    percentile_disc(0.99) within group ( order by count ) as Percent99,
    percentile_disc(0.90) within group ( order by count ) as Percent90,
    percentile_disc(0.75) within group ( order by count ) as Percent75,
    percentile_disc(0.5) within group ( order by count ) as Percent50
from image_tag_count_view;

select count(1) as count from image_tag_count_view where image_tag_count_view.count > 100;

select count(1) from tag_pairs;

create index tag_pairs_tag_id on tag_pairs(tag_id, tag_id2);
select count(1), tag_id, tag_id2 as count from tag_pairs group by tag_id, tag_id2;

select count(1), source from image_sources join image_taggings it on image_sources.image_id = it.image_id where tag_id = 189269 group by source;