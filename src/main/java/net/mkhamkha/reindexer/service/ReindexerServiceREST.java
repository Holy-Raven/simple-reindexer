package net.mkhamkha.reindexer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.mkhamkha.reindexer.component.ReindexerClient;
import net.mkhamkha.reindexer.config.property.ReindexerProperty;
import net.mkhamkha.reindexer.exceptions.ReindexerException;
import net.mkhamkha.reindexer.model.Item;
import net.mkhamkha.reindexer.util.Utils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReindexerServiceREST {

    private final ObjectMapper objectMapper;
    private final ReindexerClient reindexerClient;
    private final ReindexerProperty reindexerProperty;

    public void createCollection(String dbName, String collectionName) {
        // Создание базы данных
        Map<String, Object> dbMap = new HashMap<>();
        dbMap.put("name", dbName);
        JsonNode dbJson = objectMapper.valueToTree(dbMap);
        reindexerClient.createDb(dbJson);

        // Настройка коллекции
        Map<String, Object> namespaceMap = new HashMap<>();
        namespaceMap.put("name", collectionName);

        // Включение хранения данных
        Map<String, Object> storageOptions = new HashMap<>();
        storageOptions.put("enabled", true);
        namespaceMap.put("storage", storageOptions);

        // Индекс для поля `id`
        Map<String, Object> idIndexMap = new HashMap<>();
        idIndexMap.put("name", "id");
        idIndexMap.put("json_paths", new String[]{"id"});
        idIndexMap.put("field_type", "int");
        idIndexMap.put("index_type", "hash");
        idIndexMap.put("is_pk", true);
        idIndexMap.put("is_array", false);
        idIndexMap.put("is_dense", false);
        idIndexMap.put("is_sparse", false);
        idIndexMap.put("collate_mode", "none");
        idIndexMap.put("sort_order_letters", "");
        idIndexMap.put("expire_after", 0);

        // Настройка config для индекса id
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("enable_translit", true);
        configMap.put("enable_numbers_search", false);
        configMap.put("enable_warmup_on_ns_copy", false);
        configMap.put("enable_kb_layout", true);
        configMap.put("log_level", 0);
        configMap.put("merge_limit", 20000);
        configMap.put("extra_word_symbols", "-/+");

        // Настройка stop words, stemmers и synonyms
        configMap.put("stop_words", List.of("string"));
        configMap.put("stemmers", List.of("en", "ru"));
        configMap.put("synonyms", List.of(Map.of(
                "tokens", List.of("string"),
                "alternatives", List.of("string")
        )));

        // Параметры BM25, расстояний и других метрик
        configMap.put("bm25_boost", 1);
        configMap.put("bm25_weight", 0.1);
        configMap.put("distance_boost", 1);
        configMap.put("distance_weight", 0.5);
        configMap.put("term_len_boost", 1);
        configMap.put("term_len_weight", 0.3);
        configMap.put("position_boost", 1);
        configMap.put("position_weight", 0.1);
        configMap.put("full_match_boost", 1.1);
        configMap.put("partial_match_decrease", 15);
        configMap.put("min_relevancy", 0.05);
        configMap.put("max_typos", 2);
        configMap.put("max_typo_len", 15);
        configMap.put("max_rebuild_steps", 50);
        configMap.put("max_step_size", 4000);
        configMap.put("sum_ranks_by_fields_ratio", 0);

        // Настройка дополнительных полей в конфигурации
        configMap.put("fields", List.of(Map.of(
                "field_name", "string",
                "bm25_boost", 1,
                "bm25_weight", 0.1,
                "term_len_boost", 1,
                "term_len_weight", 0.3,
                "position_boost", 1,
                "position_weight", 0.1
        )));

        idIndexMap.put("config", configMap);

        // Добавление индекса в коллекцию
        List<Map<String, Object>> indexes = new ArrayList<>();
        indexes.add(idIndexMap);
        namespaceMap.put("indexes", indexes);

        // Преобразование в JsonNode и создание коллекции
        JsonNode namespaceNode = objectMapper.valueToTree(namespaceMap);
        reindexerClient.createNamespace(dbName, namespaceNode);
    }


    // 2. Добавление строки
    public void addItem(String dbName, String collectionName, Item item) {

        Map<String, Object> itemLineMap = new LinkedHashMap<>();
        itemLineMap.put("id", item.getId());
        itemLineMap.put("name", item.getName());

        JsonNode itemLineNode = objectMapper.valueToTree(itemLineMap);
        reindexerClient.addItem(dbName, collectionName, itemLineNode);
    }

    // 3. Загрузка файла
    @Transactional
    public void addFile(String dbName, String collectionName, String fileName) {

        // Создаем db и collection
        createCollection(dbName, collectionName);

        // Загрузка файла
        List<Item> items = Utils.loadFile(reindexerProperty.getFilePath(), fileName);

        //запись файла в бд
        items.forEach(item -> {
            try {
                addItem(dbName, collectionName, item);
            } catch (Exception e) {
                throw new ReindexerException(String.format("Ошибка записи строки id:%s в хранилище. " + e.getMessage(), item.getId()));
            }
        });
    }

    // 4. Выдача содержимого БД
    public List<Item> getAllItems(String dbName, String collectionName, Integer page, Integer size) {
        try {
            // Получаем все элементы из клиента
            String result = reindexerClient.getAllItems(dbName, collectionName);

            // Парсим JSON-ответ, чтобы получить список всех элементов
            List<Item> allItems = parseItems(result);

            // Проверяем, были ли заданы page и size
            if (page == null || size == null) {
                // Если параметры не заданы, возвращаем все элементы
                return allItems;
            }

            // Вычисляем начальный и конечный индекс для запрашиваемой страницы
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, allItems.size());

            // Если fromIndex превышает размер списка, возвращаем пустой список
            if (fromIndex >= allItems.size()) {
                return new ArrayList<>(); // Пустой список, если страница превышает количество элементов
            }

            // Возвращаем подсписок элементов для текущей страницы
            return allItems.subList(fromIndex, toIndex);
        } catch (Exception e) {
            throw new ReindexerException("Ошибка при получении элементов с постраничным выводом: " + e.getMessage());
        }
    }

    // 5. Поиск в БД по строке.
    public List<Item> searchByText(String dbName, String collectionName, String filter) {
        try {
            // Создаем фильтр с частичным совпадением и кодируем его
            String condition = "name LIKE '%" + filter + "%'";
            String encodedFilter = URLEncoder.encode(condition, "UTF-8").replace("+", "%20");

            // Выполняем запрос с закодированным фильтром
            String result = reindexerClient.searchByText(dbName, collectionName, encodedFilter);

            // Используем метод parseItems для обработки результата
            return parseItems(result);
        } catch (Exception e) {
            throw new ReindexerException("Ошибка поиска по тексту: " + e.getMessage());
        }
    }

    // 6. Выдача размера БД
    public String getDdSize(String dbName, String collectionName) {

        String result = reindexerClient.getAllItems(dbName, collectionName);
        List<Item> allItems = parseItems(result);

        return "Базе данных: " + dbName + " содержит " + allItems.size() + " item.";
    }

    // Внутренний метод сервиса, принимает JSON-строку и возвращает список объектов Item
    private List<Item> parseItems(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<Item> items = new ArrayList<>();

            if (rootNode.has("items")) {
                JsonNode itemsNode = rootNode.get("items");
                if (itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        JsonNode idNode = itemNode.get("id");
                        JsonNode textNode = itemNode.get("name");

                        if (idNode != null && idNode.isInt() && textNode != null && textNode.isTextual()) {
                            int id = idNode.asInt();
                            String text = textNode.asText();
                            items.add(new Item(id, text));
                        }
                    }
                }
            } else {
                throw new ReindexerException("Неверная структура JSON или данные отсутствуют");
            }

            return items;
        } catch (Exception e) {
            throw new ReindexerException("Ошибка при обработке JSON-ответа: " + e.getMessage());
        }
    }
}
