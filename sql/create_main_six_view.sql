drop view if exists mane6_tags;

create view mane6_tags as
select id, name, slug
from tags
where tags.slug in ('twilight+sparkle', 'rainbow+dash', 'pinkie+pie', 'rarity', 'applejack', 'fluttershy');