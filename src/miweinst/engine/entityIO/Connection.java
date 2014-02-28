package miweinst.engine.entityIO;

import java.util.HashMap;
import java.util.Map;

public class Connection {
	private Input target;
	private Map<String, String> args;
	
	/*Two constructors, one to specify HashDecorator args
	 * upon instantiation, and one without.*/
	public Connection(Input out) {
		target = out;
		args = new HashMap<String, String>();
	}
	public Connection(Input out, Map<String, String> data) {
		assert data != null;
		target = out;
		args = data;
	}
	
	/*Add arguments for this Connection to pass to connected Input.*/
	public void setDecoration(String key, String val) {
		args.put(key, val);
	}
	public void setProperties(Map<String, String> data) {
		args = data;
	}
	
	/*Forward arguments to specified Input.*/
	public void run() {
		target.run(args);		
	}
}
