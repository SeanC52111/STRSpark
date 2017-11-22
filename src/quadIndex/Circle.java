package quadIndex;

public class Circle implements java.io.Serializable{
	public Point center;
	public double radius;
	public Circle(Point p,double r) {
		center = p;
		radius = r;
	}
	private double getMinimumDist(Point q,Rect r) {
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
	@Override
	public String toString() {
		String str = ""+center.x+" "+center.y+" "+radius;
		return str;
	}
	
	public boolean isIntersects(Rect rect) {
		double mindist = getMinimumDist(center,rect);
		return mindist<=radius;
	}
	
	public static void main(String[] args) {
		Circle c = new Circle(new Point(3,1),1);
		Rect rect = new Rect(3,3,1,1);
		System.out.println(c.isIntersects(rect));
	}
}
