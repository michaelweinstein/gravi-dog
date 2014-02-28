package miweinst.engine;

public class Tuple<S, T> {
	
	public final S x;
	public final T y;

	public Tuple(S first, T second) {
		assert first != null;
		assert second != null;
		this.x = first;
		this.y = second;
	}
}
