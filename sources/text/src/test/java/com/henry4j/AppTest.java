package com.henry4j;

import static java.lang.Math.max;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

// References:
// http://mail-archives.apache.org/mod_mbox/mahout-user/201205.mbox/%3CCAKA-QbDg4DR3RTbv8KoYnMOnLXkbbqaXji0+WtrL5UdSdsKbOA@mail.gmail.com%3E
// http://mail-archives.apache.org/mod_mbox/mahout-user/201205.mbox/%3CCACYXym-0zg3zPor-SmpWr=D210B_6-YeNyyNtddNWpiU_otDrA@mail.gmail.com%3E

public class AppTest {
    /*
     * This program needs a dictionary (of terms), a document (in tfidf vector), and an LDA model (built w/ tfidf) as follows:
     * 
     * hadoop dfs -get /tmp/mahout-work-$USER/reuters-out-seqdir-sparse-lda/dictionary.file-0 /tmp/dictionary
     * hadoop dfs -get /tmp/mahout-work-$USER/reuters-out-seqdir-sparse-lda/tfidf-vectors /tmp/tfidf-vectors
     * hadoop dfs -get /tmp/mahout-work-$USER/reuters-lda-model/model-20 /tmp/lda-model-splits
     * 
     * # or,
     * curl -o /tmp/dictionary -kL https://dl.dropboxusercontent.com/u/47820156/mahout/dictionary
     * curl -o /tmp/lda-model-splits/part-r-0000\#1 -kL https://dl.dropboxusercontent.com/u/47820156/mahout/lda-model-splits/part-r-0000[0-9]
     * --create-dir
     * curl -o /tmp/tfidf-vectors -kL https://dl.dropboxusercontent.com/u/47820156/mahout/tfidf-vectors
     */

    @Test
    public void testDocumentTopicInferenceForNewDocsOverReuters() {
        val conf = new Configuration();
        val dictionary = readDictionary(new Path("/tmp/dictionary"), conf);
        assertThat(dictionary.length, equalTo(41807));

        // reads 'model' dense matrix (20 x 41K), and in 'topicSum' dense vector.
        TopicModel model = readModel(dictionary, new Path("/tmp/lda-model-splits"), conf);
        assertThat(model.getNumTopics(), equalTo(20));
        assertThat(model.getNumTerms(), equalTo(41807));

        val doc = takeOnlineDocument(conf);
        Vector docTopics = new DenseVector(new double[model.getNumTopics()]).assign(1.0 / model.getNumTopics());
        Matrix docTopicModel = new SparseRowMatrix(model.getNumTopics(), doc.size());

        for (int i = 0; i < 20 /* maxItrs */; i++) {
            model.trainDocTopicModel(doc, docTopics, docTopicModel);
            System.out.println(strip(docTopics, 0.1).toString());
        }

        // tests against doc-topic inference from training
        assertThat(Math.round(docTopics.get(0) - 31), lessThanOrEqualTo(1L));
        assertThat(Math.round(docTopics.get(3) - 22), lessThanOrEqualTo(1L));
        assertThat(Math.round(docTopics.get(7) - 22), lessThanOrEqualTo(1L));
        assertThat(Math.round(docTopics.get(10) - 13), lessThanOrEqualTo(1L));
    }

    private static Vector strip(Vector v, double min) {
        Vector sv = new SequentialAccessSparseVector(v.size());
        for (val e : v.all()) {
            if (e.get() > min) {
                sv.set(e.index(), e.get());
            }
        }
        return sv;
    }

    private static Vector takeOnlineDocument(Configuration conf) {
        // tfidf_vector represents a document in RandomAccessSparseVector.
        // TODO: make a TFIDF vector out of an online string agaist dictionary, and frequency files.
        val tfidf_vector = readTFVectorsInRange(new Path("/tmp/tfidf-vectors"), conf, 0, 1)[0].getSecond();
        assertThat(tfidf_vector.size(), equalTo(41807));
        return tfidf_vector;
    }

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
        double alpha = 0.0001; // default: doc-topic smoothing
        double eta = 0.0001; // default: term-topic smoothing
        double modelWeight = 1f;
        return new TopicModel(conf, eta, alpha, dictionary, 1, modelWeight, listModelPath(path, conf));
    }

    @SneakyThrows({ IOException.class })
    private static Path[] listModelPath(Path path, Configuration conf) {
        if (FileSystem.get(conf).isFile(path)) {
            return new Path[] { path };
        } else {
            val statuses = FileSystem.get(conf).listStatus(path, PathFilters.partFilter());
            val modelPaths = new Path[statuses.length];
            for (int i = 0; i < statuses.length; i++) {
                modelPaths[i] = new Path(statuses[i].getPath().toUri().toString());
            }
            return modelPaths;
        }
    }

    @SneakyThrows({ IOException.class })
    private static String[] readDictionary(Path path, Configuration conf) {
        val term = new Text();
        val id = new IntWritable();
        val reader = new SequenceFile.Reader(FileSystem.get(conf), path, conf);
        val termIds = ImmutableList.<Pair<String, Integer>> builder();
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
}