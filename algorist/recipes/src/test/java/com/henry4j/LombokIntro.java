package com.henry4j;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static lombok.Yield.yield;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.AccessLevel;
import lombok.Action;
import lombok.Actions.Action1;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.ExtensionMethod;
import lombok.Function;
import lombok.Functions.Function1;
import lombok.Functions.Function2;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Predicates.Predicate1;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import lombok.val;
import org.apache.http.*;
import org.apache.http.client.fluent.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;

@ExtensionMethod({ LombokIntro.Extensions.class })
public class LombokIntro {
    public class BaseTrie<K, V> { // Usually, K is Character.
        @Getter(AccessLevel.PROTECTED)
        private final Map<K, Trie<K, V>> children = Maps.newHashMap();
        @Getter @Setter @Accessors(fluent = true)
        private V value;

        public BaseTrie<K, V> map(CharSequence keys) { throw new UnsupportedOperationException("No implementation yet."); }
        public BaseTrie<K, V> map(Iterable<K> keys) { throw new UnsupportedOperationException("No implementation yet."); }
    }

    @Test
    public void testTrie() {
        val trie = new BaseTrie<Character, List<Integer>>();
        val list = trie.map("bananas").value(new ArrayList<Integer>()).value();
        list.add(10);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter // for all non-static fields
    public class ClassicEntry<K, V> {
        private K key;
        @Setter @NonNull // for null check in the setter
        private V value;
        @Getter(AccessLevel.NONE)
        private int olv; // optimistic lock value
    }

    @Test(expected=NullPointerException.class)
    public void testClassicEntry() {
        val entry = new ClassicEntry<String, Integer>();
        entry.setValue(null);
    }

    @RequiredArgsConstructor(staticName="of")
    @Getter @Accessors(fluent = true) // http://codemonkeyism.com/generation-java-programming-style/
    public static class Point {
        final private int x;
        final private int y;
    }

    @RequiredArgsConstructor(staticName="of")
    @Getter @Accessors(fluent = true)
    public static class Pair<U, V> { // design patterns: immutable data structure, and fluent interface.
        final private U first;
        final private V second;
    }

    @Test
    public void testFluentPair() {
        val duet = new Pair<String, BitSet>("anana", new BitSet());
        val pair = Pair.of("anana", new BitSet());
        duet.second().set(0);
        assertThat(pair.first(), equalTo("anana"));
    }

    @Getter @Setter @Accessors(fluent = true)
    public static class DList<E> {
        private DList<E> prev;
        private DList<E> next;
        private E value;

        public static <E> DList<E> of(E e) {
            return new DList<E>().value(e);
        }
    }

    public String mostBeautifulUnique(String s) {
        DList<Character> head = null, tail = null;
        val map = new HashMap<Character, DList<Character>>();
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (null == head) {
                map.put(c, head = tail = DList.of(c));
            } else if (!map.containsKey(c)) {
                map.put(c, head = head.prev(DList.of(c).next(head)).prev());
            } else {
                if (c > head.value()) {
                    val node = map.get(c);
                    map.put(c, head = head.prev(DList.of(c)).prev());
                    node.prev().next(node.next());
                    if (tail != node)
                       node.next().prev(node.prev());
                    else
                       (tail = tail.prev()).next(null);
                }
            }
        }
        val sb = new StringBuilder();
        for (DList<Character> node = head; null != node; node = node.next()) {
            sb.append(node.value());
        }
        return sb.toString();
    }

    @Test
    public void testMostBeautifulUnique() {
        assertThat(mostBeautifulUnique("acbdba"), equalTo("cdba"));
        assertThat(mostBeautifulUnique("acdcab"), equalTo("dcab"));
        assertThat(mostBeautifulUnique("bcdaca"), equalTo("bdca"));
    }

    @Data // a shortcut for @Getter, @Setter, @ToString, @EqualsAndHashCode
    public class DataEntry {
        private boolean weighted;
        private boolean isDirected;
        private Boolean reachable;
        @Setter(AccessLevel.NONE) @Accessors(fluent = true)
        private boolean hasCycle;
    }

    @Test
    public void testBooleanProperties() {
        val entry = new DataEntry();
        entry.isWeighted();
        entry.isDirected();
        entry.getReachable(); // 'null' means it is unknown until now.
        entry.hasCycle(); // should not be 'getHasCycle'
    }

    public class LazyEntry {
        @Getter(lazy = true) // DCL-based lazy getter http://projectlombok.org/features/GetterLazy.html
        private final double[] cached = expensive();

        private double[] expensive() {
            double[] result = new double[1000000];
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.asin(i);
            }
            return result;
        }
    }

    @Test
    public void testCleanup() throws IOException {
        @Cleanup InputStream in = new FileInputStream("");
        @Cleanup OutputStream out = new FileOutputStream("");
        byte[] b = new byte[10000];
        while (true) {
            int r = in.read(b);
            if (r == -1) break;
            out.write(b, 0, r);
        }
    }

    public static class Extensions {
        public static <U, V> Iterable<V> map(final Iterable<U> from, final Function1<U, V> function) {
            for (U u : from) {
                yield(function.apply(u));
            }
        }

        public static <E> Iterable<E> select(final Iterable<E> from, final Predicate1<E> predicate) {
            for (E e : from) {
                if (predicate.apply(e)) {
                    yield(e);
                }
            }
        }

        public static <E> Iterable<E> reject(final Iterable<E> from, final Predicate1<E> predicate) {
            for (E e : from) {
                if (!predicate.apply(e)) {
                    yield(e);
                }
            }
        }

        public static <E> void each(final Iterable<E> from, final Action1<E> process) {
            for (E e : from) {
                process.apply(e);
            }
        }

        public static <E> Iterable<E> times(final E e, final int n) {
            for (int i = 0; i < n; i++) {
                yield(e);
            }
        }

        public static <U, V> com.google.common.base.Function<U, V> guava(final Function1<U, V> f1) {
            return new com.google.common.base.Function<U, V>() {
                public V apply(U u) {
                    return f1.apply(u);
                }
            };
        }

        public static <E> Comparator<E> comparator(final Function2<E, E, Integer> f2) {
            return new Comparator<E>() {
                public int compare(E a, E b) {
                    return f2.apply(a, b);
                }
            };
        }

        public static <E> Ordering<E> ordering(final Comparator<E> comparator) {
            return Ordering.from(comparator);
        }

        public static <V, K extends Comparable<K>> ImmutableList<V> maxima(Iterable<V> values, Function1<V, K> by) {
            val indexed = Multimaps.index(values, by.guava());
            val indices = indexed.keySet();
            return indexed.get(Ordering.natural().max(indices));
        }

        public static <V, K extends Comparable<K>> ImmutableList<V> minima(Iterable<V> values, Function1<V, K> by) {
            val indexed = Multimaps.index(values, by.guava());
            val indices = indexed.keySet();
            return indexed.get(Ordering.natural().min(indices));
        }
    }

    @Test
    public void testExtensionsAndFunctions() {
        val list = Arrays.asList("bananas", "sananab", "abab", "baba");

        val max = Ordering.from(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.length() - s2.length();
            }
        }).max(list);
        val maxima = Extensions.maxima(list, new Function1<String, Integer>() {
            public Integer apply(String s) {
                return s.length();
            }
        });

        Function2<String, String, Integer> compare = compare();
        Comparator<String> comparator = Extensions.comparator(compare);
        assertThat(comparator.compare("abc", "xyz"), equalTo(-1));

        val min = compare().comparator().ordering().min(list);
        val minima = list.minima(length());

        assertThat(max, equalTo("bananas"));
        assertThat(maxima, equalTo(Arrays.asList("bananas", "sananab")));
        assertThat(min, equalTo("abab"));
        assertThat(minima, equalTo(Arrays.asList("abab", "baba")));
    }

    @Function
    public int compare(String s1, String s2) {
        return s1.length() - s2.length();
    }

    @Function
    public int length(String s) {
        return s.length();
    }

    @Log4j
    @ExtensionMethod({ Extensions.class })
    public static class AsScheduled {
        @Autowired
        private AsAsync asAsync;
        @Autowired
        private AsAdvised asAdvised;
        @Setter
        private Map<SqsQueue, Integer> maxSqsRetrievalsPerSecByQueue;

        @Scheduled(fixedRate = 1000)
        public void consumeAll() {
            val queues = maxSqsRetrievalsPerSecByQueue.keySet();
            copyOf(queues.map(consumeAsync())).each(joinQuietly());
        }

        @Function
        private Future<Boolean> consumeAsync(SqsQueue queue) {
            boolean exit;
            if (exit = asAdvised.tryConsumeMessages(queue)) {
                val n = maxSqsRetrievalsPerSecByQueue.get(queue) - 1;
                val sleep = 900 / n; // 'sleep' helps uniformly distribute asynchronous task submissions.
                copyOf(Pair.of(queue, sleep).times(n).map(consumeAsyncAsleep())).each(joinQuietly());
            }
            return new AsyncResult(exit);
        }

        @Action
        private void joinQuietly(Future<Boolean> f) {
            try {
                f.get();
            } catch (Exception e) {
                log.error(String.format("Exception uncaught!!!"), e);
            }
        }

        @Function
        private Future<Boolean> consumeAsyncAsleep(final Pair<SqsQueue, Integer> queueSleep) {
            try {
                val future = asAsync.consumeMessagesAsync(queueSleep.first());
                Thread.sleep(queueSleep.second());
                return future;
            } catch (InterruptedException e) {
                throw new RuntimeException("UNCHECKED: this bug should go unhandled.", e);
            }
        }
    }

    public static class AsAsync {
        @Autowired
        private AsAdvised asAdvised;

        @Async
        public Future<Boolean> consumeMessagesAsync(SqsQueue queue) {
            return new AsyncResult<Boolean>(asAdvised.tryConsumeMessages(queue));
        }
    }

    public static class AsAdvised {
        public Boolean tryConsumeMessages(SqsQueue queue) {
            return true; // TODO: some IO and computation.
        }
    }

    public static enum SqsQueue {
        CAM_BalanceChange,
        IW_ActionRecord,
        CROW_ReversalNotification,
        OWEN_ShipmentCompletion, OWEN_CompletedShipmentUpdate;
    }

    // We get synchronized on fields $lock = Object[0] and $Locks = new Object[0] for instance & static methods.
    public static class SynchronizedRight {
        private final Object readLock = new Object[0];

        @Synchronized
        public static void hello() {
            System.out.println("world");
        }

        @Synchronized
        public int answerToLife() {
            return 42;
        }

        @Synchronized("readLock")
        public void foo() {
            System.out.println("bar");
        }
    }

    @Log4j
    public static class SneakyThrowsOutOfReasoning implements Runnable {
        @SneakyThrows(UnsupportedEncodingException.class) // b/c there is no reason throw & catch impossible exceptions
        public String utf8ToString(byte[] bytes) {
            return new String(bytes, "UTF-8");
        }

        // b/c throwing a runtime exception from a needlessly strict interface only obscures the real cause of the issue.
        // @SneakyThrows({ IOException.class })
        public void run() {
            try {
                // Apache Fluent Http Client http://hc.apache.org/httpcomponents-client-ga/fluent-hc/index.html
                String result1 = Request.Get("http://somehost/")
                        .version(HttpVersion.HTTP_1_1)
                        .connectTimeout(1000)
                        .socketTimeout(1000)
                        .viaProxy(new HttpHost("myproxy", 8080))
                        .execute().returnContent().asString();
                log.debug(result1);
            } catch (IOException e) {
                throw new RuntimeException("UNCHECKED: this bug should go unhandled.", e);
            }
        }
    }
}