package ui;

import language.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import vm.VM;

import static ui.Bridge.p;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Editor {
	
	PGraphics canvas;
	PGraphics canvasOverview;
	PGraphics canvasWorking;
	
	
	int defaultWidth = 200;
	int defaultHeight = 20;
	int gutter = 10;
	int ioOffset = 50;
	
	public Laxel point;
	public Laxel head;
	
	public HashMap<String, Laxel> functions;
	
	private EditorDialog<?> dialogs = null;
	
	private int textHeight = 20;
	
	private String[] reservedNames = {
			"int",
			"format",
			"noop",
			"print",
	};
	
	public Editor(PGraphics canvas, PGraphics canvasOverview) {
		this.canvas = canvas;
		
		this.canvasOverview = canvasOverview;
		
		canvasWorking = p.createGraphics(canvas.width, canvas.height);
		
		this.functions = new HashMap<String, Laxel>();
	}
	
	public void draw() {
		canvas.beginDraw();
		canvas.background(255);
		
		if (dialogs != null) {
			canvasWorking.beginDraw();
			canvas.image(dialogs.draw(), DrawableDialog.gutter, DrawableDialog.gutter);
			canvasWorking.endDraw();
		}
		
		canvas.stroke(0);
		canvas.noFill();
		canvas.strokeWeight(1);
		canvas.rect(0,0,canvas.width - 1,canvas.height - 1);
		
		LaxelDisplay laxelTree = getTree(point);
		canvas.translate(canvas.width / 2 - laxelTree.width / 2,canvas.height / 2 - laxelTree.height / 2);
		renderLaxel(laxelTree);
		
		canvas.endDraw();
		
		canvasOverview.beginDraw();
		canvasOverview.background(255);
		
		canvasOverview.stroke(0);
		canvasOverview.noFill();
		canvasOverview.strokeWeight(1);
		canvasOverview.rect(0,0,canvasOverview.width - 1,canvasOverview.height - 1);
		
		canvasOverview.fill(0);
		canvasOverview.noStroke();
		canvasOverview.textAlign(PApplet.LEFT, PApplet.TOP);
		canvasOverview.text("Main", gutter,gutter);
		String[] functionNames = functions.keySet().toArray(new String[functions.keySet().size()]);
		for (int i = 0; i < functionNames.length; i++) {
			canvasOverview.text(functionNames[i], gutter, (i + 1) * textHeight + gutter);
		}
		
		canvasOverview.endDraw();
	}
	
	
	public void save() {
		JSONObject output = new JSONObject();
		output.setString("name", "unnamed");
		
		
		VM.clearLaxel(head);
		JSONArray laxelArray = head.toJSON(new JSONArray());
		
		output.setJSONArray("laxels", laxelArray);
		
		output.setString("head",head.uuid.toString());
		
		p.saveJSONObject(output, "output/output.json");
	}
	
	
	/**
	 * Load a script from file into the editor head
	 */
	public void load() {
		JSONObject json = p.loadJSONObject("output/output.json");
		String name = json.getString("name");
		JSONArray jsonLaxels = json.getJSONArray("laxels");
		UUID headUUID = UUID.fromString(json.getString("head"));
		
		HashMap<UUID, Laxel> laxels = new HashMap<UUID, Laxel>();
		HashMap<UUID, JSONObject> jsonMap = new HashMap<UUID, JSONObject>();
		
		for (int i = 0; i < jsonLaxels.size(); i++) {
			JSONObject jsonLaxel = jsonLaxels.getJSONObject(i);
			Laxel laxel = Laxel.fromJSON(jsonLaxel);
			laxels.put(laxel.uuid, laxel);
			jsonMap.put(laxel.uuid, jsonLaxel);
		}
		
		head = laxels.get(headUUID);
		point = head;
		
		loadConnections(head, jsonMap,laxels);
	}
	
	
	/**
	 * After all Laxels objects have been created during loading, loadConnections is called recursively from head to connect all inlets and outlets
	 * @param laxel
	 * @param jsonMap
	 * @param laxels
	 */
	public void loadConnections(Laxel laxel,HashMap<UUID, JSONObject> jsonMap, HashMap<UUID, Laxel> laxels) {
		if (laxel.loadFlag) {
			return;
		}
		
		JSONObject laxelJSON = jsonMap.get(laxel.uuid);
		
		JSONArray inletArray = laxelJSON.getJSONArray("inlets");
		for(int i = 0; i < inletArray.size(); i++) {
			JSONObject inletJSON = inletArray.getJSONObject(i);
			Laxel target = laxels.get(UUID.fromString(inletJSON.getString("targetId")));
			int connectionId = inletJSON.getInt("connectionId");
			laxel.inlets[i].target = target;
			laxel.inlets[i].connectionId = connectionId;
			
			loadConnections(target, jsonMap, laxels);
		}
		
		JSONArray outletArray = laxelJSON.getJSONArray("outlets");
		for (int i = 0; i < outletArray.size(); i++) {
			JSONObject outletJSON = outletArray.getJSONObject(i);
			UUID targetId = UUID.fromString(outletJSON.getString("targetId"));
			int connectionId = outletJSON.getInt("connectionId");
			laxel.outlets[i].target = laxels.get(targetId);
			laxel.outlets[i].connectionId = connectionId;
		}
		
		laxel.loadFlag = true;
	}
	
	
	public void edit(Laxel laxel) {
		if (laxel instanceof LaxelLiteralInt) {
			dialogs = new DialogEditInt(laxel);
		}
	}
	
	
	public LaxelDisplay getTree(Laxel laxel) {
		return getTree(laxel, Laxel.Direction.BOTH, 0);
	}
	
	
	/**
	 * Setup function for rendering. Given the laxel point, returns a tree of LaxelDisplay objects with sizing information
	 * @param laxel
	 * @param direction
	 * @param depth
	 * @return
	 */
	public LaxelDisplay getTree(Laxel laxel, Laxel.Direction direction, int depth) {
		int maxDepth = 3;
		
		LaxelDisplay ld = new LaxelDisplay(laxel);
		
		int greatestInletWidth = 0;
		int greatestOutletWidth = 0;
		
		for (Laxel.Inlet inlet : laxel.inlets) {
			if (laxel.foldState == Laxel.FoldState.OPEN) {
				ld.inletTextWidth = Math.max(ld.inletTextWidth, Math.max(p.textWidth(inlet.name),p.textWidth(inlet.dataType.getSimpleName())));	
			}
			else if (laxel.foldState == Laxel.FoldState.MID) {
				ld.inletTextWidth = Math.max(ld.inletTextWidth, p.textWidth(inlet.name));	
			}
			
			if (direction == Laxel.Direction.INLET || direction == Laxel.Direction.BOTH) {
				if (inlet.target == null || depth >= maxDepth) {
					ld.inlets.add(null);
				}
				else {
					LaxelDisplay branch = getTree(inlet.target, Laxel.Direction.INLET, depth + 1);
					ld.inlets.add(branch);
					greatestInletWidth = Math.max(greatestInletWidth, branch.width);		
				}
			}
		}	
		
		for (Laxel.Outlet outlet : laxel.outlets) {
			if (laxel.foldState == Laxel.FoldState.OPEN) {
				ld.outletTextWidth = Math.max(ld.outletTextWidth, Math.max(p.textWidth(outlet.name),p.textWidth(outlet.dataType.getSimpleName())));
			}
			else if (laxel.foldState == Laxel.FoldState.MID) {
				ld.outletTextWidth = Math.max(ld.outletTextWidth, p.textWidth(outlet.name));
			}
			
			if (direction == Laxel.Direction.OUTLET || direction == Laxel.Direction.BOTH) {
				if (outlet.target == null || depth >= maxDepth) {
					ld.outlets.add(null);
				}
				else {
					LaxelDisplay branch = getTree(outlet.target, Laxel.Direction.OUTLET, depth + 1);
					ld.outlets.add(branch);
					greatestOutletWidth = Math.max(greatestOutletWidth, branch.width);
				}
			}
		}
		
		int maxIOCount = Math.max(laxel.inlets.length, laxel.outlets.length);
		ld.height = Math.max(defaultHeight, (ioOffset * maxIOCount) + (gutter * 2));
		if (direction == Laxel.Direction.INLET || direction == Laxel.Direction.BOTH) {
			for (LaxelDisplay l : ld.inlets) {
				if (l != null) {
					ld.height = Math.max(ld.height, l.height);
					l.width = greatestInletWidth;
				}
			}	
		}
		
		if (direction == Laxel.Direction.OUTLET || direction == Laxel.Direction.BOTH) {
			for (LaxelDisplay l : ld.outlets) {
				if (l != null) {
					ld.height = Math.max(ld.height, l.height);
					l.width = greatestOutletWidth;
				}
			}
		}
		
		
		if (laxel.foldState == Laxel.FoldState.OPEN ) {
			ld.width = (int)(ld.inletTextWidth + ld.outletTextWidth + p.textWidth(laxel.displayName) + (gutter * 4));	
		}
		
		else if (laxel.foldState == Laxel.FoldState.MID) {
			ld.width = (int)(ld.inletTextWidth + ld.outletTextWidth + textHeight + (gutter * 4));
		}
		
		else if (laxel.foldState == Laxel.FoldState.FOLDED) {
			ld.width = (int)(textHeight + (gutter * 2));
		}
		
		
		
		return ld;
	}
	
	
	public void renderLaxel(LaxelDisplay ld) {
		renderLaxel(ld, Laxel.Direction.BOTH);
	}
	
	
	/**
	 * Renders a single laxel box, and depending on direction, all of its inlets or outlets recursively
	 * @param ld
	 * @param direction
	 */
	public void renderLaxel(LaxelDisplay ld, Laxel.Direction direction) {
		Laxel laxel = ld.laxel;
		
		canvas.strokeWeight(laxel == point ? 2 : 1);
		
		canvas.stroke(0);
		canvas.noFill();
		canvas.rect(0,0, ld.width, ld.height);
		canvas.noStroke();
		canvas.fill(0);
		
		canvas.textAlign(PApplet.LEFT, PApplet.CENTER);
		if (ld.laxel.foldState == Laxel.FoldState.OPEN) {
			canvas.text(laxel.displayName, gutter + ld.inletTextWidth + gutter, ld.height / 2);	
		}
		else if (ld.laxel.foldState == Laxel.FoldState.MID) {
			canvas.pushMatrix();
			canvas.rotate(-PApplet.PI/2);
			canvas.text(laxel.displayName,-ld.height / 2 - p.textWidth(laxel.displayName) / 2, gutter + ld.inletTextWidth + gutter + (textHeight / 2));
			canvas.popMatrix();
		}
		else if (ld.laxel.foldState == Laxel.FoldState.FOLDED) {
			canvas.pushMatrix();
			canvas.rotate(-PApplet.PI/2);
			canvas.text(laxel.displayName, -ld.height / 2 - p.textWidth(laxel.displayName) / 2, gutter + (textHeight / 2));
			canvas.popMatrix();
		}
		
		int iy = 0;
		for (int i = 0; i < laxel.inlets.length; i++) {
			Laxel.Inlet inlet = laxel.inlets[i];
			int y = (i * ioOffset) + (ioOffset / 2) + gutter;
			canvas.fill(255);
			canvas.stroke(0);
			canvas.ellipse(0, y, 10, 10);
			
			canvas.fill(0);
			canvas.noStroke();
			canvas.textAlign(PApplet.LEFT, PApplet.CENTER);
			
			if (laxel.foldState == Laxel.FoldState.OPEN) {
				canvas.text(inlet.dataType.getSimpleName(), gutter, y + 10);
				canvas.text(inlet.name, gutter, y - 10);
			}
			else if (laxel.foldState == Laxel.FoldState.MID) {
				canvas.text(inlet.name, gutter, y);
			}
			
			if (direction == Laxel.Direction.INLET || direction == Laxel.Direction.BOTH) {	
				LaxelDisplay ldInlet = ld.inlets.get(i);
				if (ldInlet != null) {
					canvas.pushMatrix();
					canvas.translate(-ldInlet.width, iy);
					iy += ldInlet.height;
					renderLaxel(ldInlet, Laxel.Direction.INLET);
					canvas.popMatrix();
				}	
			}
		}
		
		int oy = 0;
		for (int i = 0; i < laxel.outlets.length; i++) {
			Laxel.Outlet outlet = laxel.outlets[i];
			int y = (i * ioOffset) + (ioOffset / 2) + gutter;
			canvas.fill(255);
			canvas.stroke(0);
			canvas.ellipse(ld.width, y, 10, 10);
			
			canvas.fill(0);
			canvas.noStroke();
			canvas.textAlign(PApplet.RIGHT, PApplet.CENTER);
			
			if (laxel.foldState == Laxel.FoldState.OPEN) {
				canvas.text(outlet.dataType.getSimpleName(), ld.width - gutter, y + 10);
				canvas.text(outlet.name, ld.width - gutter, y - 10);
			}
			else if (laxel.foldState == Laxel.FoldState.MID) {
				canvas.text(outlet.name, ld.width - gutter, y);
			}
			
			
			if (direction == Laxel.Direction.OUTLET || direction == Laxel.Direction.BOTH) {	
				LaxelDisplay ldOutlet = ld.outlets.get(i);
				if (ldOutlet != null) {
					canvas.pushMatrix();
					canvas.translate(ld.width, oy);
					oy += ldOutlet.height;
					renderLaxel(ldOutlet, Laxel.Direction.OUTLET);
					canvas.popMatrix();
				}	
			}
		}
	}
	
	
	public void keyPressed(int key) {
		if (key == PApplet.ESC) {
			p.key = 0; // Cancels the built in quit action on ESC
			dialogs = null;
		}
		
		if (dialogs != null) {
			if (dialogs.keyPressed(key)) {
				dialogs = null;
			}
		}
		
		else if (key == 'a') {
			dialogs = new DialogMove(Laxel.Direction.INLET);
			
		}
		
		else if (key == 'd') {
			dialogs = new DialogMove(Laxel.Direction.OUTLET);
		}
		
		
		else if (key == 'e') {
			edit(point);
		}
		
		else if (key == 'f') {
			point.cycleFoldState();
		}
		

		else if (key == 'i') {
			dialogs = new DialogInsert(point);
		}
		
		else if (key == 'l') {
			load();
		}
		
		else if (key == 'n') {
			dialogs = new DialogCreateFunction();
		}
		
		else if (key == 'r') {
			dialogs = new DialogReplace(point);
		}
		
		else if (key == 's') {
			save();
		}
		
		else if (key == 'x') {
			VM.execute(head);
		}
		
		else if (key == '`') {
			dialogs = new DialogCommand();
		}
		
		if (dialogs != null && dialogs.complete) {
			dialogs = null;
		}
	}
	
	
	private class LaxelDisplay {
		public Laxel laxel;
		public int width = 0;
		public int height = 0;
		
		public float inletTextWidth;
		public float outletTextWidth;
		
		public ArrayList<LaxelDisplay> inlets = new ArrayList<LaxelDisplay>();
		public ArrayList<LaxelDisplay> outlets = new ArrayList<LaxelDisplay>();
		
		public LaxelDisplay(Laxel laxel) {
			this.laxel = laxel;
		}
	}
	
	
	/**
	 * Base UI unit. T indicates the expected return type from the dialogs action upon execution
	 *
	 * @param <T>
	 */	
	private abstract class DrawableDialog {
		protected DrawableDialog childDialog;
		
		protected String displayName = "";
		protected String subtitle = null;
		
		protected int width = 100;
		protected int height = 20;
		
		private static final int baseWidth = 100;
		private static final int baseHeight = 20;
		
		private final static int gutter = 3;
		
		public final PImage draw() {
			
			width = (int)Math.max(baseWidth, p.textWidth(displayName) + (gutter * 2));
			height = baseHeight;
			
			PImage childImage = null;
			
			if (childDialog != null) {
				childImage = childDialog.draw();
			}


			if (childImage != null) {
				 width += childImage.width + gutter;
				 height = Math.max(childImage.height + (gutter * 2), height);
			}
			
			canvasWorking.background(255);
				
			drawLocal();
	
			canvasWorking.noFill();
			canvasWorking.stroke(0);
			canvasWorking.rect(0,0,width - 1, height - 1);
			
			if (childImage != null) {
				canvasWorking.image(childImage, width - gutter - childImage.width, height / 2 - childImage.height / 2);	
			}
				
			PImage output = canvasWorking.get(0,0,width, height);
			
			return output;
		}
		
		
		/**
		 * By default, draws the dialog name and subtitle
		 * Override if additional functionality is needed
		 */
		protected void drawLocal() {
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			
			if (subtitle == null) {
				canvasWorking.text(displayName, gutter, height / 2);	
			}
			else {
				height += 16;
				canvasWorking.text(displayName, gutter, height / 2 - 10);	
				canvasWorking.text(subtitle, gutter, height / 2 + 10);	
			}
		}
	}
	
	
	private abstract class EditorDialog<T> extends DrawableDialog {	
		protected abstract T execute();
		
		public boolean complete = false;
		
		public boolean keyPressed(int key) {
			return false;
		}	
	}
	
	
	private class DialogCreateLaxel extends EditorDialog<Laxel> {		
		EditorDialog<String> produceString;

		public DialogCreateLaxel() {
			produceString = new DialogTextEntry();
			childDialog = produceString;
			displayName = "LAXEL";
		}

		@Override
		public Laxel execute() {
			String name = produceString.execute();
			
			Laxel output = null;
			
			switch(name) {
				case "print":
					output = new LaxelPrint();
				break;
				case "int":
					output = new LaxelLiteralInt(0);
				break;
				case "format":
					output = new LaxelFormat();
			}
			
			if (functions.keySet().contains(name)) {
				output = functions.get(name);
			}
			
			return output;
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (produceString.keyPressed(key)) {
				return true;
			}
			return false;
		}	
	}
	
	
	private class DialogEditInt extends EditorDialog<Boolean> {
		
		private EditorDialog<String> produceInt;
		
		private Laxel parent;
		
		int value = 0;
		
		public DialogEditInt(Laxel parent) {
			produceInt = new DialogTextEntry();
			childDialog = produceInt;
			this.parent = parent;
			displayName = "EDIT";
		}

		@Override
		public Boolean execute() {
			parent.editValue(value);
			return true;
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (produceInt.keyPressed(key)) {
				String s = produceInt.execute();
				try {
					int i = Integer.valueOf(s);	
					value = i;
					execute();
					return true;
				}
				catch (NumberFormatException e) {
					produceInt = new DialogTextEntry();
				}
				
			}
			return false;
		}
	}
	
	
	private class DialogInsert extends EditorDialog<Boolean> {
		
		private Laxel parent;
		private Laxel newLaxel = null;
		
		private EditorDialog<Character> produceSide;
		private EditorDialog<Character> produceIOId;
		private EditorDialog<Laxel> produceLaxel;
		
		Laxel.Direction direction = Laxel.Direction.NONE;
		int id = -1;
		
		int hkState;
		
		public DialogInsert(Laxel parent) {
			this.parent = parent;

			hkState = 0;
			if (parent.inlets.length > 0) {
				hkState += 1;
			}
			if (parent.outlets.length > 0) {
				hkState += 2;
			}
			
			// No IO, can't insert
			if (hkState == 0) {
				complete = true;
			}
			else if (hkState == 1) {
				direction = Laxel.Direction.INLET;
			}
			// Only outlets
			else if (hkState == 2) {
				direction = Laxel.Direction.OUTLET;
			}
			// Inlets and outlets
			else if (hkState == 3) {
				produceSide = new DialogHotkey(new char[]{'i','o'});	
				childDialog = produceSide;
			}
			
			setIOId();
			
			produceLaxel = new DialogCreateLaxel();
			
			if (childDialog == null) {
				childDialog = produceLaxel;
			}
			
			displayName = "INSERT";
			setSubtitle();
		}
		
		private void setIOId() {
			char[] numberKeys = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
			if (hkState == 1) {
				int length = parent.inlets.length;
				if (length == 1) {
					id = 0;
				}
				produceIOId = new DialogHotkey(Arrays.copyOfRange(numberKeys, 0, length));
			}
			// Only outlets
			else if (hkState == 2) {
				int length = parent.outlets.length;
				if (length == 1) {
					id = 0;
				}
				produceIOId = new DialogHotkey(Arrays.copyOfRange(numberKeys, 0, length));
			}
			// Inlets and outlets
			else if (hkState == 3) {
				int length = Math.max(parent.inlets.length, parent.outlets.length);
				if (length == 1) {
					id = 0;
				}
				produceIOId = new DialogHotkey(Arrays.copyOfRange(numberKeys, 0, length));
			}
			
			if (childDialog == null && id != 0) {
				childDialog = produceIOId;
			}
		}
		
		
		private void setSubtitle() {
			if (direction == Laxel.Direction.INLET) {
				subtitle = "INLET";
			}
			else if (direction == Laxel.Direction.OUTLET) {
				subtitle = "OUTLET";
			}
			
			if (direction != Laxel.Direction.NONE) {
				if (id > -1) {
					subtitle += " " + id;
				}
			}
		}
		

		@Override
		public Boolean execute() {
			if (direction == Laxel.Direction.INLET) {
				parent.inlets[id].setTarget(newLaxel);
				newLaxel.outlets[0].setTarget(parent);
			}
			else if (direction == Laxel.Direction.OUTLET) {
				parent.outlets[id].setTarget(newLaxel);
				newLaxel.inlets[0].setTarget(parent);
			}
			return true;
		}

		
		@Override
		public boolean keyPressed(int key) {
			if (direction == Laxel.Direction.NONE) {
				if (produceSide.keyPressed(key)) {
					char c = produceSide.execute();
					if (c == 'i') {
						direction = Laxel.Direction.INLET;
					}
					else if (c == 'o') {
						direction = Laxel.Direction.OUTLET;
					}
					
					if (id == -1) {
						childDialog = produceIOId;
					}
					else if (id == 0) {
						childDialog = produceLaxel;
					}
				}
				
			}
			else if (id == -1) {
				if (produceIOId.keyPressed(key)) {
					char c = produceIOId.execute();
					try {
						id = Integer.valueOf(c + "");
					}
					catch (NumberFormatException e) {
						setIOId();
					}
					
					if (id != -1) {
						childDialog = produceLaxel;
					}
				}
			}
			else {
				if (produceLaxel.keyPressed(key)) {
					newLaxel = produceLaxel.execute();
					if (newLaxel != null) {
						execute();
						return true;	
					}
				}
			}
			setSubtitle();
			return false;
		}
	}
	
	
	private class DialogReplace extends EditorDialog<Boolean> {
		private Laxel laxelOld;
		private Laxel laxelNew;
		
		private EditorDialog<Laxel> produceLaxel;
		
		public DialogReplace(Laxel lexelOld) {
			this.laxelOld = lexelOld;
			
			produceLaxel = new DialogCreateLaxel();
			childDialog = produceLaxel;
			
			displayName = "REPLACE";
			subtitle = laxelOld.displayName;
		}
		
		
		@Override
		public Boolean execute() {
			laxelNew = produceLaxel.execute();
			if (laxelNew == null) {
				produceLaxel = new DialogCreateLaxel();
				return false;
			}
			if (head == point) {
				head = laxelNew;
			}
			point = laxelNew;
			return true;
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (produceLaxel.keyPressed(key)) {
				execute();
				return true;
			}
			return false;
		}
	}
	
	
	private class DialogTextEntry extends EditorDialog<String> {
		String entry = "";
		
		@Override
		public boolean keyPressed(int key) {
			boolean executeFlag = false;
			if (key == PApplet.RETURN || key == PApplet.ENTER) {
				executeFlag = true;
			}
			else if (Character.isLetterOrDigit(key)) {
				entry += Character.toString(key);
			}
			else if (key == PApplet.BACKSPACE) {
				if (entry.length() > 0) {
					entry = entry.substring(0,entry.length() - 1);
				}
			}
			this.displayName = entry;
			return executeFlag;
		}
		
		
		@Override
		public String execute() {
			return entry;
		}
	}
	
	private class DialogHotkey extends EditorDialog<Character> {
		
		private HashSet<Character> characters;
		
		Character output = null;
		
		public DialogHotkey(char[] inputs) {
			characters = new HashSet<Character>();
			for (char c : inputs) {
				characters.add(c);
			}
			
			this.displayName = characters.toString();
		}

		@Override
		public Character execute() {
			return output;
		}

		
		@Override
		public boolean keyPressed(int key) {
			if (characters.contains((char)key)) {
				output = (char)key;
				return true;
			}
			return false;
		}	
	}
	
	
	private class DialogMove extends EditorDialog<Boolean> {
		
		private EditorDialog<String> produceId;
		
		private Laxel.Direction direction;
		
		int id = -1;
		
		public DialogMove(Laxel.Direction direction) {
			this.direction = direction;
			
			produceId = new DialogTextEntry();
			childDialog = produceId;
			
			if (direction == Laxel.Direction.INLET) {
				if (point.inlets.length == 1) {
					if (point.inlets[0].target == null) {
						complete = true;
					}
					else {
						id = 0;
						execute();	
					}
					
				}
				else if (point.inlets.length == 0) {
					complete = true;
				}
			}
			else if (direction == Laxel.Direction.OUTLET) {
				if (point.outlets.length == 1) {
					if (point.outlets[0].target == null) {
						complete = true;
					}
					else {
						id = 0;
						execute();	
					}
				}
				else if (point.outlets.length == 0) {
					complete = true;
				}
			}
			
			displayName = "MOVE";
			subtitle = direction.toString();
		}

		@Override
		public Boolean execute() {
			if (direction == Laxel.Direction.INLET) {
				if (point.inlets.length > id && point.inlets[id].target != null) {
					point = point.inlets[id].target;
					complete = true;
				}
				
				
			}
			else if (direction == Laxel.Direction.OUTLET) {
				if (point.outlets.length > id && point.outlets[id].target != null) {
					point = point.outlets[id].target;
					complete = true;
				}
			}
			return true;
		}
		
		@Override 
		public boolean keyPressed(int key) {
			if (produceId.keyPressed(key)) {
				String s = produceId.execute();
				try {
					id = Integer.valueOf(s);
					execute();
					return true;
				}
				catch (NumberFormatException e) {
					produceId = new DialogTextEntry();
				}
			}
			return false;
		}
	}
	
	
	private class DialogCreateFunction extends EditorDialog<Boolean> {

		private EditorDialog<String> produceFunctionName;
		
		private String name = "";
		
		public DialogCreateFunction() {
			displayName = "CREATE FUNCTION";
			
			produceFunctionName = new DialogTextEntry();
			childDialog = produceFunctionName;
		}
		
		
		@Override
		public Boolean execute() {
			if (functions.containsKey(name) == false) {
				LaxelFunction function = new LaxelFunction();
				functions.put(name, function);	
			}
			return true;
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (produceFunctionName.keyPressed(key)) {
				name = produceFunctionName.execute();
				if (Arrays.asList(reservedNames).contains(name) == false) {
					execute();	
				}
				else {
					produceFunctionName = new DialogTextEntry();
				}
				return true;
			}
			return false;
		}
	}
	
	
	public class DialogCommand extends EditorDialog<Boolean> {
		
		EditorDialog<String> produceCommand;

		String command = "";
		
		public DialogCommand() {
			this.produceCommand = new DialogTextEntry();
			childDialog = produceCommand;
			this.displayName = "COMMAND";
		}
		
		@Override
		public Boolean execute() {
			if (command.equals("inlet")) {
				
				return false;
			}
			return false;
		}
		
		@Override 
		public boolean keyPressed(int key) {
			if (produceCommand.keyPressed(key)) {
				this.command = produceCommand.execute();	
			}
			
			return execute();
		}
	}
}
