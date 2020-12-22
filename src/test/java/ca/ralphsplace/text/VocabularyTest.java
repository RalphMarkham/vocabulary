package ca.ralphsplace.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


class VocabularyTest {

    @Test
    void wordCount() throws URISyntaxException, IOException {
        Path path = Paths.get(Vocabulary.class.getResource("/t8.shakespeare.txt").toURI());
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.asyncWordCount(path).subscribe(map -> Assertions.assertTrue(map.get(27643).contains("the")));
        Assertions.assertTrue(vocabulary.aggregateMap(vocabulary.wordCount(Files.lines(path))).get(27643L).contains("the"));
    }
}