package ui;

import language.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import static ui.Bridge.p;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Editor {
	
	PGraphics canvas;
	PGraphics canvasWorking;
	
	
	int defaultWidth = 200;
	int defaultHeight = 20;
	int gutter = 10;
	int ioOffset = 50;
	
	public Laxel point;
	
	private EditorDialog<?> dialogs = null;
	
	public Editor(PGraphics canvas) {
		this.canvas = canvas;
		
		canvasWorking = p.createGraphics(canvas.width, canvas.height);
	}
	
	public void draw() {
		canvas.beginDraw();
		canvas.background(255);
		
		
		
		if (dialogs != null) {
			canvasWorking.beginDraw();
			canvas.image(dialogs.draw(), EditorDialog.gutter, EditorDialog.gutter);
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
	}
	
	private class LaxelDisplay {
		public Laxel laxel;
		public int x = 0;
		public int y = 0;
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
	
	
	public LaxelDisplay getTree(Laxel laxel) {
		return getTree(laxel, Laxel.Direction.BOTH, 0);
	}
	
	public LaxelDisplay getTree(Laxel laxel, Laxel.Direction direction, int depth) {
		int maxDepth = 3;
		
		LaxelDisplay ld = new LaxelDisplay(laxel);
		
		int greatestInletWidth = 0;
		int greatestOutletWidth = 0;
		
		
		if (direction == Laxel.Direction.INLET || direction == Laxel.Direction.BOTH) {
			for (Laxel.Inlet inlet : laxel.inlets) {
				if (inlet == null || inlet.target == null || depth >= maxDepth) {
					ld.inlets.add(null);
				}
				else {
					LaxelDisplay branch = getTree(inlet.target, Laxel.Direction.INLET, depth + 1);
					ld.inlets.add(branch);
					ld.inletTextWidth = Math.max(ld.inletTextWidth, p.textWidth(inlet.name));
					greatestInletWidth = Math.max(greatestInletWidth, branch.width);
					
				}
			}	
		}
		
		if (direction == Laxel.Direction.OUTLET || direction == Laxel.Direction.BOTH) {
			for (Laxel.Outlet outlet : laxel.outlets) {
				if (outlet == null || outlet.target == null || depth >= maxDepth) {
					ld.outlets.add(null);
				}
				else {
					LaxelDisplay branch = getTree(outlet.target, Laxel.Direction.OUTLET, depth + 1);
					ld.outlets.add(branch);
					ld.outletTextWidth = Math.max(ld.outletTextWidth, p.textWidth(outlet.name));
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
		
		ld.width = (int)(ld.inletTextWidth + ld.outletTextWidth + p.textWidth(laxel.displayName) + (gutter * 4));
		
		
		return ld;
	}
	
	public void renderLaxel(LaxelDisplay ld) {
		renderLaxel(ld, Laxel.Direction.BOTH);
	}
	
	
	public void renderLaxel(LaxelDisplay ld, Laxel.Direction direction) {
		Laxel laxel = ld.laxel;
		
		
		canvas.strokeWeight(laxel == point ? 2 : 1);
		
		canvas.stroke(0);
		canvas.noFill();
		canvas.rect(0,0, ld.width, ld.height);
		canvas.noStroke();
		canvas.fill(0);
		canvas.textAlign(PApplet.LEFT, PApplet.CENTER);
		canvas.text(laxel.displayName, gutter + ld.inletTextWidth + gutter, ld.height / 2);
		
		if (direction == Laxel.Direction.INLET || direction == Laxel.Direction.BOTH) {	
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
				canvas.text(inlet.name, 10, y - 10);
				canvas.text(inlet.dataType.getSimpleName(), 10, y + 10);
				
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
		
		if (direction == Laxel.Direction.OUTLET || direction == Laxel.Direction.BOTH) {	
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
				canvas.text(outlet.name, ld.width - 10, y - 10);
				canvas.text(outlet.dataType.getSimpleName(), ld.width - 10, y + 10);
				
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
		
		else if (key == 'i') {
			dialogs = new DialogInsert(point);
		}
		
		
		
		else if (key == 'r') {
			dialogs = new DialogReplace(point);
		}
		
		if (dialogs != null && dialogs.complete) {
			dialogs = null;
		}
		
		
		
	}
	
	
	private abstract class EditorDialog<T> {	
		public abstract T execute();
		
		public abstract PImage drawLocal();
	
		public final static int gutter = 3;
		
		public static final int baseWidth = 100;
		public static final int baseHeight = 20;
		
		public int width = 100;
		public int height = 20;
		
		public boolean complete = false;
		
		public boolean keyPressed(int key) {
			return false;
		}
		
		public PImage draw() {
			
			width = baseWidth;
			height = baseHeight;
			
			
			
			PImage child = drawLocal();
			

			//canvasDialog.fill(255);
			canvasWorking.noFill();
			canvasWorking.stroke(0);
			canvasWorking.rect(0,0,width - 1, height - 1);
			
			if (child != null) {
				canvasWorking.image(child, width - gutter - child.width, height / 2 - child.height / 2);	
			}
				
			PImage output = canvasWorking.get(0,0,width, height);
			
			return output;
			
		}
	}
	
	
	private class DialogCreateLaxel extends EditorDialog<Laxel> {		
		EditorDialog<String> produceString;

		public DialogCreateLaxel() {
			produceString = new DialogTextEntry();
		}

		@Override
		public Laxel execute() {
			String name = produceString.execute();
			
			Laxel output = null;
			
			switch(name) {
				case "print":
					output = new LPrint();
				break;
				case "int":
					output = new LiteralInt(0);
				break;
			}
			
			
			return output;
		}
		
		@Override
		public PImage drawLocal() {	
			PImage child = produceString.draw();
			
			if (child != null) {
				 width += child.width + gutter;
				 height = Math.max(child.height + (gutter * 2), height);
			}
			
			canvasWorking.background(255);
			
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasWorking.text("LAXEL", gutter, height / 2);
			
			return child;
			
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (produceString.keyPressed(key)) {
				return true;
			}
			return false;
		}	
	}
	
	
	private class DialogInsert extends EditorDialog<Boolean> {
		
		private Laxel parent;
		private Laxel newLaxel = null;
		
		private EditorDialog<Character> produceSide;
		private EditorDialog<String> produceInletId;
		private EditorDialog<Laxel> produceLaxel;
		
		Boolean side = null;
		int id = -1;
		
		public DialogInsert(Laxel parent) {
			char[] hotkeys = {'i','o'};
			produceSide = new DialogHotkey(hotkeys);
			produceInletId = new DialogTextEntry();
			produceLaxel = new DialogCreateLaxel();
			
			this.parent = parent;
		}

		@Override
		public Boolean execute() {
			if (side == true) {
				parent.inlets[id].setTarget(newLaxel);
				newLaxel.outlets[0].setTarget(parent);
			}
			else if (side == false) {
				parent.outlets[id].set(newLaxel);
				parent.inlets[0].setTarget(parent);
			}
			return true;
		}

		@Override
		public PImage drawLocal() {
			height += 20;
			
			PImage child = null;
			if (side == null) {
				child = produceSide.draw();
			}
			else if (id == -1) {
				child = produceInletId.draw();
			}
			else {
				child = produceLaxel.draw();
			}
			
			
			if (child != null) {
				 width += child.width + gutter;
				 height = Math.max(child.height + (gutter * 2), height);
			}
			
			canvasWorking.background(255);
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			
			String title = "INSERT";
			if (side != null) {
				if (side == true) {
					title += " INLET";
				}
				else if (side == false) {
					title += " OUTLET";
				}	
			}
			
			canvasWorking.text(title, gutter, height / 2);
			
			return child;
		}
		
		@Override
		public boolean keyPressed(int key) {
			if (side == null) {
				if (produceSide.keyPressed(key)) {
					side = produceSide.execute() == 'i';
				}
				return false;
			}
			else if (id == -1) {
				if (produceInletId.keyPressed(key)) {
					String s = produceInletId.execute();
					try {
						id = Integer.valueOf(s);
					}
					catch (NumberFormatException e) {
						produceInletId = new DialogTextEntry();
					}
				}
				return false;
			}
			else {
				if (produceLaxel.keyPressed(key)) {
					newLaxel = produceLaxel.execute();
					execute();
					return true;
				}
				return false;
			}
		}
		
	}
	
	
	private class DialogReplace extends EditorDialog<Boolean> {
		private Laxel laxelOld;
		private Laxel laxelNew;
		
		private EditorDialog<Laxel> produceLaxel;
		
		public DialogReplace(Laxel lexelOld) {
			this.laxelOld = lexelOld;
			
			produceLaxel = new DialogCreateLaxel();
		}
		
		@Override
		public PImage drawLocal() {
			height += 20;
			
			PImage child = produceLaxel.draw();
			
			if (child != null) {
				 width += child.width + gutter;
				 height = Math.max(child.height + (gutter * 2), height);
			}
			
			canvasWorking.background(255);
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasWorking.text("REPLACE", gutter, height / 2);
			canvasWorking.text(laxelOld.displayName, gutter, height / 2 + 12);
			
			return child;
		}
		
		@Override
		public Boolean execute() {
			laxelNew = produceLaxel.execute();
			if (laxelNew == null) {
				produceLaxel = new DialogCreateLaxel();
				return false;
			}
			//laxelNew.inlets = laxelOld.inlets;
			//laxelNew.outlets = laxelOld.outlets;
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
			return executeFlag;
		}
		
		
		@Override
		public PImage drawLocal() {
			canvasWorking.background(255);
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasWorking.text(entry, gutter, height / 2);
			
			return null;
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
		}

		@Override
		public Character execute() {
			return output;
		}

		@Override
		public PImage drawLocal() {
			canvasWorking.background(255);
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasWorking.text(characters.toString(), gutter, height / 2);
			
			return null;
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
		
		EditorDialog<String> produceId;
		
		Laxel.Direction direction;
		
		int id = -1;
		
		public DialogMove(Laxel.Direction direction) {
			this.direction = direction;
			
			produceId = new DialogTextEntry();
			
			if (direction == Laxel.Direction.INLET) {
				if (point.inlets.length == 1) {
					id = 0;
					execute();
				}
				else if (point.inlets.length == 0) {
					complete = true;
				}
			}
			else if (direction == Laxel.Direction.OUTLET) {
				if (point.outlets.length == 1) {
					id = 0;
					execute();
				}
				else if (point.outlets.length == 0) {
					complete = true;
				}
			}
		}

		@Override
		public Boolean execute() {
			if (direction == Laxel.Direction.INLET) {
				if (point.inlets.length > id) {
					point = point.inlets[id].target;
					complete = true;
				}
				
			}
			else if (direction == Laxel.Direction.OUTLET) {
				if (point.outlets.length > id) {
					point = point.outlets[id].target;
					complete = true;
				}
			}
			return true;
		}

		@Override
		public PImage drawLocal() {
			height += 20;
			
			PImage child = produceId.draw();
			
			if (child != null) {
				 width += child.width + gutter;
				 height = Math.max(child.height + (gutter * 2), height);
			}
			
			canvasWorking.background(255);
			canvasWorking.fill(0);
			canvasWorking.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasWorking.text("MOVE", gutter, height / 2 - 10);
			canvasWorking.text(direction.toString(), gutter, height / 2 + 10);
			
			
			return child;
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
	
}
