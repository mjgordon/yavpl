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
	public boolean loadFlag = false;
	
	public void clear() {
		runFlag = false;
		saveFlag = false;
		loadFlag = false;
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
				JSONObject inletJSON = new JSONObject();
				inletJSON.setString("targetId", inlet.target.uuid.toString());
				inletJSON.setInt("connectionId", inlet.connectionId);
				inletArray.append(inletJSON);
			}
			else {
				inletArray.append(new JSONObject());
			}
		}
		self.setJSONArray("inlets", inletArray);
		
		JSONArray outletArray = new JSONArray();
		for (Outlet outlet : outlets) {
			if (outlet.target != null) {
				JSONObject outletJSON = new JSONObject();
				outletJSON.setString("targetId", outlet.target.uuid.toString());
				outletJSON.setInt("connectionId", outlet.connectionId);
				outletArray.append(outletJSON);
			}
			else {
				outletArray.append(new JSONObject());
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
		public int id;
		
		public String name;
		public Class dataType;
		
		public Laxel target;
		public int connectionId;
		
		public Inlet(String name, Class dataType, int id) {
			target = null;
			connectionId = -1;
			this.dataType = dataType;
			this.name = name;
			this.id = id;
		}
		
		public Object get() {
			if (target != null) {
				if (this.dataType.isAssignableFrom(target.outlets[connectionId].dataType)) {
					return target.outlets[connectionId].data;	
				}
				else {
					return new Error(ErrorType.WRONG_TYPE);
				}
			}
			return null;
		}
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connectionId = 0;
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public class Outlet extends IONode {
		public int id;
		
		public String name;
		public Class dataType;
		
		public Laxel target;
		public int connectionId;
		
		public Object data = null;
		
		public Outlet(String name, Class dataType, int id) {
			this.dataType = dataType;
			this.name  = name;
			target = null;
			connectionId = -1;
			this.id = id;
		}
		
		public void set(Object data) {
			this.data = data;
		}
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connectionId = 0;
		}
		
	}
}
