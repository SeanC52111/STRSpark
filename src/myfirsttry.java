import java.lang.Iterable;

import scala.Tuple2;
import STRTree.*;
import org.apache.spark.TaskContext;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.spark.Partition;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import java.util.*;
import quadIndex.*;
import java.io.*;
import Tool.*;

public class myfirsttry {
  public static void main(String[] args) throws Exception {
    SparkConf conf = new SparkConf().setAppName("STR");
    conf.setMaster("local[*]");
	JavaSparkContext sc = new JavaSparkContext(conf);
    // Load our input data.
	String inputFile = "hdfs://bigdata.comp.hkbu.edu.hk:8020/home/comp/cezhang/nymapsplit";
	//String outputTree = "hdfs://bigdata.comp.hkbu.edu.hk:8020/home/comp/cezhang/testoutput";
	JavaRDD<String> input = sc.textFile(inputFile);
	//System.out.println(input.getNumPartitions());
	
	JavaRDD<String> mapinput = input.map(new Function<String,String>(){
		public String call(String s) {
			String []split = s.split(" ");
			return split[1]+" "+split[2];
		}
	});
	JavaRDD<STRTree> treeindex = mapinput.mapPartitions(new FlatMapFunction<Iterator<String>,STRTree>(){
		ArrayList<Rect> rects = new ArrayList<Rect>();
		@Override
		public Iterable<STRTree> call(Iterator<String> s)throws Exception{
			while(s.hasNext()) {
				String[] split = s.next().split(" ");
				double x = Double.valueOf(split[0]);
				double y = Double.valueOf(split[1]);
				Rect r = new Rect(x,x,y,y);
				rects.add(r);
			}
			ArrayList<STRTree> treelist = new ArrayList<STRTree>();
			treelist.add(new STRTree(rects,100));
			return treelist;
		}
	}, true);
	treeindex.cache();
	
	long start = System.currentTimeMillis();
	JavaRDD<String> globalindex = treeindex.map(new Function<STRTree,String>(){
		@Override
		public String call(STRTree tree) {
			int id = TaskContext.getPartitionId();
			String s = "";
			STRNode r = tree.root;
			s += id+"#"+r.MBR.toString()+"#"+r.hashvalue;
			return s;
		}
	});
	globalindex.cache();
	List<String> globallist = globalindex.collect();
	//global index
	ArrayList<GlobalRecord> globalrecord = computeGlobal(globallist);
	ArrayList<String> mbrhash = new ArrayList<String>();
	for(GlobalRecord g:globalrecord) {
		mbrhash.add(g.mbr.toString()+g.hash);
	}
	Sig sig = new Sig("rsasig","secretkey");
	sig.condenseRSA(mbrhash);
	
	long end = System.currentTimeMillis();
	System.out.println(end-start);
	//System.out.println(globalrecord.get(3).mbr.toString());
	Point point = new Point(40.6850766,-74.5009991);
	//Point point = new Point(40.6698351,-73.8225834);
	//Point point = new Point(62,81);
	int k = 1024;
	start = System.currentTimeMillis();
	String sid = findHomeIndex(globalrecord,point);
	
	dknn(k,sid,point,globalrecord,treeindex);
	end = System.currentTimeMillis();
	System.out.println("total time: "+(end-start));
	
  }
  
  
  public static void dknn(int k,String sid,Point q,ArrayList<GlobalRecord> gr,JavaRDD<STRTree> treeindex) {
	  Tuple2<List,String> result = localknn(k,sid,q,gr,treeindex);
	  List hRS = result._1;
	  String hVO = result._2;
	  Object kthobj = hRS.get(k-1);
	  Rect kth = (Rect)kthobj;
	  Circle rcircle = new Circle(q,q.getdistance(new Point(kth.x2,kth.y1)));
	  ArrayList<Rect> RS = new ArrayList<Rect>(hRS);
	  ArrayList<String> VO = new ArrayList<String>();
	  VO.add(hVO);
	  ArrayList<Integer> candidate = new ArrayList<Integer>();
		for(GlobalRecord g:gr) {
			if(g.mbr.isIntersects(rcircle) && !g.id.equals(sid)) {
				//System.out.println(gr.id);
				candidate.add(Integer.valueOf(g.id));
			}
		}
		//System.out.println("candidate size:"+candidate.size());
		if(candidate.size()!=0) {
			JavaRDD<Tuple2<List,String>> rangerdd = treeindex.flatMap(new RangeMapFunction(candidate,rcircle));
			List<Tuple2<List,String>> rangeres = rangerdd.collect();
			
			
			for(Tuple2<List,String> tup:rangeres) {
				if(!tup._2.equals("")) {
					for(Object o:tup._1) {
						Rect r = (Rect)o;
						RS.add(r);
					}
					VO.add(tup._2);
				}
			}
			
			Comparator<Rect> distcomp = new Comparator<Rect>() {
				public int compare(Rect r1,Rect r2) {
					Point p1 = new Point(r1.x1,r1.y1);
					Point p2 = new Point(r2.x1,r2.y1);
					double dis1 = p1.getdistance(q);
					double dis2 = p2.getdistance(q);
					if(dis1 < dis2)
						return -1;
					else if(dis1 > dis2)
						return 1;
					else
						return 0;
				}
			};
			RS.sort(distcomp);
		}
		
		ArrayList<Rect> kRS = new ArrayList<Rect>();
		for(int i=0;i<k;i++) {
			kRS.add(RS.get(i));
			//System.out.println(RS.get(i));
		}
		ArrayList<GlobalRecord> nprocess = new ArrayList<GlobalRecord>();
		for(GlobalRecord cur:gr) {
			if(!cur.id.equals(sid) && !candidate.contains(Integer.valueOf(cur.id))) {
				//System.out.println(cur.id);
				nprocess.add(cur);
			}
		}
		for(GlobalRecord np:nprocess) {
			VO.add(np.mbr.toString()+np.hash);
		}
		
		String time = ""+System.currentTimeMillis();
		try {
			String resultfile = "knnresult"+time+".txt";
			String vofile = "knnvo"+time+".txt";
			FileWriter fw = new FileWriter(resultfile,false);
			
			for(Rect r:kRS) {
				fw.write(r.toString()+"\n");
			}
			fw.close();
			fw = new FileWriter(vofile,false);
			for(String s:VO) {
				fw.write(s+"\n");
			}
			fw.close();
		}catch(Exception e) {e.printStackTrace();}
  }
  public static Tuple2<List,String> localknn(int k,String sid,Point q,ArrayList<GlobalRecord> gr,JavaRDD<STRTree> treeindex) {
	  int id = Integer.valueOf(sid);
	  JavaRDD<Tuple2<List,String>> resrdd = treeindex.map(new localkNNMapFunction(id,k,q));
	  resrdd.cache();
	  List<Tuple2<List,String>> res = resrdd.collect();
	  return res.get(id);
  }
  public static ArrayList<GlobalRecord> computeGlobal(List<String> globallist){
	  ArrayList<GlobalRecord> globalrecord = new ArrayList<GlobalRecord>();
	  for(String s:globallist) {
			String[] split = s.split("#");
			String id = split[0];
			String rect = split[1];
			String hash = split[2];
			String[] splitrect = rect.split(" ");
			double x1 = Double.valueOf(splitrect[0]);
			double x2 = Double.valueOf(splitrect[1]);
			double y1 = Double.valueOf(splitrect[2]);
			double y2 = Double.valueOf(splitrect[3]);
			Rect mbr = new Rect(x1,x2,y1,y2);
			globalrecord.add(new GlobalRecord(id,mbr,hash));
		}
	  return globalrecord;
  }
  public static String findHomeIndex(ArrayList<GlobalRecord> gr,Point q) {
	  String id = "";
	  double min = Double.MAX_VALUE;
	  for(GlobalRecord r:gr) {
		  double dis = getMinimumDist(q,r.mbr);
		  if(dis < min) {
			  id = r.id;
			  min = dis;
		  }
	  }
	  return id;
  }
  private static double getMinimumDist(Point q,Rect r) {
		double ret = 0.0;
		if(q.x < r.x1)
			ret += Math.pow(r.x1-q.x, 2);
		else if(q.x > r.x2)
			ret += Math.pow(q.x-r.x2, 2);
		if(q.y < r.y1)
			ret += Math.pow(r.y1-q.y, 2);
		else if(q.y > r.y2)
			ret += Math.pow(q.y-r.y2, 2);
		return Math.sqrt(ret);
	}
}