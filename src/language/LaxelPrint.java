package language;

public class LaxelPrint extends Laxel {
	
	public LaxelPrint() {
		displayName = "print";
		
		inlets = new Inlet[1];
		outlets = new Outlet[0];
	
		inlets[0] = new Inlet("s", String.class, 0);
	}

	
	@Override
	public boolean execute() {
		Object i0 = inlets[0].getData();
		if (i0 instanceof Error) {
			return false;
		}
		else {
			String s = (String)i0;			
			System.out.println(s);			
			super.execute();			
			return true;
		}
	}
}
