package thread_pop_up;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import Client.ModelClient;
import Client.RRTimer;

public class popupresolution extends Thread {
	
	RRTimer resot ; 
	ModelClient m ;
	
	
 public popupresolution(RRTimer rt,ModelClient m){
		super();
		this.resot = rt;
		this.m=m;
	}
 @Override
	public void run() {
	 String rep = JOptionPane
				.showInputDialog("Resolution : Entrez les deplacements");

		if (rep != null) {
			if (resot.isAlive()) {
				resot.setTime(0);
				rep = rep.toUpperCase();
				if (isValidMoves(rep))
					m.sendtoserver("SOLUTION/"
							+ m.getUser() + "/" + rep + "/");
				else {
					JOptionPane.showMessageDialog(m.getRRFrame(),
							"Faute de frappe re-saisir les déplacement !");
					this.run();
				}
			} else
				JOptionPane.showMessageDialog(m.getRRFrame(), "trop tard !");
		}
}
 private boolean isValidMoves(String rep) {
		Pattern p = Pattern.compile("^([RJVB][DGHB])+$") ;      
		Matcher m = p.matcher(rep) ;    
		  return m.matches() ;
		
	}
}
