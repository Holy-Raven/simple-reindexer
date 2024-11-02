package net.mkhamkha.reindexer.util;

import net.mkhamkha.reindexer.exceptions.ReindexerException;
import net.mkhamkha.reindexer.model.Item;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Item> loadFile(String path, String fileName) {

        List<Item> result = new ArrayList<>();

        // Чтение файла
        try (BufferedReader reader = new BufferedReader(new FileReader(new ClassPathResource(path + fileName).getFile()))) {

            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                result.add(new Item(id++, line));
            }
        } catch (IOException e) {
            throw new ReindexerException("Ошибка чтения файла. " + e.getMessage());
        }
        return result;
    }

}
