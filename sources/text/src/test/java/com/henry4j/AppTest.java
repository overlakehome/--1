package com.henry4j;

import org.apache.mahout.clustering.lda.cvb.TopicModel;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SparseRowMatrix;
import org.apache.mahout.math.Vector;
import org.junit.Test;

public class AppTest {
    @Test
    public void test() {
        int numTopics = 20;
        int numTerms = 41807;
        double eta = 0.0001;
        double alpha = 0.0001;
        String[] dictionary;
        TopicModel model = new TopicModel(numTopics, numTerms, Float.NaN, Float.NaN, dictionary, modelWeight)
        Vector doc = null;
        Vector docTopics = new DenseVector(numTopics).assign(1.0/numTopics);
        Matrix docModel = new SparseRowMatrix(numTopics, doc.get().size());
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