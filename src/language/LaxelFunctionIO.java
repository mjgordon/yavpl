package language;

public class LaxelFunctionIO extends Laxel {
	public Object data;
	private Direction direction = Direction.NONE;
	
	public LaxelFunctionIO(Object data, Direction direction) {
		this.data = data;
		this.direction = direction;
		
		if (this.direction == Direction.OUTLET) {
			this.inlets = new Inlet[] {new Inlet("o", Object.class, 0)};
			this.outlets = new Outlet[0];
			this.displayName = "RETURN";
		}
		
		else if (this.direction == Direction.INLET) {
			this.inlets = new Inlet[0];
			this.outlets = new Outlet[] {new Outlet("o", Object.class, 0)};
			this.displayName = "PARAM";
		}
	}
	
	
	@Override
	public boolean execute() {
		if (this.direction == Direction.OUTLET) {
			this.data = inlets[0].getData();
		}
		
		else if (this.direction == Direction.INLET) {
			outlets[0].data = this.data;
		}
		
		return true;
	}
	
}
