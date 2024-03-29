package language;

public class LiteralInt extends Laxel {
	public int data;
	
	public LiteralInt(int input) {
		this.data = input;
		
		this.displayName = "INT " + this.data;
		
		inlets = new Inlet[0];
		outlets = new Outlet[1];
		
		outlets[0] = new Outlet("i", Integer.class);
	}
	
	public void execute() {
		outlets[0].set(data);
	}
}
