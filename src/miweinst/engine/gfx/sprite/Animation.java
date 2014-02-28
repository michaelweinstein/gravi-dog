package miweinst.engine.gfx.sprite;


public class Animation {
	public static final String string = "Animation";
	
	//A counter for the loops; can go arbitrarily high
	private int _loopCounter;
	//Counts frames in the loop, so it knows when to increment _loopCounter
//	private int _frameCounter;
	//The speed at which frames are updated, in ms
	private int _animSpeed;
	//Counter to allow next frame when _animSpeed is reached
	private int _timer;	
	
	public boolean _running;
	
	private Sprite _currLoop;	
		
	public Animation() {
		_loopCounter = 0;
//		_frameCounter = 0;
		_timer = 0;
		
		_running = false;
		
		//200 milliseconds 
		_animSpeed = 200;
	}
	
	/**
	 * This gives the Animation a reference to the
	 * Sprite that it is animating. The same Animation
	 * object can run the animation loop
	 * of any sprite that is stored in its
	 * _currLoop var.
	 * @param loop
	 */
	public void setLoop(Sprite loop) {
		_currLoop = loop;
	}
	
	/**
	 * Has to be called from an onTick method, cycles through 
	 * frames of Sprite stored in _currLoop. Sprite's frames
	 * are stored in order. If this is not the case,
	 * the Sprite has a setLoopOrder method to change
	 * the order of the ArrayList storing its frames, so
	 * this runLoop method can be generic.
	 */
	public void runLoop() {		
		if (_running) {
			if (_loopCounter % _currLoop.looplength() == 0) {
//				_frameCounter = 0;
				_loopCounter += 1;
			}	
			_currLoop.nextFrame();
//			_frameCounter += 1;
		}
	}
	
	/**
	 * Sets boolean to true, which
	 * means the code in runLoop, which
	 * is called in onTick at _animSpeed,
	 * will be executed.
	 */
	public void startLoop() {
		_running = true;
	}
	
	/**
	 * Stops the loop, resets the counters
	 * and shows the cover frame on sprite.
	 */
	public void stopLoop() {
		_running = false;
		
		_loopCounter = 0;
		_currLoop.showCoverFrame();
	}
	
	/**
	 * This is called when animation should be run, and
	 * it runs the loop. Uses the _timer to vary speed
	 * of animation based on ms spacing between frames.
	 * @param nanos
	 */
	public void onTick(long nanos) {
		//Convert to milliseconds
		_timer += (int) nanos/1000000;
		//Only update frame in runLoop at specified speed
		if (_timer > _animSpeed) {
			this.runLoop();
			_timer = 0;
		}
	}
	
	/**
	 * Returns the number of cycles the loop
	 * has run through.
	 * @return
	 */
	public int getLoopCount() {
		return _loopCounter;
	}
	
/*	public void draw(Graphics2D g, int x1, int y1, int x2, int y2, int sx1, int sy1, int sx2, int sy2) {
		g.drawImage(_currFrame, x1, y1, x2, y2, sx1, sy1, sx2, sy2, null);
	}*/
}
