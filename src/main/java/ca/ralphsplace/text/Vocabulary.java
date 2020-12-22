package ca.ralphsplace.text;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Vocabulary {
    
    private static final Integer ONE = 1;
    private static final BiConsumer<Map<String, Integer>, String> accumulator = (map, word) -> map.merge(word, ONE, Integer::sum);

    public Mono<Map<Integer, Collection<String>>> asyncWordCount(final Stream<String> stream) {

        return Flux.using(() -> stream, Flux::fromStream, Stream::close)
                .flatMap(str -> Flux.fromStream(Arrays.stream(str.split("\\s"))
                .map(s -> s.toLowerCase().replaceAll("[^a-z]", ""))))
                .filter(s -> !s.isEmpty())
                .collect(HashMap::new, accumulator)
                .flux()
                .flatMap(m -> Flux.fromStream(m.entrySet().stream()))
                .collectMultimap(Map.Entry::getValue, Map.Entry::getKey);
    }

    public Map<String,Long> wordCount(final Stream<String> stream) {
        return stream.parallel()
                .flatMap(str -> Arrays.stream(str.split("\\s")))
                .map(s -> s.toLowerCase().replaceAll("[^a-z]", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingByConcurrent(Function.identity(),
                                                        ConcurrentHashMap::new,
                                                        Collectors.counting()));
    }

    public <K,V> Map<K, List<V>> aggregateMap(final Map<V,K> map) {
        return  map.entrySet()
                    .stream()
                    .parallel()
                    .collect(Collectors.groupingByConcurrent(Map.Entry<V,K>::getValue,
                            ConcurrentSkipListMap::new,
                            Collectors.mapping(Map.Entry<V,K>::getKey,Collectors.toList()))
                );
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: Vocabulary path");
            System.exit(1);
        }

        final Path path = Paths.get(args[0]);
        final Vocabulary vocabulary = new Vocabulary();

        try {
            vocabulary.asyncWordCount(Files.lines(path)).subscribe(map -> map.keySet()
                    .stream()
                    .sorted((o1, o2) -> Integer.compare(o2, o1))
                    .forEach(count ->
                            map.get(count).forEach(w -> System.out.println(count + "\t" + w))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Mono<Map<Long,List<String>>> m = Mono.just(vocabulary.aggregateMap(vocabulary.wordCount(Files.lines(path))));
            m.subscribe(map -> map.keySet()
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2, o1))
                    .forEach(count ->
                            map.get(count).forEach(w -> System.out.println(count + "\t" + w)))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
