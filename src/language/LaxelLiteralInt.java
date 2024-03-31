package language;

public class LaxelLiteralInt extends Laxel {
	public int data;
	
	public LaxelLiteralInt() {
		this.data = 0;
		
		this.displayName = "INT " + this.data;
		
		inlets = new Inlet[0];
		outlets = new Outlet[1];
		
		outlets[0] = new Outlet("i", Integer.class, 0);
	}
	
	public LaxelLiteralInt(int data) {
		this.data = data;
		
		this.displayName = "INT " + this.data;
		
		inlets = new Inlet[0];
		outlets = new Outlet[1];
		
		outlets[0] = new Outlet("i", Integer.class, 0);
	}
	
	public boolean execute() {
		outlets[0].data = data;
		super.execute();
		return true;
	}
	
	
	@Override
	public String[] getEditable() {
		return new String[] {"int"};
	}
	
	
	@Override
	public void editValue(int i) {
		this.data = i;
		this.displayName = "INT " + this.data;
	}
	
}
