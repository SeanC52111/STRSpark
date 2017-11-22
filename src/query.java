import java.util.Arrays;
import java.util.List;
import java.lang.Iterable;

import scala.Tuple2;
import scala.xml.Null;
import STRTree.*;
import org.apache.spark.TaskContext;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.NullWritable;
import org.apache.spark.Partition;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import java.util.*;
import quadIndex.*;

public class query {
  public static void main(String[] args) throws Exception {
    SparkConf conf = new SparkConf().setAppName("STR");
    conf.setMaster("local[*]");
    String outputTree = "hdfs://bigdata.comp.hkbu.edu.hk:8020/home/comp/cezhang/nytreespark";
	JavaSparkContext sc = new JavaSparkContext(conf);
    JavaPairRDD<Null,STRTree> tree = sc.sequenceFile(outputTree, Null.class, STRTree.class);
    tree.cache();
    tree.first()._2.root.MBR.toString();
	/*
	List<Partition> plist = treeindex.partitions();
	Point point = new Point(40.5793904,-73.8431041);
	//start = System.currentTimeMillis();
	List result = new LinkedList();
	List VO = new LinkedList();
	start = System.currentTimeMillis();
	treeindex.first().secureKNN(32, point, result, VO);
	end = System.currentTimeMillis();
	//end = System.currentTimeMillis();
	System.out.println(end-start);
	for(Object t:result) {
		System.out.println(((Rect)t).toString());
	}
	*/
	}
}

