package net.mkhamkha.reindexer.component;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ReindexerClient", url = "${reindexer-property.base-url}")
public interface ReindexerClient {

    // Метод для создания db
    @PostMapping(value = "/db", consumes = "application/json")
    void createDb(@RequestBody JsonNode db);

    // Метод для создания namespace
    @PostMapping(value = "/db/{dbName}/namespaces", consumes = "application/json")
    void createNamespace(@PathVariable("dbName") String dbName,
                         @RequestBody JsonNode namespace);

    // Метод для добавления элемента в namespace
    @PostMapping(value = "/db/{dbName}/namespaces/{collectionName}/items", consumes = "application/json")
    void addItem(@PathVariable("dbName") String dbName,
                 @PathVariable("collectionName") String collectionName,
                 @RequestBody JsonNode item);

    // Метод для получения всех элементов из namespace
    @GetMapping(value = "/db/{dbName}/namespaces/{collectionName}/items", consumes = "application/json")
    String getAllItems(@PathVariable("dbName") String dbName,
                       @PathVariable("collectionName") String collectionName);

    // Метод для элемента по части слова
    @GetMapping("/db/{dbName}/namespaces/{collectionName}/items")
    String searchByText(@PathVariable("dbName") String dbName,
                        @PathVariable("collectionName") String collectionName,
                        @RequestParam("filter") String filter);
}
