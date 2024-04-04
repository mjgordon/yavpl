package language;

public class LaxelFunction extends Laxel {
	LaxelFunctionIO[] inletLaxels;
	LaxelFunctionIO[] outletLaxels;
	
	public LaxelFunction() {
		inlets = new Inlet[] {new Inlet("o", Object.class, 0)};
		outlets = new Outlet[] {new Outlet("o", Object.class, 0)};
		
		inletLaxels = new LaxelFunctionIO[]{new LaxelFunctionIO(null, Direction.INLET)};
		outletLaxels = new LaxelFunctionIO[]{new LaxelFunctionIO(null, Direction.OUTLET)};
		
		outletLaxels[0].inlets[0].setTarget(inletLaxels[0]);
	}
	
	
	@Override
	public boolean execute() {
		boolean flag = true;
		
		for (int i = 0; i < inletLaxels.length; i++) {
			inletLaxels[i].data = inlets[i].getData();
		}
		
		for (int i = 0; i < outletLaxels.length; i++) {
			outletLaxels[i].clear();
		}
		
		for (int i = 0; i < outletLaxels.length; i++) {
			if (outletLaxels[i].execute() == false) {
				flag = false;
			}
		}
		
		return flag;
	}
}
