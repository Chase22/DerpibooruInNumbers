drop view if exists tag_aliases_count_view;

create view tag_aliases_count_view as
    select ttic.target_tag_id, ttic.count, name
    from (select target_tag_id, count(1) as count from tag_aliases group by target_tag_id) as ttic
             join tags on ttic.target_tag_id = tags.id
    order by count desc;