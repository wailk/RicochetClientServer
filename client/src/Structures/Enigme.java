package Structures;

import java.util.ArrayList;



public class Enigme {
	
public Couleur CouleurCible ;
public int xr,yr ,xj,yj ,xb,yb ,xv ,yv , xc ,yc  ;

public ArrayList<Robot> robots ;

public Enigme(int xr, int yr, int xj, int yj, int xb, int yb, int xv, int yv, int xc, int yc ,Couleur couleurCible) {
	CouleurCible = couleurCible;
	this.xr = xr;
	this.yr = yr;
	this.xj = xj;
	this.yj = yj;
	this.xb = xb;
	this.yb = yb;
	this.xv = xv;
	this.yv = yv;
	this.xc = xc;
	this.yc = yc;
	this.robots = new ArrayList<Robot>();
	robots.add(new Robot(xr,yr,Couleur.R));
	robots.add(new Robot(xj,yj,Couleur.J));
	robots.add(new Robot(xb,yb,Couleur.B));
	robots.add(new Robot(xv,yv,Couleur.V));
	
	
}


public void modifier (int xr, int yr, int xj, int yj, int xb, int yb, int xv, int yv, int xc, int yc ,Couleur couleurCible){
	CouleurCible = couleurCible;
	this.xr = xr;
	this.yr = yr;
	this.xj = xj;
	this.yj = yj;
	this.xb = xb;
	this.yb = yb;
	this.xv = xv;
	this.yv = yv;
	this.xc = xc;
	this.yc = yc;
	
}
 public ArrayList<Robot> getRobots(){ return this.robots;}

}
