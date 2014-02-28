package miweinst.engine.entityIO;

import java.util.ArrayList;

public class Output {
	
	private ArrayList<Connection> connections;
	
	public Output() {
		connections = new ArrayList<Connection>();
	}
	
	public void connect(Connection c) {
		connections.add(c);
	}
	
	public void run() {
		for (Connection c: connections) {
			c.run();
		}
	}
}
