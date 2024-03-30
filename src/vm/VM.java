package vm;

import language.Laxel;

public class VM {
	public void execute(Laxel laxel) {
		clearLaxel(laxel);
		
		runLaxel(laxel);
		
	}
	
	
	private void runLaxel(Laxel laxel) {
		for (Laxel.Inlet inlet : laxel.inlets) {
			if (inlet.target != null && inlet.target.runFlag == false) {
				runLaxel(inlet.target);
			}
		}
		
		laxel.execute();
	}
	
	
	private void clearLaxel(Laxel laxel) {
		laxel.clear();
		
		for (Laxel.Inlet inlet : laxel.inlets) {
			if (inlet.target != null) {
				clearLaxel(inlet.target);	
			}
			
		}
	}
}
