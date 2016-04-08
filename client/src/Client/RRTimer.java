package Client;


import javax.swing.JLabel;
public class RRTimer  extends Thread {


		/** JLabel lab = affichage du chrnomètre */
		JLabel lab;
		/** int sec = les secondes du chrono */
		int sec;

		/**
		 * Constructeur du chrono
		 * @param l L'etiquette du timer
		 */
		public RRTimer(JLabel l){
			lab = l;
			sec = 60 * 5;
		}

		/**
		 * Methode de lancement du thread chronomètre
		 */
		public void run() {
			try {
				while(sec >=0){
					String min;
					if(sec%60 < 10) min = "0"+sec%60;
					else min = ""+sec%60;
					if(sec < 10)
						lab.setText(String.format("<html><font color='red'>%s:0%s</font></html>", "0",sec));
					else
						lab.setText(sec/60+":"+min);
					sec--;
					Thread.sleep(1000);
				}
			} catch (InterruptedException v) {
				System.out.println(v);
			}
		}  

		/**
		 * Methode pour modifier le chrono
		 * @param t Le nouveau temps du chrono
		 */
		public void setTime(int t){
			sec = t;
		}
	}


