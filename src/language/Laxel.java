package language;

public abstract class Laxel {	
	public Inlet[] inlets;
	public Outlet[] outlets;
	public String displayName = "";
	public String tag;
	
	public boolean runFlag = false;
	
	public void clear() {
		runFlag = false;
	}
	
	public boolean execute() {
		runFlag = true;
		return true;
	}
	
	
	public enum Direction {
		NONE,
		INLET,
		OUTLET,
		BOTH
	}
	

	public abstract class IONode {
		public abstract void setTarget(Laxel laxel);
	}
	
	@SuppressWarnings("rawtypes")
	public class Inlet extends IONode {
		public Laxel target;
		public Outlet connection;
		public String name;
		
		public Class dataType;
		
		public Inlet(String name, Class dataType) {
			target = null;
			connection = null;
			this.dataType = dataType;
			this.name = name;
		}
		
		public Object get() {
			if (target != null && connection != null) {
				if (this.dataType.isAssignableFrom(connection.dataType)) {
					return connection.data;	
				}
				else {
					return new Error(ErrorType.WRONG_TYPE);
				}
			}
			return null;
		}
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connection = laxel.outlets[0];
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public class Outlet extends IONode {
		public Laxel target;
		public Inlet connection;
		public String name;
		
		public Class dataType;
		
		public Object data = null;
		
		public Outlet(String name, Class dataType) {
			this.dataType = dataType;
			this.name  = name;
			target = null;
			connection = null;
		}
		
		public void set(Object data) {
			this.data = data;
		}
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connection = laxel.inlets[0];
		}
		
	}
}
