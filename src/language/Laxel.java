package language;

import java.util.ArrayList;

public abstract class Laxel {
	
	public abstract void execute();
	
	public Inlet[] inlets;
	public Outlet[] outlets;
	public String displayName = "";
	
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
				if (connection.dataType == this.dataType) {
					return connection.data;	
				}
			}
			return null;
		}
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connection = laxel.outlets[0];
		}
	}
	
	
	public enum Direction {
		NONE,
		INLET,
		OUTLET,
		BOTH
	}
	
	
	@SuppressWarnings("rawtypes")
	public class Outlet extends IONode {
		public Laxel target;
		public Inlet connection;
		public String name;
		
		public Class dataType;
		
		public Object data;
		
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
