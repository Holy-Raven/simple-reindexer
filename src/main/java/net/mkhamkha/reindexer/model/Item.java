package net.mkhamkha.reindexer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rt.restream.reindexer.IndexType;
import ru.rt.restream.reindexer.annotations.FullText;
import ru.rt.restream.reindexer.annotations.Reindex;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Reindex(name = "id", isPrimaryKey = true)
    private Integer id;

    @Reindex(name = "name")
//    @Reindex(name = "name", type = IndexType.TEXT)
//    @FullText
    private String name;
}
