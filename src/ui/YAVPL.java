package ui;

import language.*;
import processing.core.PApplet;
import processing.core.PGraphics;

public class YAVPL extends PApplet {
	PGraphics g_editor;
	PGraphics g_vm_monitor;
	
	Editor editor;
	
	int gutter = 10;
	
	
	public void settings() {
		size(1500,900);
	}
	
	
	public void setup() {
		
		Bridge.p = this;
		
		int a = (width - (gutter * 3)) * 2 / 3;
		int b = (width - (gutter * 3)) * 1 / 3;
		g_editor = createGraphics(a, height - (gutter * 2));
		g_vm_monitor = createGraphics(b, height - (gutter * 2));
		
		editor = new Editor(g_editor);
		
		editor.head = new NOOP();
		editor.point = editor.head;
	}
	
	
	public void draw() {
		background(200);
		
		stroke(0);
		line(0,0,width,0);
		
		editor.draw();
		
		g_vm_monitor.beginDraw();
		g_vm_monitor.stroke(0);
		g_vm_monitor.rect(0,0,g_vm_monitor.width - 1,g_vm_monitor.height - 1);
		g_vm_monitor.endDraw();
		
		image(g_editor,gutter,gutter);
		image(g_vm_monitor,g_editor.width + (gutter * 2), gutter);
		
		fill(0);
		text((int)frameRate, 10, 10);
	}
	
	
	public void keyPressed() {
		editor.keyPressed(key);
	}
	
	
	public static void main(String[] args) {
		PApplet.main(new String[] { ui.YAVPL.class.getName() });
	}
}
