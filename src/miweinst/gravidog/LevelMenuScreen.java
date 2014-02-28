package miweinst.gravidog;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import miweinst.engine.App;
import miweinst.engine.FileIO;
import cs195n.Vec2f;
import cs195n.Vec2i;

public class LevelMenuScreen extends GravidogScreen {	
	//Color Constants for LevelMenuScreen
	public static final Color BG_COL = Constants.MAIN_BG_COL;
	
	//Andddd the private sector
	private ArrayList<LevelBox> _boxes;
	
	//Static vars, NOT constants
	public static int CURRENT_LEVEL = 1;
	private static HashMap<Integer, Integer> star_map = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> max_star_map = new HashMap<Integer, Integer>();
		
//	private App _app;
	public LevelMenuScreen(App a) {
		super(a);
//		_app = a;
		setBackgroundColor(BG_COL);		
		
		_boxes = new ArrayList<LevelBox>();
		
		//Creates instances of LevelBox
		initBoxes();
		
////// 	TOGGLE BOXES FOR TESTING	
		boolean allOpen;
		allOpen = false;
		if (allOpen) { 
			for (LevelBox box: _boxes) {
				box.setLevelOpen(true);
			}
		}
//////^^^^
		//Load star data upon instantiation
		loadLevel();
	}
	
	/**Creates a LevelBox for each level. This is where you 
	 * should add level boxes to the list if we have created a new one.
	 * Once we have all the LevelBoxes we need, let's throw it into
	 * a loop from 1-n level boxes*/
	private void initBoxes() {
		//Open; In for loop: if i=1, boxes[i].setLevelOpen(true)
		LevelBox first = new LevelBox(1);
		first.box.setColor(Color.PINK);
		first.setLevelOpen(true);
		
		//Closed
		LevelBox second = new LevelBox(2);
		second.box.setColor(Color.LIGHT_GRAY);
		//Closed
		LevelBox third = new LevelBox(3);
		third.box.setColor(Color.LIGHT_GRAY);
		//Closed
		LevelBox fourth = new LevelBox(4);
		fourth.box.setColor(Color.LIGHT_GRAY);
		
		LevelBox fifth = new LevelBox(5);
		fourth.box.setColor(Color.LIGHT_GRAY);
		
		LevelBox sixth = new LevelBox(6);
		sixth.box.setColor(Color.LIGHT_GRAY);
		
		LevelBox seventh = new LevelBox(7);
		seventh.box.setColor(Color.LIGHT_GRAY);	
		
		LevelBox eighth = new LevelBox(8);
		eighth.box.setColor(Color.LIGHT_GRAY);
		
		LevelBox ninth = new LevelBox(9);
		
		LevelBox tenth = new LevelBox(10);
		
		_boxes.add(first);
		_boxes.add(second);
		_boxes.add(third);
		_boxes.add(fourth);
		_boxes.add(fifth);
		_boxes.add(sixth);
		_boxes.add(seventh);
		_boxes.add(eighth);
		_boxes.add(ninth);
		_boxes.add(tenth);
	}
	
	// Save/Load 	
	/**Loads star data for each level and sets
	 * levels open or closed accordingly. 
	 * Called only in constructor of LevelMenuScreen.*/
	private void loadLevel() {
		//First level is always open
		_boxes.get(0).setLevelOpen(true);
		//Load star data upon instantiation
		Map<String, String> starData = FileIO.read();
		for (Integer i=0; i<starData.size(); i++) {
			if (i > 0) {
				//Get star count of previous level to set open/closed
				Integer j = i-1;
				Integer lastCount = Integer.parseInt(starData.get(j.toString()));
				//If previous level has 1-3 stars
				if (lastCount > 0) {
					_boxes.get(i).setLevelOpen(true);
				}
			}		
			//Update star count
			Integer starCount = Integer.parseInt(starData.get(i.toString()));			
			max_star_map.put(i, starCount);
			_boxes.get(i).setStars(starCount);
		}
	}
	
	/**Saves star data for each level to save_data.txt.*/
	public static void save() {
		HashMap<String, String> saveData = new HashMap<String, String>();
		ArrayList<String> saveList = new ArrayList<String>();
		for (Integer i=0; i<max_star_map.size(); i++) {
			if (max_star_map.containsKey(i)) {
				saveData.put(i.toString(), max_star_map.get(i).toString());
			}
			else {
				saveData.put(i.toString(), new Integer(0).toString());
			}
			String line = i.toString() + ": " + saveData.get(i.toString());
			saveList.add(line);
		}
		FileIO.write(saveList);
	}
	
	/** GO! */
	private void startLevel(String lvlPath) {
		if (lvlPath != null)
			app.setScreen(new PlayScreen(app, new File(lvlPath)));
	}
	
	/* Information on level menu, draws updated menu. */
	/**Sets the level for the specified box
	 * number to be open. Also sets frame visible. */
	public void openLevel(int boxNumber) {
		if (boxNumber < _boxes.size()) {
			//Adjust for zero-indexing for ArrayList
			_boxes.get(boxNumber).setLevelOpen(true);
		}
	}
	
	/** Erases all save data and star maps. */
	public static void clear() {
		FileIO.write(new ArrayList<String>());
		max_star_map.clear();
		star_map.clear();
	}
	
	/* Star methods */
	/**Increments by one star on current level.*/	
	public static void addStar() {
		if (star_map.containsKey(CURRENT_LEVEL-1)) {
			//Zero indexing
			star_map.put(CURRENT_LEVEL-1, star_map.get(CURRENT_LEVEL-1) + 1);
		}
		else {
			star_map.put(CURRENT_LEVEL-1, 1);
		}
	}
/*	*//** Returns 0 if lvl not contained in HashMap*//*
	public static int getStars() {
		int lvl = CURRENT_LEVEL;
		if (star_map.containsKey(lvl))
			return star_map.get(lvl);
		else
			return 0;
	}*/
	/**Returns star_map earned at specified level number*/
	public static int getStarsFor(int level) {
		return star_map.get(level);
	}
	/**Called if Player dies or level is restarted,
	 * delete stars earned thus far.*/
	public static void levelIncomplete() {
		int index = CURRENT_LEVEL-1;
		star_map.put(index, 0);
	}
	
	/**Sets stars of LevelBox to current stars if
	 * the score is higher than max score, else to max
	 * score again.*/
	public void updateStars() {
		for (int i=0; i<_boxes.size(); i++) {
			if (star_map.containsKey(i)) {
				int stars = star_map.get(i);
				if (max_star_map.containsKey(i)) {
					if (stars > max_star_map.get(i)) {
						max_star_map.put(i, stars);
					}				
				}
				else {
					max_star_map.put(i, stars);
				}
			}
			//Levels not yet played through
			else {
				_boxes.get(i).setStars(0);
				if (!max_star_map.containsKey(i))
					max_star_map.put(i, 0);
			}
			_boxes.get(i).setStars(max_star_map.get(i));
		}
		clearStars();
	}
	/**Clears current level's star information, but retains
	 * high score star information for all levels.*/
	public void clearStars() {
		star_map.clear();
	}
	
	/* User Input methods */
	@Override 
	public void onMousePressed(MouseEvent e) {
		super.onMousePressed(e);
		for (LevelBox box: _boxes) { 
			if (box.contains(new Vec2f(e.getX(), e.getY()))) {
				if (box.isLevelOpen()) {
					CURRENT_LEVEL = box.level_num;
					this.startLevel(box.level_path);
				}
			}	
		}
	}
	
	@Override
	public void onMouseMoved(MouseEvent e) {
		super.onMouseMoved(e);
		Vec2f mouseLoc = new Vec2f(e.getX(), e.getY());
		for (LevelBox box: _boxes) {
			if (box.contains(mouseLoc)) 
				box.onMouseOver();
			else
				box.onMouseOut();
		}
	}
	
	@Override
	public void onResize(Vec2i size) {
		super.onResize(size);
		Vec2f newSize = new Vec2f(size);
		///CHANGES LOCATION BY THE AMOUNT RESIZED (SKIP THIS)
		Vec2f oldSize = initialDimensions;
		//Half the difference in sizes	
		Vec2f delta2 = newSize.minus(oldSize).sdiv(2f);

		Vec2f offset = delta2;
		LevelBox.setOffset(offset);
		for (LevelBox box: _boxes) {
			box.updateBoxLocation();
		}
	}
	
	@Override
	public void onDraw(Graphics2D g) {
		super.onDraw(g);
		for (LevelBox box: _boxes) {
			box.draw(g);
		}
	}
}
