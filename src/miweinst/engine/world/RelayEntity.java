package miweinst.engine.world;

import java.util.Map;

import miweinst.engine.entityIO.Connection;
import miweinst.engine.entityIO.Input;
import miweinst.engine.entityIO.Output;

public class RelayEntity extends PhysicsEntity {
	//Boolean indicating whether relay is open or closed
	private boolean _enabled;
	
	/* Input classes for opening/closing relay throughway
	 * by running doEnable/doDisable to toggle boolean.*/
	public Input doEnable = new Input() 
	{
		public void run(Map<String, String> args) {
			doEnable();
		}
	};
	public Input doDisable = new Input() 
	{
		public void run(Map<String, String> args) {
			doDisable();
		}
	};	
	/* Input to actually activate the pathway, only goes
	 * through if relay enabled/open.*/	
	public Input doActivate = new Input() 
	{
		public void run(Map<String, String> args) {
			doActivate();
		}
	};
	/*Output is run if relay is enabled and doActivate is called.*/
	public Output onActivate;
	
	public RelayEntity(GameWorld world) {
		super(world);
		onActivate = new Output();
		this.setStatic(true);		
		this.setInteractive(false);
		this.setVisible(false);
		_enabled = false;		
	}
	
	/*Runs Output if relay is open, otherwise
	 * nothing happens.*/
	public void doActivate() {
		if (_enabled)
			onActivate.run();
	}
	
	/*Connects Output to c.*/
	public void connect(Connection c) {
		onActivate.connect(c);
	}
	
	/*Opens and closes the relay, without
	 * triggering actual doActivate(). But allows 
	 * doActivate() to be successfully triggered.*/
	public void doEnable() {
		_enabled = true;
	}
	public void doDisable() {
		_enabled = false;
	}

	/*Sets attributes of RelayEntity based on specific string to 
	 * value represented by String.*/
	public void setProperties(Map<String, String> props) {
		//enabled/disabled; initializes relay to open or closed
		if (props.containsKey("enabled"))
			_enabled = Boolean.parseBoolean(props.get("enabled"));
	}
	
	/*Returns input/output mapped to specific associated String. 
	 * So level editor has access to IO using only Strings.*/
	public Output getOutput(String s) {
		if (new String("onActivate").equals(s)) 
			return onActivate;
		return null;
	}
	public Input getInput(String s) {
		if (new String("doActivate").equals(s))
			return doActivate;
		if (new String("doEnable").equals(s))
			return doEnable;
		if (new String("doDisable").equals(s))
			return doDisable;
		return null;
	}
}
