package ui;

import language.*;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import static ui.Bridge.p;

public class Editor {
	
	PGraphics canvas;
	PGraphics canvasDialog;
	
	
	int defaultWidth = 60;
	int defaultHeight = 20;
	int gutter = 10;
	
	public Laxel head;
	public Laxel point;
	
	private EditorDialog<?> dialogs = null;
	
	public Editor(PGraphics canvas) {
		this.canvas = canvas;
		
		canvasDialog = p.createGraphics(canvas.width, canvas.height);
	}
	
	public void draw() {
		canvas.beginDraw();
		canvas.background(255);
		
		
		
		if (dialogs != null) {
			canvasDialog.beginDraw();
			canvas.image(dialogs.draw(), EditorDialog.gutter, EditorDialog.gutter);
			canvasDialog.endDraw();
		}
		
		
		canvas.stroke(0);
		canvas.noFill();
		canvas.strokeWeight(1);
		canvas.rect(0,0,canvas.width - 1,canvas.height - 1);
		
		canvas.translate(canvas.width / 2,canvas.height / 2);
		renderLaxel(point);
		
		
		canvas.endDraw();
	}
	
	
	public void renderLaxel(Laxel laxel) {
		
		canvas.strokeWeight(laxel == point ? 2 : 1);
		
		canvas.stroke(0);
		canvas.noFill();
		canvas.rect(0,0, defaultWidth, defaultHeight);
		canvas.noStroke();
		canvas.fill(0);
		canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
		canvas.text(laxel.displayName, defaultWidth / 2, defaultHeight / 2);
	}
	
	
	public void keyPressed(int key) {
		if (dialogs != null) {
			if (dialogs.keyPressed(key)) {
				dialogs = null;
			}
		}
		
		else if (key == 'r') {
			dialogs = new DialogReplace(point);
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
		
		public boolean keyPressed(int key) {
			return false;
		}
		
		public PImage draw() {
			
			width = baseWidth;
			height = baseHeight;
			
			
			
			PImage child = drawLocal();
			

			//canvasDialog.fill(255);
			canvasDialog.noFill();
			canvasDialog.stroke(0);
			canvasDialog.rect(0,0,width - 1, height - 1);
			
			if (child != null) {
				canvasDialog.image(child, width - gutter - child.width, height / 2 - child.height / 2);	
			}
				
			PImage output = canvasDialog.get(0,0,width, height);
			
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
			if (name.equals("print")) {
				output = new LPrint();
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
			
			canvasDialog.background(255);
			
			canvasDialog.fill(0);
			canvasDialog.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasDialog.text("LAXEL", gutter, height / 2);
			
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
			
			canvasDialog.background(255);
			canvasDialog.fill(0);
			canvasDialog.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasDialog.text("REPLACE", gutter, height / 2);
			canvasDialog.text(laxelOld.displayName, gutter, height / 2 + 12);
			
			return child;
		}
		
		@Override
		public Boolean execute() {
			laxelNew = produceLaxel.execute();
			if (laxelNew == null) {
				produceLaxel = new DialogCreateLaxel();
				return false;
			}
			laxelNew.inputs = laxelOld.inputs;
			laxelNew.outputs = laxelOld.outputs;
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
					entry = entry.substring(entry.length() - 2);
				}
			}
			return executeFlag;
		}
		
		
		@Override
		public PImage drawLocal() {
			canvasDialog.background(255);
			canvasDialog.fill(0);
			canvasDialog.textAlign(PApplet.LEFT, PApplet.CENTER);
			canvasDialog.text(entry, gutter, height / 2);
			
			return null;
		}
		
		@Override
		public String execute() {
			return entry;
		}
	}
	
}
