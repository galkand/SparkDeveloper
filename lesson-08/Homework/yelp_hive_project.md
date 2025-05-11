
## Домашнее задание: Hive + Yelp Open Dataset

### Введение

В основе данного ДЗ лежит набор данных, предоставленный ресурсом yelp: https://business.yelp.com/data/resources/open-dataset/
Несмотря на то, что в HDFS мы заливаем все файлы датасета, в данном ДЗ будет использоваться только business.json

business.json содержит информацию о местах, куда приходят пользователи проводят там какое-то время и потом ставят этому месту оценки и иногда оставляют отзывы.

Можно рассматривать решение данного ДЗ как задел для будущего дипломного проекта.

Джойны тоже будут, так как внешнюю таблицу мы предварительно распилим на три внутренних. Куда впихнуть union, я не придумал, за то есть LATERAL VIEW

### Построение объектов
#### Закидываем все в контейнер и потом в hive

```bash
docker cp "..\datasets\yelp\business.json" hadoop-hive-server-1:/opt
docker cp "..\datasets\yelp\user_data.json" hadoop-hive-server-1:/opt
docker cp "..\datasets\yelp\review.json" hadoop-hive-server-1:/opt
docker cp "..\datasets\yelp\tip.json" hadoop-hive-server-1:/opt
docker cp "..\datasets\yelp\checkin.json" hadoop-hive-server-1:/opt

docker-compose exec hive-server bash

hdfs dfs -mkdir -p /user/hive/yelp/{business,user_data,review,tip,checkin}

dfs -put -f /opt/business.json /user/hive/yelp/business;
dfs -put -f /opt/user_data.json /user/hive/yelp/user_data;
dfs -put -f /opt/review.json /user/hive/yelp/review;
dfs -put -f /opt/tip.json /user/hive/yelp/tip;
dfs -put -f /opt/checkin.json /user/hive/yelp/checkin;
```

---
#### Создаем внешнюю таблицу

```sql
CREATE EXTERNAL TABLE IF NOT EXISTS yelp.business (
business_id STRING,
name STRING,
address STRING,
city STRING,
state STRING,
postal_code STRING,
latitude DOUBLE,
longitude DOUBLE,
stars FLOAT,
review_count INT,
is_open INT,
attributes MAP<STRING, STRING>,
categories STRING, -- категории перечислены через запятую, считаем, что это массив
hours MAP<STRING, STRING> -- может быть пустым
)
ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe'
STORED AS TEXTFILE
LOCATION '/user/hive/yelp/business';
```

---
#### Распиливаем по таблицам, на которых будем строить аналитику

##### Центральная таблица

```sql
drop table yelp.business_core;
CREATE TABLE yelp.business_core AS
SELECT
business_id,
name,
address,
city,
state,
postal_code,
latitude,
longitude,
stars,
review_count,
is_open
FROM yelp.business;
```
##### Категории бизнеса

```sql
drop table yelp.business_category;
CREATE TABLE yelp.business_category AS
SELECT business_id, category
FROM yelp.business
LATERAL VIEW explode(split(categories, ', ')) category_table AS category;
```

##### Расписание работы заведения

```sql
drop table yelp.business_hours;
CREATE TABLE yelp.business_hours AS
SELECT
  business_id,
  day_of_week,
	  
  -- Время открытия
  CAST(split(split(working_hours, '-')[0], ':')[0] AS INT) AS open_hour,
  CAST(split(split(working_hours, '-')[0], ':')[1] AS INT) AS open_minute,

  -- Время закрытия
  CAST(split(split(working_hours, '-')[1], ':')[0] AS INT) AS close_hour,
  CAST(split(split(working_hours, '-')[1], ':')[1] AS INT) AS close_minute,

  -- Минуты с начала суток (чтобы можно было быстро считать сколько по времени работает заведение)
  CAST(split(split(working_hours, '-')[0], ':')[0] AS INT) * 60 +
  CAST(split(split(working_hours, '-')[0], ':')[1] AS INT) AS open_minutes,

  CAST(split(split(working_hours, '-')[1], ':')[0] AS INT) * 60 +
  CAST(split(split(working_hours, '-')[1], ':')[1] AS INT) AS close_minutes

FROM (
  SELECT business_id, day_of_week, working_hours
  FROM yelp.business
  LATERAL VIEW explode(hours) working_hours AS day_of_week, working_hours
) t
WHERE working_hours IS NOT NULL
```
##### Атрибуты заведения

```sql
drop table yelp.business_attributes;
CREATE TABLE yelp.business_attributes AS
SELECT
  business_id,
  --убираем результат кривой десериализации
  regexp_replace(attribute_key, "u'|'|\"", '') as attribute_key,
  attribute_value
FROM yelp.business
LATERAL VIEW explode(attributes) attribute_table AS attribute_key, attribute_value;
```

---
#### Витрины для superset
##### Смотрим популярность (кол-во) тех или иных категорий бизнеса по городам и их рейтинг:

```sql
drop table yelp.business_city_category_rating_agg;
CREATE TABLE yelp.business_city_category_rating_agg AS
SELECT
bc.city,
cat.category,
COUNT(*) AS business_count,
ROUND(AVG(bc.stars), 2) AS avg_rating
FROM yelp.business_core bc
JOIN yelp.business_category cat
ON bc.business_id = cat.business_id
GROUP BY bc.city, cat.category
ORDER BY business_count DESC;
```
##### Топ-3 бизнеса в каждой категории в разрезе городов:

```sql
drop table yelp.top_businesses_per_category;
CREATE TABLE yelp.top_businesses_per_category AS
SELECT *
FROM (
SELECT
bc.business_id,
bc.name,
cat.category,
bc.city,
bc.stars,
bc.review_count,
ROW_NUMBER() OVER (PARTITION BY cat.category ORDER BY bc.stars DESC, bc.review_count DESC) AS rn
FROM yelp.business_core bc
JOIN yelp.business_category cat ON bc.business_id = cat.business_id
) t
WHERE rn <= 3;
```
##### Категории, в которых доля заведений, где принимают карты <50%:

Если мы, например, делаем сервис безналичной оплаты, можно прийти в эти заведения с нашими предложениями

```sql
drop table yelp.business_category_creditcard_share;
CREATE TABLE yelp.business_category_creditcard_share AS
SELECT
cat.category,
COUNT(DISTINCT ba.business_id) AS businesses_with_info,
COUNT(DISTINCT CASE WHEN LOWER(ba.attribute_value) = 'true' THEN ba.business_id END) AS accepting_credit_cards,
ROUND(
COUNT(DISTINCT CASE WHEN LOWER(ba.attribute_value) = 'true' THEN ba.business_id END) * 100.0 /
COUNT(DISTINCT ba.business_id),
2
) AS acceptance_level
FROM yelp.business_attributes ba
JOIN yelp.business_category cat ON ba.business_id = cat.business_id
WHERE LOWER(ba.attribute_key) = 'businessacceptscreditcards'
GROUP BY cat.category
HAVING
ROUND(
COUNT(DISTINCT CASE WHEN LOWER(ba.attribute_value) = 'true' THEN ba.business_id END) * 100.0 /
COUNT(DISTINCT ba.business_id),
2
) < 50
ORDER BY acceptance_level DESC;
```
##### Влияние наличия летней веранды на рейтинг заведения в разрезе категорий:

```sql
drop table yelp.business_category_outdoor_rating;
CREATE TABLE yelp.business_category_outdoor_rating AS
SELECT
cat.category,
LOWER(ba.attribute_value) AS outdoor_seating,
ROUND(AVG(bc.stars), 2) AS avg_rating,
COUNT(*) AS business_count
FROM yelp.business_attributes ba
JOIN yelp.business_core bc ON ba.business_id = bc.business_id
JOIN yelp.business_category cat ON ba.business_id = cat.business_id
WHERE LOWER(ba.attribute_key) = 'outdoorseating'
GROUP BY cat.category, LOWER(ba.attribute_value)
ORDER BY avg_rating DESC;
```
##### Города, реализующие лучший сервис:

```sql
drop table yelp.best_city_avg_rate;
CREATE TABLE yelp.best_city_avg_rate AS
SELECT
bc.city,
ROUND(AVG(bc.stars), 2) as avg_rating
FROM yelp.business_core bc
GROUP BY bc.city
ORDER BY avg_rating DESC;
```