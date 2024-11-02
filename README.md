### Simple Reindexer Service

* Java 17
* Spring boot 3.0

###### Docker container runner
docker run --name reindexer -p 9088:9088 -p 6534:6534 -it reindexer/reindexer

###### Before start
Необходимо создать bd. Имя БД прописать в MainConfig в Bean нашего reindexer.

curl --location 'http://localhost:9088/api/v1/db' \
--header 'Content-Type: application/json' \
--data '{
"name": "bb",
"storage": {}
}'