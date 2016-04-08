package thread_pop_up;

import javax.swing.JOptionPane;

import Client.ModelClient;
import Client.RRTimer;

public class popupenchere extends Thread{

	RRTimer et ; 
	ModelClient m ;
	
	
	public popupenchere(RRTimer rt,ModelClient m) {
		super();
		this.et = rt;
		this.m=m;
	}


	@Override
	public void run() {
		String rep = JOptionPane.showInputDialog("Mettre enchere");

		if (rep != null) {
			if (et.isAlive()) {
				m.sendtoserver("ENCHERE/" + m.getUser()
						+ "/" + rep + "/");
				this.run();
			} else
				JOptionPane.showMessageDialog(m.getRRFrame(), "trop tard !");
		}
	}
		

}
