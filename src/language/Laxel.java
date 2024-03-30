package language;

import java.util.UUID;

import processing.data.JSONArray;
import processing.data.JSONObject;

public abstract class Laxel {	
	public Inlet[] inlets;
	public Outlet[] outlets;
	public String displayName = "";
	public String tag;
	
	public UUID uuid = UUID.randomUUID();
	
	public boolean runFlag = false;
	public boolean saveFlag = false;
	
	public void clear() {
		runFlag = false;
		saveFlag = false;
	}
	
	public boolean execute() {
		runFlag = true;
		return true;
	}
	
	public JSONArray toJSON(JSONArray accumulator) {
		
		for (Laxel.Inlet inlet : inlets) {
			if (inlet.target != null && inlet.target.saveFlag == false) {
				accumulator = inlet.target.toJSON(accumulator);
			}
		}
		
		JSONObject self = new JSONObject();
		self.setString("id", uuid.toString());
		self.setString("type", this.getClass().getCanonicalName());
		
		JSONArray inletArray = new JSONArray();
		for (Inlet inlet : inlets) {
			if (inlet.target != null) {
				inletArray.append(inlet.target.uuid.toString());
			}
			else {
				inletArray.append("");
			}
		}
		self.setJSONArray("inlets", inletArray);
		
		JSONArray outletArray = new JSONArray();
		for (Outlet outlet : outlets) {
			if (outlet.target != null) {
				outletArray.append(outlet.target.uuid.toString());
			}
			else {
				outletArray.append("");
			}
		}
		self.setJSONArray("outlets", outletArray);
		
		accumulator.append(self);
		
		saveFlag = true;
		
		return accumulator;
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
	
	@SuppressWarnings({"rawtypes", "unchecked"})
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
