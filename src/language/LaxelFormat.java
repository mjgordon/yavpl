package language;

public class LaxelFormat extends Laxel {
	
	public String formatString;
	
	
	public LaxelFormat() {
		this.formatString = "%s";
		this.displayName = "FORMAT";
		
		this.inlets = new Inlet[1];
		this.outlets=  new Outlet[1];
		
		this.inlets[0] = new Inlet("o", Object.class, 0);
		this.outlets[0] = new Outlet("s", String.class, 0);
	}
	
	
	public boolean execute() {
		Object input = this.inlets[0].getData();
		String s = String.format(formatString, input);
		this.outlets[0].data = s;
		return true;
	}
}
