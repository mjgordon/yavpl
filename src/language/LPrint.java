package language;

public class LPrint extends Laxel {
	public LPrint() {
		displayName = "print";
		
		inlets = new Inlet[1];
		outlets = new Outlet[0];
	
		inlets[0] = new Inlet("input", String.class);
	}

	@Override
	public void execute() {
		String s = (String)inlets[0].get();
		
		System.out.println(s);
	}
}
