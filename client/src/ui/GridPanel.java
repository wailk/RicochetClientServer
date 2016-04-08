package ui;

import java.awt.*; // Uses AWT's Layout Managers


import javax.swing.*; // Uses Swing's Container/Components

import Structures.Couleur;
import Structures.Mur;
import Structures.Robot;

import java.util.ArrayList;

public class GridPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Name-constants for the game properties
	public static final int GRID_SIZE = 16; // Size of the board

	// Name-constants for UI control (sizes, colors and fonts)
	public static final int CELL_SIZE = 60; // Cell width/height in pixels
	public static final int CANVAS_WIDTH = (CELL_SIZE + 3) * GRID_SIZE;
	public static final int CANVAS_HEIGHT = (CELL_SIZE + 3) * GRID_SIZE;
	// Board width/height in pixels
	public static final Color OPEN_CELL_BGCOLOR = Color.YELLOW;
	public static final Color OPEN_CELL_TEXT_YES = new Color(0, 255, 0); // RGB
	public static final Color OPEN_CELL_TEXT_NO = Color.RED;
	
	
	private Case[][] tfCells = new Case[GRID_SIZE][GRID_SIZE];
	// For testing, open only 2 cells.

	

	// private Side[][] horizontal_Murs = new Side[GRID_SIZE-1][GRID_SIZE];

	public GridPanel(ArrayList<Mur> Murs,ArrayList<Robot> Robots ,int xc ,int yc ,Couleur c) {

		GridLayout layout = new GridLayout(GRID_SIZE, GRID_SIZE);
		setLayout(layout);
		
		
		for (int row = 0; row < GRID_SIZE; ++row) {
			for (int col = 0; col < GRID_SIZE; ++col) {
				
				tfCells[row][col] = new Case(false,false,false,false);
				add(tfCells[row][col]);
			}
		}

		for (Mur m : Murs) {
			switch(m.s){
			case B : tfCells[m.x][m.y].bas =  true ;
				break ;
			case H : tfCells[m.x][m.y].haut =  true ;
			break ;
			case D : tfCells[m.x][m.y].droite =  true ;
			break ;
			case G : tfCells[m.x][m.y].gauche =  true ;
			break ;
			default:
				break;
			}
			
			tfCells[m.x][m.y].repaint();
			
		}
		for (Robot r : Robots) {
			int x = r.x;
			int y = r.y;
			tfCells[x][y].r = r;
			tfCells[x][y].repaint();
			
		}
		tfCells[xc][yc].cible = true;
		tfCells[xc][yc].couleurCible = c;
		tfCells[xc][yc].repaint();
	}

	
	/*
	
	/** The entry main() entry method */
	/*
	public static void main(String[] args) {

		// MurS
		ArrayList<Mur> arr = new ArrayList<Mur>();
		arr.add(new Mur(10, 10, Side.B));
		arr.add(new Mur(12, 13, Side.G));

		JFrame frame = new JFrame("test");
		GridPanel game = new GridPanel(arr);
		// frame.setResizable(false);
		frame.getContentPane().add(game);
		frame.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		frame.pack();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Handle window
																// closing
		frame.setTitle("Sudoku");
		frame.setVisible(true);
		game.setVisible(true);
	}
	*/
}
