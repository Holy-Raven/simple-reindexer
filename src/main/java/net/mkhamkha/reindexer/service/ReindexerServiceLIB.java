package net.mkhamkha.reindexer.service;

import lombok.RequiredArgsConstructor;
import net.mkhamkha.reindexer.config.property.ReindexerProperty;
import net.mkhamkha.reindexer.exceptions.ReindexerException;
import net.mkhamkha.reindexer.model.Item;
import net.mkhamkha.reindexer.util.Utils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rt.restream.reindexer.NamespaceOptions;
import ru.rt.restream.reindexer.Query;
import ru.rt.restream.reindexer.Reindexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ru.rt.restream.reindexer.Query.Condition.*;

@Service
@RequiredArgsConstructor
public class ReindexerServiceLIB {

    private final ReindexerProperty reindexerProperty;
    private final Reindexer reindexer;

    // 1. Создание новой коллекции, или открытие по имени.
    public void createOrOpenCollection(String collectionName) {
        reindexer.openNamespace(collectionName, NamespaceOptions.defaultOptions(), Item.class);
    }

    // 2. Добавление строки
    public void addItem(String collectionName, Item itemLine) {
        createOrOpenCollection(collectionName);
        reindexer.upsert(collectionName, itemLine);
    }

    // 3. Загрузка файла
    @Transactional
    public void addFile(String collectionName, String fileName) {

        // Загрузка файла
        List<Item> items = Utils.loadFile(reindexerProperty.getFilePath(), fileName);

        //запись файла в бд
        items.forEach(item -> {
            try {
                addItem(collectionName, item);
            } catch (Exception e) {
                throw new ReindexerException(String.format("Ошибка записи строки id:%s в хранилище. " + e.getMessage(), item.getId()));
            }
        });
    }

    // 4. Выдача содержимого БД
    public List<Item> getAllItems(String collectionName, Integer page, Integer size) {

        Iterator<Item> iterator = reindexer.query(collectionName, Item.class)
                .execute();

        List<Item> allItems = new ArrayList<>();
        iterator.forEachRemaining(allItems::add);

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

    }

    // 5. Поиск в БД по строке с использованием EQ
    public List<Item> searchByText(String collectionName, String filter) {
        // Проверяем наличие фильтра и добавляем запрос с учетом условия EQ
        Query<Item> query = reindexer.query(collectionName, Item.class);

        if (filter != null && !filter.isEmpty()) {
            query.where("name", EQ, "=" + filter.toLowerCase() + "*"); // Точное вхождение, начало name соответсвует filter. Больше pattern в описании.
        }

        // Выполняем запрос
        Iterator<Item> iterator = query.execute();

        // Создаем список для хранения элементов
        List<Item> items = new ArrayList<>();
        iterator.forEachRemaining(items::add);

        return items;
    }

    // 6. Выдача размера БД
    public String getDdSize(String collectionName) {

        Iterator<Item> iterator = reindexer.query(collectionName, Item.class)
                .execute();

        List<Item> items = new ArrayList<>();
        iterator.forEachRemaining(items::add);

        return "В коллекции " + collectionName + " содержится " + items.size() + " item.";
    }
}
