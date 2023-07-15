drop view if exists tag_aliases_view;

create view tag_aliases_view as
select tags.id   as tag_id,
       tags.name as tag_name,
       tags.slug as tag_slug,
       taa.id    as target_id,
       taa.name  as target_name,
       taa.slug  as target_slug
from tags
         join tag_aliases ta on tags.id = ta.tag_id
         join tags taa on ta.target_tag_id = taa.id;