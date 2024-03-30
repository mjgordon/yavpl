package vm;

import language.Laxel;

public class VM {
	public static void execute(Laxel laxel) {
		clearLaxel(laxel);
		runLaxel(laxel);
	}
	
	
	public static void runLaxel(Laxel laxel) {
		for (Laxel.Inlet inlet : laxel.inlets) {
			if (inlet.target != null && inlet.target.runFlag == false) {
				runLaxel(inlet.target);
			}
		}
		
		laxel.execute();
	}
	
	
	public static void clearLaxel(Laxel laxel) {
		laxel.clear();
		
		for (Laxel.Inlet inlet : laxel.inlets) {
			if (inlet.target != null) {
				clearLaxel(inlet.target);	
			}
		}
	}
}
