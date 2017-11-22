package STRTree;
import java.util.*;
import java.io.*;
import quadIndex.*;
public class test {
	public static void main(String[] args) {
		
		ArrayList<Rect> rlist=null;
		STRNode root;

		try {
			rlist = new ArrayList<Rect>();
			FileInputStream fis = new FileInputStream("nymap");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			String []arrs = null;
			while((line=br.readLine())!=null) {
				arrs=line.split(" ");
				Rect r = new Rect(new Point(Double.valueOf(arrs[1]),Double.valueOf(arrs[2])));
				rlist.add(r);
			}
			br.close();
		}catch(Exception e) {}
		STRTree strtree = new STRTree(rlist,100);
		LinkedList<Rect> result = new LinkedList<Rect>();
		LinkedList<String> VO = new LinkedList<String>();
		Point p = new Point(40.345,-74.091555);
		
		long start = System.currentTimeMillis();
		strtree.nkNN(1024, p, result, VO);
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		start = System.currentTimeMillis();
		strtree.nkNN(512, p, result, VO);
		end = System.currentTimeMillis();
		System.out.println(end-start);
		start = System.currentTimeMillis();
		strtree.nkNN(256, p, result, VO);
		end = System.currentTimeMillis();
		System.out.println(end-start);
		start = System.currentTimeMillis();
		strtree.nkNN(128, p, result, VO);
		end = System.currentTimeMillis();
		System.out.println(end-start);
		start = System.currentTimeMillis();
		strtree.nkNN(64, p, result, VO);
		end = System.currentTimeMillis();
		System.out.println(end-start);
		/*
		Comparator<String> distcomp = new Comparator<String>() {
			public int compare(String r1,String r2) {
				String rr1 = r1.split("\t")[1];
				String rr2 = r2.split("\t")[1];
				Double x1 = Double.valueOf(rr1.split(" ")[1]);
				Double y1 = Double.valueOf(rr1.split(" ")[2]);
				Double x2 = Double.valueOf(rr2.split(" ")[1]);
				Double y2 = Double.valueOf(rr2.split(" ")[2]);
				
				Point p1 = new Point(x1,y1);
				Point p2 = new Point(x2,y2);
				Point q = new Point(0,0);
				double dis1 = p1.getdistance(q);
				double dis2 = p2.getdistance(q);
				
				if(dis1<dis2)
					return -11;
				else if(dis1>dis2)
					return 1;
				else
					return 0;
			}
		};
		//priority queue to sort the result by distance to q;
		PriorityQueue<String> pq = new PriorityQueue<String>(4,distcomp);
		pq.offer("1\t1 1 0 0");
		pq.offer("1\t2 2 0 0");
		pq.offer("1\t1 1 1 1");
		pq.offer("1\t4 4 0 0");
		for(int i=0;i<2;i++)
			System.out.println(pq.poll());
			*/
	}
}
