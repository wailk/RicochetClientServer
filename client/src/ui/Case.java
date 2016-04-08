package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import Structures.Couleur;
import Structures.Robot;

public class Case extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int CASE_SIZE = 30;

	Robot r= null;

	boolean haut;
	boolean bas;
	boolean gauche;
	boolean droite;

	boolean cible = false;
	Couleur couleurCible = null;

	public Case(boolean haut, boolean bas, boolean gauche, boolean droite) {
		super();
		this.haut = haut;
		this.bas = bas;
		this.gauche = gauche;
		this.droite = droite;
	}

	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		setToolTipText(null);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, CASE_SIZE, CASE_SIZE);
		g2d.setColor(Color.DARK_GRAY);

		if (bas)
			g2d.fillRect( 0,CASE_SIZE, CASE_SIZE, 5);
		if (gauche)
			g2d.fillRect(0, 0, 5, CASE_SIZE);
		if (haut)
			g2d.fillRect(0, 0, CASE_SIZE,5);
		if (droite)
			g2d.fillRect(CASE_SIZE ,0, 5, CASE_SIZE);
		
		
		
		if(this.r != null){
			switch (r.c) {
			case  J: g2d.setColor(Color.YELLOW);
			
			g2d.fillOval(0,0,30,30);
				
				break;
			case  B: g2d.setColor(Color.BLACK);
			g2d.fillOval(0,0,30,30);
			break;
			case  V: g2d.setColor(Color.GREEN);
			g2d.fillOval(0,0,30,30);
			break;
			case  R: g2d.setColor(Color.RED);
			g2d.fillOval(0,0,30,30);
			break;

			default:
				break;
			}
		}
		
		if(this.cible){
			switch (couleurCible) {
			
			case  J: g2d.setColor(Color.YELLOW);
			g2d.fillRect(5, 5, CASE_SIZE-10, CASE_SIZE-10);
				break;
			case  B: g2d.setColor(Color.BLACK);
			g2d.fillRect(5, 5, CASE_SIZE-10, CASE_SIZE-10);
			break;
			case  V: g2d.setColor(Color.GREEN);
			g2d.fillRect(5, 5, CASE_SIZE-10, CASE_SIZE-10);
			break;
			case  R: g2d.setColor(Color.RED);
			g2d.fillRect(5, 5, CASE_SIZE-10, CASE_SIZE-10);
			break;

			default:
				break;
			}
			
			
		}
	}
}
