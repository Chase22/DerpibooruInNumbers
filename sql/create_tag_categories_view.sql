drop view if exists tag_categories;

create view tag_categories as select distinct category from tags;