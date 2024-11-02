package net.mkhamkha.reindexer.controller;

import lombok.RequiredArgsConstructor;
import net.mkhamkha.reindexer.model.Item;
import net.mkhamkha.reindexer.service.ReindexerServiceLIB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gra/lib")
@RequiredArgsConstructor
public class ReindexerControllerLIB {

    private final ReindexerServiceLIB reindexerService;

    @PostMapping("/{collectionName}")
    public ResponseEntity<String> createCollection(@PathVariable String collectionName) {
        reindexerService.createOrOpenCollection(collectionName);
        return ResponseEntity.ok("Коллекция  " + collectionName + " готова к использованию");
    }

    @PostMapping("/{collectionName}/add")
    public ResponseEntity<String> addItem(@PathVariable String collectionName, @RequestBody Item item) {
        reindexerService.addItem(collectionName, item);
        return ResponseEntity.ok("Элемент добавлен успешно");
    }

    @PostMapping("/{collectionName}/upload")
    public ResponseEntity<String> uploadFile(@PathVariable String collectionName, @RequestParam String fileName) {
        reindexerService.addFile(collectionName, fileName);
        return ResponseEntity.ok("Файл  " + fileName + " успешно загружен в коллекцию " + collectionName);
    }

    @GetMapping("/{collectionName}")
    public ResponseEntity<List<Item>> getAllItems(@PathVariable String collectionName,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size) {
        List<Item> items = reindexerService.getAllItems(collectionName, page, size);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{collectionName}/db-size")
    public ResponseEntity<String> getDbSize(@PathVariable String collectionName) {
        String dbSize = reindexerService.getDdSize(collectionName);
        return ResponseEntity.ok(dbSize);
    }

    @GetMapping("/{collectionName}/search")
    public ResponseEntity<List<Item>> searchItems(@PathVariable String collectionName,
                                                  @RequestParam String filter) {
        List<Item> items = reindexerService.searchByText(collectionName, filter);
        return ResponseEntity.ok(items);
    }
}
