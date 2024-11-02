package net.mkhamkha.reindexer.controller;

import lombok.RequiredArgsConstructor;
import net.mkhamkha.reindexer.model.Item;
import net.mkhamkha.reindexer.service.ReindexerServiceREST;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gra/rest")
public class ReindexerControllerREST {

    private final ReindexerServiceREST reindexerService;

    @PostMapping("/create-collection")
    public ResponseEntity<String> createCollection(@RequestParam String dbName,
                                                   @RequestParam String collectionName) {
        reindexerService.createCollection(dbName, collectionName);
        return ResponseEntity.ok("Коллекция  " + collectionName + " готова к использованию");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String dbName,
                                             @RequestParam String collectionName,
                                             @RequestParam String fileName) {
        reindexerService.addFile(dbName, collectionName, fileName);
        return ResponseEntity.ok("Файл  " + fileName + " успешно загружен в коллекцию " + collectionName);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchByText(@RequestParam String dbName,
                                                   @RequestParam String collectionName,
                                                   @RequestParam String query) {
        List<Item> result = reindexerService.searchByText(dbName, collectionName, query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/db/{dbName}/namespaces/{collectionName}/items")
    public ResponseEntity<List<Item>> getAllItems(@PathVariable String dbName,
                                                  @PathVariable String collectionName,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        List<Item> items = reindexerService.getAllItems(dbName, collectionName, page, size);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/db/{dbName}/namespaces/{collectionName}/db-size")
    public ResponseEntity<String> getDbSize(@PathVariable String dbName,
                                            @PathVariable String collectionName) {
        // Вызываем метод сервиса
        String dbSize = reindexerService.getDdSize(dbName, collectionName);
        return ResponseEntity.ok(dbSize);
    }
}
