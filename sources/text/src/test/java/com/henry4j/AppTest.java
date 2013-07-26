package com.henry4j;

import static java.lang.Math.max;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.clustering.lda.cvb.TopicModel;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.henry4j.commons.base.Actions.Action2;

public class AppTest {
    @Test
    public void test() {
        // hadoop dfs -get /tmp/mahout-work-hylee/reuters-out-seqdir-sparse-lda/dictionary.file-0 /tmp/dictionary.file-0
        // hadoop dfs -get /tmp/mahout-work-hylee/reuters-lda-model/model-20 /tmp/model-n
        // hadoop dfs -getmerge /tmp/mahout-work-hylee/reuters-out-seqdir-sparse-lda/tfidf-vectors /tmp/tfidf-vectors

        val conf = new Configuration();
        val dictionary = readDictionary(new Path("/tmp/dictionary.file-0"), conf);
        assertThat(dictionary.length, equalTo(41807));

        // tfidf_vector represents a document in RandomAccessSparseVector.
        val tfidf_vector = readTFVectorsInRange(new Path("/tmp/tfidf-vectors"), conf, 0, 1)[0].getSecond();
        assertThat(tfidf_vector.size(), equalTo(41807));

        // reads 'model' dense matrix (20 x 41K), and in 'topicSum' dense vector.
        TopicModel model = readModel(dictionary, new Path("/tmp/model-n"), conf);
        assertThat(model.getNumTopics(), equalTo(20));
        assertThat(model.getNumTerms(), equalTo(41807));

        
        // model.trainDocTopicModel(original, topics, docTopicModel);
        // /tmp/tfidf
        // model.infer(original, docTopics);

//        Vector doc = null;
//        Vector docTopics = new DenseVector(numTopics).assign(1.0/numTopics);
//        Matrix docModel = new SparseRowMatrix(numTopics, doc.get().size());
//    
//        SequenceFileReader<, V>
    }

    // http://mail-archives.apache.org/mod_mbox/mahout-user/201205.mbox/%3CCAKA-QbDg4DR3RTbv8KoYnMOnLXkbbqaXji0+WtrL5UdSdsKbOA@mail.gmail.com%3E
    // http://mail-archives.apache.org/mod_mbox/mahout-user/201205.mbox/%3CCACYXym-0zg3zPor-SmpWr=D210B_6-YeNyyNtddNWpiU_otDrA@mail.gmail.com%3E

    @SneakyThrows({ IOException.class })
    private static Pair<String, Vector>[] readTFVectorsInRange(Path path, Configuration conf, int offset, int length) {
        val seq = new SequenceFile.Reader(FileSystem.get(conf), path, conf);
        val documentName = new Text();
        @SuppressWarnings("unchecked")
        Pair<String, Vector>[] vectors = new Pair[length];
        VectorWritable vector = new VectorWritable();
        for (int i = 0; i < offset + length && seq.next(documentName, vector); i++) {
            if (i >= offset) {
                vectors[i - offset] = Pair.of(documentName.toString(), vector.get());
            }
        }
        return vectors;
    }

    @SneakyThrows({ IOException.class })
    private static TopicModel readModel(String[] dictionary, Path path, Configuration conf) {
//        int numTopics = 20;
//        int numTerms = 41807;
        double alpha = 0.0001; // default: doc-topic smoothing
        double eta = 0.0001; // default: term-topic smoothing
        double modelWeight = 1f;
        return new TopicModel(conf, eta, alpha, dictionary, 1, modelWeight, listModelPath(path, conf));
    }

    @SneakyThrows({ IOException.class })
    private static Path[] listModelPath(Path path, Configuration conf) {
        val statuses = FileSystem.get(conf).listStatus(path, PathFilters.partFilter());
        val modelPaths = new Path[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            modelPaths[i] = new Path(statuses[i].getPath().toUri().toString());
        }
        return modelPaths;
    }

    @SneakyThrows({ IOException.class })
    private static String[] readDictionary(Path path, Configuration conf) {
        val term = new Text();
        val id = new IntWritable();
        val reader = new SequenceFile.Reader(FileSystem.get(conf), path, conf);
        val termIds = ImmutableList.<Pair<String, Integer>>builder();
        int maxId = 0;
        while (reader.next(term, id)) {
            termIds.add(Pair.of(term.toString(), id.get()));
            maxId = max(maxId, id.get());
        }
        String[] terms = new String[maxId + 1];
        for (val termId : termIds.build()) {
            terms[termId.getSecond().intValue()] = termId.getFirst().toString();
        }
        return terms;
    }

    @Test
    // http://stackoverflow.com/questions/17851806/how-can-i-infer-a-new-document-against-mahout-topicmodel-output
    public void stackoverflow() {
//        TopicModel model = new TopicModel();
//
//        Vector documentInTermFrequency = new RandomAccessSparseVector();
//        documentInTermFrequence.setQuick(termIdX, 10);
//        documentInTermFrequence.setQuick(termIdY, 20);

//        Vector docTopic = new DenseVector(new Double[10] { 0.1, 0.1, ..., 0.1 });
          // 0.1 probabilities
//        Vector documentTopicInference = model.infer(documentInTermFrequence, docTopic);
    }
}