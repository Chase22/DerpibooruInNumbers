drop view if exists image_count_by_episode;

create view image_count_by_episode as
select tag_id, name, count
from (select count(1) as count, tag_id
      from images
          join image_taggings it on images.id = it.image_id
          join tags t on it.tag_id = t.id
      where category = 'spoiler' or category = 'content-official'
      group by tag_id) as tag_count
         join tags on tag_count.tag_id = tags.id order by count desc;