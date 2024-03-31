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
		
		for (Outlet outlet : outlets) {
			outlet.data = null;
		}
	}
	
	
	public boolean execute() {
		runFlag = true;
		return true;
	}
	
	
	public String[] getEditable() {
		return new String[0];
	}
	
	
	public void editValue(int i) {
		
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
	

	@SuppressWarnings("rawtypes")
	public abstract class IONode {
		public int id;
		
		public String name;
		public Class dataType;
		
		public Laxel target;
		public int connectionId;
		
		public void setTarget(Laxel laxel) {
			target = laxel;
			connectionId = 0;
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public class Inlet extends IONode {
		
		public Inlet(String name, Class dataType, int id) {
			this.id = id;
			this.name = name;
			this.dataType = dataType;
			target = null;
			connectionId = -1;
		}
		
		public Object getData() {
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
	}
	
	
	@SuppressWarnings("rawtypes")
	public class Outlet extends IONode {

		public Object data = null;
		
		public Outlet(String name, Class dataType, int id) {
			this.id = id;
			this.name  = name;
			this.dataType = dataType;
			target = null;
			connectionId = -1;
		}
	}
}
