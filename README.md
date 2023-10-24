# Derpibooru in numbers

This repository contains all my resources for my talk "Derpibooru in Numbers" first held at the PonyConHolland 2023.

The resources are not sorted, nor complete, but here's a quick overview of the available things

## CachingServer
Kotlin source code for a derpibooru proxy. Calling localhost:8080/<derpibooru_image_id> will look for the referenced image in the imageCache folder. 
If it's not found the image is downloaded and then served

## slideImages
Some raw images that got edited to be used in the slides

## slides
The actual presentation. It's a [remark](https://github.com/gnab/remark/) presentation. An export of the presentation is also avaible in the root folder

## sql
A colorful collection of sql queries. Mostly for creating useful view in the database. Playground.sql are just disconnected queries

## src
Some kotlin files to generate plotly graphs, database tables or other database exports.

## slides-export.pdf
The full presentation as pdf export