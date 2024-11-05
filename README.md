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

### Пример паттернов поиска
###### 1. Поиск по началу слова (термин с *)
    public List<Item> searchWithPrefix(String collectionName, String prefix) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, prefix + "*");
        return query.execute().toList();
    }
   Описание: Этот запрос ищет все записи, где поле name начинается с заданного префикса (termina*).

###### 2. Фаззовый поиск (слово с ~ для учета опечаток)
    public List<Item> searchWithFuzzy(String collectionName, String term) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, term + "~");
        return query.execute().toList();
    }
   Описание: Этот запрос ищет записи, в которых поле name примерно совпадает со словом term, с учетом одной опечатки (black~).

###### 3. Бустинг результатов (слово^2)
    public List<Item> searchWithBoost(String collectionName, String term1, String term2) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, term1 + " " + term2 + "^2");
        return query.execute().toList();
    }
   Описание: Поиск с приоритетом для одного из слов. Результаты, содержащие term2, будут иметь больший вес.

###### 4. Обязательное присутствие слова (+слово)
    public List<Item> searchWithMandatoryTerm(String collectionName, String term1, String term2) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, term1 + " +" + term2);
        return query.execute().toList();
    }
   Описание: Результаты будут содержать term1, но только если term2 присутствует обязательно (fox +fast).

###### 5. Поиск точной фразы ("фраза")
    public List<Item> searchExactPhrase(String collectionName, String phrase) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, "\"" + phrase + "\"");
        return query.execute().toList();
    }
   Описание: Поиск документов, содержащих точную фразу ("one two").

###### 6. Поиск слов в пределах расстояния ("фраза"~N)
    public List<Item> searchPhraseWithinDistance(String collectionName, String word1, String word2, int distance) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, "\"" + word1 + " " + word2 + "\"~" + distance);
        return query.execute().toList();
    }

   Описание: Поиск документов, где слова расположены на расстоянии до distance слов друг от друга ("one two"~5).

###### 7. Поиск в конкретном поле с бустингом
    public List<Item> searchInFieldWithBoost(String collectionName, String fieldName, String term, double boost) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where(fieldName, Query.Condition.EQ, "@" + fieldName + "^" + boost + " " + term);
        return query.execute().toList();
    }
   Описание: Поиск только в поле name со словом rush, где совпадения по этому полю получают больший вес (@name^1.5,* rush).

###### 8. Исключение фраз (слово -"фраза")
    public List<Item> searchWithExclusion(String collectionName, String term, String excludedPhrase) {
        Query<Item> query = reindexer.query(collectionName, Item.class);
        query.where("name", Query.Condition.EQ, term + " -" + "\"" + excludedPhrase + "\"");
        return query.execute().toList();
    }
   
Описание: Результаты будут содержать term, но исключать любые документы с excludedPhrase (one -"phrase example").

Как использовать
Каждый из этих примеров использует структуру query.where() с конкретными условиями, адаптированными под ваш шаблон. Подобные паттерны позволяют гибко настраивать запросы к базе данных Reindexer, улучшая релевантность и эффективность поиска.