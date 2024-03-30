package language;

public class LaxelLiteralInt extends Laxel {
	public int data;
	
	public LaxelLiteralInt(int input) {
		this.data = input;
		
		this.displayName = "INT " + this.data;
		
		inlets = new Inlet[0];
		outlets = new Outlet[1];
		
		outlets[0] = new Outlet("i", Integer.class);
	}
	
	public boolean execute() {
		outlets[0].set(data);
		super.execute();
		return true;
	}

	@Override
	public void clear() {
		outlets[0].set(null);
		super.clear();
	}
}
