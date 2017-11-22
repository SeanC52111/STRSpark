package STRTree;
import quadIndex.*;

public class GlobalRecord {
	public String id = "";
	public Rect mbr = null;
	public String hash = "";
	public GlobalRecord(String id,Rect mbr,String hash) {
		this.id = id;
		this.mbr = mbr;
		this.hash = hash;
	}
	
	@Override
	public String toString() {
		return id+" "+mbr.toString()+" "+hash;
	}
}
