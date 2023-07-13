drop view if exists tag_image_count_view;
drop view if exists image_tag_count_view;

create view tag_image_count_view as select ttic.tag_id, ttic.count, name
                                from (select count(1) as count, tag_id
                                      from image_taggings
                                      group by tag_id) as ttic
                                         join tags on ttic.tag_id = tags.id
                                order by count desc;

create view image_tag_count_view as select count(1) as count, image_id
                                    from image_taggings
                                    group by image_id order by count desc;