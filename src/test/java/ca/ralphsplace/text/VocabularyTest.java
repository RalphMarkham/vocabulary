package ca.ralphsplace.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


class VocabularyTest {

    @Test
    void wordCount() throws URISyntaxException {
        Path path = Paths.get(Vocabulary.class.getResource("/t8.shakespeare.txt").toURI());
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.wordCount(path).subscribe(map -> Assertions.assertTrue(map.get(27643).contains("the")));
    }
}