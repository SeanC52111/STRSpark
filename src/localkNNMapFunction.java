import org.apache.spark.api.java.function.Function;
import quadIndex.*;
import STRTree.STRTree;

import java.util.LinkedList;
import java.util.List;
import scala.Tuple2;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.function.FlatMapFunction;
public class localkNNMapFunction implements Function<STRTree,Tuple2<List,String>>{
	public int id=0;
	public Point point;
	public int k = 32;
	public localkNNMapFunction(int id,int k,Point q) {
		this.id = id;
		this.point = q;
		this.k = k;
	}
	@Override
	public Tuple2<List,String> call(STRTree strtree){
		int curid = TaskContext.getPartitionId();
		//System.out.println("hid: "+id+"curid: "+curid);
		LinkedList result = new LinkedList();
		LinkedList VO = new LinkedList();
		//long start = System.currentTimeMillis();
		if(curid == id) {
			strtree.nkNN(k, point, result, VO);
		}
		//long end = System.currentTimeMillis();
		//System.out.println(end-start);
		String s = String.join("#", VO);
		Tuple2<List,String> t = new Tuple2(result,s);
		return t;
	}
	
}
