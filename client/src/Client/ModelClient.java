package Client;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import Structures.Couleur;
import Structures.Enigme;
import Structures.Mur;
import Structures.Side;
import Structures.deplacement;
import thread_pop_up.popupenchere;
import thread_pop_up.popupreflex;
import thread_pop_up.popupresolution;
import ui.RRFrame;

public class ModelClient {

	private RRFrame vue;

	private int port;
	/** String user = le nom de l'utilisateur */
	private String user;
	/** String host = l'adresse de connexion (localhost) */
	private String host;
	/** Socket sock = le socket pour acceder au serveur */
	private Socket sock;

	/** BufferedReader inchan = le canal sur lequel le serveur envoie */
	private BufferedReader inchan;
	/** PrintWriter outchan = le canal sur lequel le client envoie */
	private PrintWriter outchan;

	/** List des murs dans le plateau */
	ArrayList<Mur> listmurs;
	/** enigme courante */
	Enigme enigme;

	/** Solutions : une liste des deplacement */
	ArrayList<deplacement> solutionCourante;
	ArrayList<deplacement> solutionFinale;

	popupreflex popr;
	popupenchere popen;
	popupresolution popres;

	public ModelClient(RRFrame fen) {
		vue = fen;
		host = "localhost";
		user = "wail";
		port = 2016;

	}

	public void connexion() {
		try {
			sock = new Socket(host, port);

			InetAddress adresa = sock.getInetAddress();
			System.out.print(
					"Connecting on : " + adresa.getHostAddress() + " with hostname : " + adresa.getHostName() + "\n");

			outchan = new PrintWriter(sock.getOutputStream(), true);
			inchan = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			vue.popupConnexion();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			vue.alert("Erreur de connexion au serveur !!! ");
			e.printStackTrace();
		}
	}

	// sending to server
	public void sendtoserver(String s) {
		s = s.trim();
		outchan.println(s);

	}

	@SuppressWarnings("deprecation")
	public void receivefromserver() {

		String receive;
		try {
			while ((receive = inchan.readLine()) != null) {
				System.out.println("S -> C : " + receive);
				String cmd = receive.split("/")[0];
				String[] args = receive.split("/");

				if (cmd.equals("BIENVENUE")) {
					vue.alert("Validation de la connexion:  " + args[1]);
					this.user = args[1];
					vue.ajouteAuChat("Bienvenue, " + user);
				} else if (cmd.equals("CONNECTE")) {
					vue.ajouteAuServeur("Connexion de " + args[1]);
				} else if (cmd.equals("DECONNEXION")) {
					vue.ajouteAuServeur("Deconnexion de :  " + args[1]);
				} else if (cmd.equals("SESSION")) {
					vue.ajouteAuServeur("Nouvelle session");
					initialisationPlateau(args[1]);
				} else if (cmd.equals("VAINQUEUR")) {
					if (this.popres != null)
						this.popres.stop();
					vue.resot.interrupt();
					vue.ajouteAuServeur(" Fin de la session courante");
					vue.ajouteAuServeur("Scores finaux : " + convertBilan(args[1]));
				} else if (cmd.equals("TOUR")) {
					if (this.popres != null)
						this.popres.stop();
					vue.nouveauTour();
					vue.setrtTime(300);
					initialisationEnigme(args[1]);
					vue.ajouteAuServeur("Tour " + args[2].charAt(0) + ". Scores acutuels : \n" + convertBilan(args[2]));
					this.popr = new popupreflex(vue.rt, this);
					this.popr.start();
				} else if (cmd.equals("TUASTROUVE")) {
					this.popr.stop();
					vue.setetTime(30);
					vue.rt.interrupt();
					vue.alert("Tu as trouvé ! Fin de la phase reflexion");
					this.popen = new popupenchere(vue.et, this);
					this.popen.start();
					// vue.popupEnchere();
				} else if (cmd.equals("ILATROUVE")) {
					this.popr.stop();
					vue.setetTime(30);
					vue.rt.interrupt();
					vue.ajouteAuServeur("Le joueur : " + args[1] + " a trouvé avec " + args[2]
							+ " coups ! Fin de la phase reflexion");
					this.popen = new popupenchere(vue.et, this);
					this.popen.start();
					// vue.popupEnchere();
				} else if (cmd.equals("FINREFLEXION")) {
					this.popr.stop();
					vue.setetTime(30);
					vue.rt.interrupt();
					vue.ajouteAuServeur("Expiration du delai imparti a la reflexion, fin de la phase de reflexion !");
					this.popen = new popupenchere(vue.et, this);
					this.popen.start();
					// vue.popupEnchere();
				}
				// Phase d'enchere
				else if (cmd.equals("VALIDATION")) {
					vue.alert(" Phase d'enchere :votre enchere a été validé");
					// vue.popupEnchere();
				} else if (cmd.equals("ECHEC")) {
					vue.alert("Phase d'enchere :Annulation votre enchere car incoherente avec celle de " + args[1]);
					// vue.popupEnchere();
				} else if (cmd.equals("NOUVELLEENCHERE")) {
					vue.ajouteAuServeur(
							" Phase d'enchere :le joueur :" + args[1] + " a mis une enchere de " + args[2] + " coups");
					// vue.popupEnchere();
				} else if (cmd.equals("FINENCHERE")) {
					vue.ajouteAuServeur(" Phase d'enchere :Fin des encheres , le joueur actif est " + args[1]
							+ " avec une enchere de " + args[2] + " coups !");
					this.popen.stop();
					if (args[1].equals(getUser())) {
						if (this.popen != null)
							this.popen.stop();
						vue.et.interrupt();
						vue.setresotTime(60);
						this.popres = new popupresolution(vue.resot, this);
						this.popres.start();
					}
				}
				// Phase résolution
				else if (cmd.equals("SASOLUTION")) {
					vue.et.interrupt();
					vue.ajouteAuServeur("Phase résolution : Solution proposé par " + args[1] + " est " + args[2]);
					setSolutionCourante(convertSolution(args[2]));
				} else if (cmd.equals("BONNE")) {
					this.popres.stop();
					vue.ajouteAuServeur("Phase résolution : Solution accepté , fin du tour ");
					setSolutionFinale(getSolutionCourante());
				} else if (cmd.equals("MAUVAISE")) {
					vue.ajouteAuServeur(
							"Phase résolution : Solution refusee , passage au solution suivante de " + args[1]);
					if (args[1].equals(getUser())) {
						if (this.popres != null)
							this.popres.stop();
						vue.setresotTime(60);
						this.popres = new popupresolution(vue.resot, this);
						this.popres.start();

					}
				} else if (cmd.equals("FINRESO")) {
					if (this.popres != null)
						this.popres.stop();
					vue.et.interrupt();
					vue.ajouteAuServeur("Phase résolution :Plus de joueurs restants, fin du tour");
				} else if (cmd.equals("TROPLONG")) {
					vue.ajouteAuServeur("Phase résolution :Temps depasse, passage au solution suivante de " + args[1]);
					if (args[1] == getUser()) {
						if (this.popres != null)
							this.popres.stop();
						vue.et.interrupt();
						vue.setresotTime(60);
						this.popres = new popupresolution(vue.resot, this);
						this.popres.start();
					}
				} else if (cmd.equals("LISTEN")) {
					Date dt = new Date();
					Calendar calendar = GregorianCalendar.getInstance(); // creates
																			// a
																			// new
																			// calendar
																			// instance
					calendar.setTime(dt); // assigns calendar to given date
					calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h
														// format
					int heure = calendar.get(Calendar.HOUR_OF_DAY); // gets hour
																	// in 12h
																	// format
					int minute = calendar.get(Calendar.MINUTE); // gets month
																// number, NOTE
																// this is zero
																// based!
					int secondes = calendar.get(Calendar.SECOND);
					String sheure, sminute, ssecondes;
					if (heure < 10)
						sheure = "0" + heure;
					else
						sheure = "" + heure;
					if (minute < 10)
						sminute = "0" + minute;
					else
						sminute = "" + minute;
					if (secondes < 10)
						ssecondes = "0" + secondes;
					else
						ssecondes = "" + secondes;
					vue.ajouteAuChat("(" + sheure + ":" + sminute + ":" + ssecondes + ") " + args[1] + " : " + args[2]);
				} else {
					vue.alert("Commande inconnue :" + cmd);

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<deplacement> getSolutionFinale() {
		return solutionFinale;
	}

	public void setSolutionFinale(ArrayList<deplacement> solutionFinale) {
		this.solutionFinale = solutionFinale;
	}

	public void setSolutionCourante(ArrayList<deplacement> solutionCourante) {
		this.solutionCourante = solutionCourante;
	}

	private ArrayList<deplacement> getSolutionCourante() {
		return solutionCourante;
	}

	private ArrayList<deplacement> convertSolution(String s) {
		ArrayList<deplacement> sos = new ArrayList<deplacement>();
		Couleur c = null;
		Side side = null;
		for (int i = 0; i < s.length(); i = i++) {
			if (i % 2 == 0) {
				switch (s.charAt(i)) {
				case 'R':
					c = Couleur.R;
				case 'B':
					c = Couleur.B;
				case 'V':
					c = Couleur.V;
				case 'J':
					c = Couleur.J;

				}

				sos.add(new deplacement(c));
			} else {
				switch (s.charAt(i)) {
				case 'H':
					side = Side.H;
				case 'B':
					side = Side.B;
				case 'D':
					side = Side.D;
				case 'G':
					side = Side.G;
				}
				sos.get(sos.size()).s = side;
			}
		}
		return sos;
	}

	private void initialisationEnigme(String senigme) {
		senigme = senigme.substring(1, senigme.length() - 1);
		String[] tab = senigme.split(",");
		Couleur cibleCouleur = null;
		int xr, yr, xj, yj, xb, yb, xv, yv, xc, yc;

		yr = Integer.parseInt(tab[0]);
		xr = Integer.parseInt(tab[1]);
		yj = Integer.parseInt(tab[2]);
		xj = Integer.parseInt(tab[3]);
		yb = Integer.parseInt(tab[4]);
		xb = Integer.parseInt(tab[5]);
		yv = Integer.parseInt(tab[6]);
		xv = Integer.parseInt(tab[7]);
		yc = Integer.parseInt(tab[8]);
		xc = Integer.parseInt(tab[9]);

		switch (tab[10]) {
		case "R":
			cibleCouleur = Couleur.R;
			break;
		case "B":
			cibleCouleur = Couleur.B;
			break;
		case "V":
			cibleCouleur = Couleur.V;
			break;
		case "J":
			cibleCouleur = Couleur.J;
			break;
		}

		setEnigme(new Enigme(xr, yr, xj, yj, xb, yb, xv, yv, xc, yc, cibleCouleur));
		vue.setListRobots(this.enigme.getRobots());
		vue.ciblecouleur = cibleCouleur;
		vue.xc = xc;
		vue.yc = yc;
		vue.setGrid();

	}

	private String convertBilan(String s) {
		// suppression de l'info du tour courant
		s = s.substring(2);
		String[] tab = s.split(",");
		String chaine = " Score du joueur " + tab[0] + " est : " + tab[1].charAt(0) + "\n";

		for (int i = 1; i < tab.length - 1; i++) {
			chaine = chaine + " Score du joueur " + tab[i].substring(3, tab[i].length()) + " est : "
					+ tab[i + 1].charAt(0) + "\n";

		}
		return chaine;
	}

	// initialisation du plateau pour un client
	private void initialisationPlateau(String pl) {

		listmurs = new ArrayList<Mur>();
		// i =0 or i = 1
		String tmp = pl.replaceAll("\\)\\(", "\\);\\(");
		String Tab[] = tmp.split(";");
		for (int i = 0; i < Tab.length; i = i + 1) {
			String aux = Tab[i].substring(1, Tab[i].length() - 1);
			String auxTab[] = aux.split(",");
			int y = Integer.parseInt(auxTab[0]);
			int x = Integer.parseInt(auxTab[1]);
			Side s = null;
			switch (auxTab[2]) {
			case "H":
				s = Side.H;
				break;
			case "B":
				s = Side.B;
				break;
			case "D":
				s = Side.D;
				break;
			case "G":
				s = Side.G;
				break;
			}
			listmurs.add(new Mur(x, y, s));
		}
		vue.setListMurs(listmurs);

	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String nom) {
		this.user = nom;

	}

	public Enigme getEnigme() {
		return enigme;
	}

	public ArrayList<Mur> getListmurs() {
		return listmurs;
	}

	public void setEnigme(Enigme enigme) {
		this.enigme = enigme;
	}

	public Component getRRFrame() {
		return this.vue;
	}

}
