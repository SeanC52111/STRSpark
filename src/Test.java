import java.util.Arrays;
import java.util.List;
import java.lang.Iterable;
import java.util.*;

import scala.Tuple2;

import org.apache.commons.lang.StringUtils;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import com.clearspring.analytics.util.Lists;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
class ConvertToNativeTypes implements PairFunction<Tuple2<Text,IntWritable>,String,Integer>{
	public Tuple2<String,Integer> call(Tuple2<Text,IntWritable> record){
		return new Tuple2(record._1.toString(),record._2.get());
	}
}
public class Test {
  public static void main(String[] args) throws Exception {
    String inputFile = args[0];
    String outputFile = args[1];
    // Create a Java Spark Context.
    SparkConf conf = new SparkConf().setAppName("wordCount");
    conf.setMaster("local[*]");
		JavaSparkContext sc = new JavaSparkContext(conf);
    // Load our input data.
		/*
    JavaRDD<String> input = sc.textFile(inputFile);
    // Split up into words.
    JavaRDD<String> words = input.flatMap(
      new FlatMapFunction<String, String>() {
        public Iterable<String> call(String x) {
          return Arrays.asList(x.split(" "));
        }});
    // Transform into word and count.
    JavaPairRDD<String, Integer> counts = words.mapToPair(
      new PairFunction<String, String, Integer>(){
        public Tuple2<String, Integer> call(String x){
          return new Tuple2(x, 1);
        }}).reduceByKey(new Function2<Integer, Integer, Integer>(){
            public Integer call(Integer x, Integer y){ return x + y;}});
    // Save the word count back out to a text file, causing evaluation.
    counts.saveAsTextFile(outputFile);
    */
		ArrayList<String> al = new ArrayList<String>();
		al.add("a");
		al.add("b");
		JavaRDD<String> rdd = sc.parallelize(al);
		JavaPairRDD<Text,IntWritable> myPairRDD = rdd.mapToPair(new PairFunction<String,Text,IntWritable>(){
			@Override
			public Tuple2<Text,IntWritable> call(String input)throws Exception{
				Text t = new Text(input);
				IntWritable i = new IntWritable();
				i.set(input.length());
				return new Tuple2<Text,IntWritable>(t,i);
			}
		});

		myPairRDD.saveAsHadoopFile("testfile", Text.class, IntWritable.class, SequenceFileOutputFormat.class);
		JavaPairRDD<Text,IntWritable> readin = sc.hadoopFile("testfile", SequenceFileInputFormat.class, Text.class, IntWritable.class);
		JavaPairRDD<String,Integer> result = readin.mapToPair(new ConvertToNativeTypes());
		List<Tuple2<String,Integer>> resultlist = result.collect();
		for(Tuple2<String,Integer> record:resultlist) {
			System.out.println(record);
		}
	}
}