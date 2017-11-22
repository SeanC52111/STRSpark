import org.apache.spark.api.java.function.Function;
import quadIndex.*;
import STRTree.STRTree;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import scala.Tuple2;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.function.FlatMapFunction;
public class RangeMapFunction implements FlatMapFunction<STRTree,Tuple2<List,String>>{
	public ArrayList<Integer> candlist;
	public Circle range;
	public RangeMapFunction(ArrayList<Integer> candlist,Circle range) {
		this.candlist = candlist;
		this.range = range;
	}
	@Override
	public Iterable<Tuple2<List,String>> call(STRTree strtree){
		int curid = TaskContext.getPartitionId();
		List result = new LinkedList();
		List VO = new LinkedList();
		List<Tuple2<List,String>> l = new LinkedList();
		if(candlist.contains(curid)) {
			strtree.secureRangeQuery(strtree.root, range, result, VO);
		}
		String vo = String.join("#", VO);
		Tuple2<List,String> t = new Tuple2(result,vo);
		l.add(t);
		return l;
	}
}
