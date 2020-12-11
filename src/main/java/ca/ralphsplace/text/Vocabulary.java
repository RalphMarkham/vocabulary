package ca.ralphsplace.text;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Vocabulary {
    
    private static final Integer ONE = 1;

    public Mono<Map<Integer, Collection<String>>> wordCount(Path path) {
        BiConsumer<Map<String, Integer>, String> accumulator = (map, word) -> map.merge(word, ONE, Integer::sum);

        return Flux.using(() -> Files.lines(path), Flux::fromStream, Stream::close)
                .flatMap(str -> Flux.fromStream(Arrays.stream(str.split("\\s"))
                .map(s -> s.toLowerCase().replaceAll("[^a-z]", ""))))
                .filter(s -> !s.isEmpty())
                .collect(HashMap::new, accumulator)
                .flux()
                .flatMap(m -> Flux.fromStream(m.entrySet().stream()))
                .collectMultimap(Map.Entry::getValue, Map.Entry::getKey);
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: Vocabulary path");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.wordCount(path).subscribe(map -> map.keySet()
                .stream()
                .sorted((o1, o2) -> Integer.compare(o2, o1))
                .forEach(count ->
                        map.get(count).forEach(w -> System.out.println(count + "\t" + w))));
    }
}
