package thread_pop_up;

import javax.swing.JOptionPane;

import Client.ModelClient;
import Client.RRTimer;

public class popupreflex extends Thread {
	
	
	RRTimer rt ; 
	ModelClient m ;
	
	
	public popupreflex(RRTimer rt,ModelClient m) {
		super();
		this.rt = rt;
		this.m=m;
	}


	@Override
	public void run() {
		
		String rep = JOptionPane
				.showInputDialog("Reflexion : Entrez le nombre de coup");

		if (rep != null) {

			if (rt.isAlive()) {
				rt.setTime(0);
				m.sendtoserver("TROUVE/" + m.getUser()
						+ "/" + rep + "/");
			} else
				JOptionPane.showMessageDialog(m.getRRFrame(), "trop tard !");

		}
	}

}
