package Tricheur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import ui.RRFrame;
import Structures.Enigme;
import Structures.Mur;
import Structures.deplacement;


public class Tricheur extends Thread{

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
	
	private Random rand;

	/** Solutions : une liste des deplacement */
	ArrayList<deplacement> solutionCourante ;
	ArrayList<deplacement> solutionFinale ;
	

	int Enchere ;

	public Tricheur() {
		rand = new Random(System.currentTimeMillis()); 

		host = "localhost";
		user = TricheurInfo.getName();
		System.out.println("Nom du tricheur "+user);
		port = 2016;

	}

	
	
	
	

//	public static void main(String[] args) {
//		if (args.length != 1) {
//			System.err.println("usage: java -jar bot.jar <host>");
//		}
//		else {
//			new Bot(args[0]);
//		}
//	}

	private void attendre(long secondes) {
		try {
			Thread.sleep(secondes*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void connexion() {
		try {
			sock = new Socket(host, port);

			InetAddress adresa = sock.getInetAddress();
			System.out.print("Connecting on : " + adresa.getHostAddress()
					+ " with hostname : " + adresa.getHostName() + "\n");

			outchan = new PrintWriter(sock.getOutputStream(), true);
			inchan = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			sendtoserver("CONNEXION/" + user+ "/");
		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Erreur de connexion au serveur !!! ");
			e.printStackTrace();
		}
	}
	
	// sending to server
		public void sendtoserver(String s) {
			s = s.trim();
			outchan.println(s);

		}
	

		
		public void receivefromserver() {

			String receive;
			try {
				while ((receive = inchan.readLine()) != null) {
					System.out.println("S -> C : " + receive);
					String cmd = receive.split("/")[0];
					String[] args = receive.split("/");

					if (cmd.equals("BIENVENUE")) {
						this.user = args[1];
						System.out.println("Bienvenue, " + user);
					} else if (cmd.equals("CONNECTE")) {
						if (Math.random() < 0.6) {
							attendre(2);
							String msg = TricheurInfo.getMessageBienvenue()+" "+user;
							sendtoserver("CHAT/" + user+ "/"+msg+"/");
						}
						if (Math.random() > 0.9) {
							attendre(2);
							String msg = TricheurInfo.getMessageBye()+" "+user;
							
							sendtoserver("CHAT/" + user+ "/"+msg+"/");
							attendre (1);
							sendtoserver("SORT/" + user+ "/"+msg+"/");
						}
						
						
						
						
						
					} else if (cmd.equals("DECONNEXION")) {
						System.out.println("Deconnexion de :  " + args[1]);
					} else if (cmd.equals("SESSION")) {
						System.out.println("Nouvelle session");
						sendtoserver("CHAT/"+user+"/"+"Bonne chance guys !");
					} else if (cmd.equals("VAINQUEUR")) {
						System.out.println(" Fin de la session courante");
						System.out.println("Scores finaux : "
								+ convertBilan(args[1]));
					} else if (cmd.equals("TOUR")) {
						
						System.out.println("Phase de reflexion");
						attendre(4);
						if (Math.random() < 0.4) {
							String msg = TricheurInfo.getMessageDEBUTTOUR();
							sendtoserver("SORT/" + user+ "/"+msg+"/");
						}
						
						
						System.out.println("Tour " + args[2].charAt(0)
								+ ". Scores acutuels : \n" + convertBilan(args[2]));
						
						// initialisation de la phase reflexion ??

					} else if (cmd.equals("TUASTROUVE")) {
					} else if (cmd.equals("ILATROUVE")) {
						System.out.println("Le joueur : " + args[1]
								+ " a trouvé avec " + args[2]
								+ " coups ! Fin de la phase reflexion");
					
						if (rand.nextDouble() < 0.15) {
							attendre(1);
						}
						double alea = rand.nextDouble();
						System.out.println("probabilité de l'enchere "+alea);
						
					} else if (cmd.equals("FINREFLEXION")) {
						
						System.out.println("Expiration du delai imparti a la reflexion, fin de la phase de reflexion !");
						
						double alea = rand.nextDouble();
						if (alea < 0.95) {
							attendre(2);
							sendtoserver("ENCHERE/"+user+"/29/");
							Enchere = 29;
						}
						if (Math.random() > 0.95) {
							String msg = TricheurInfo.getMessageDEBUTTOUR();
							sendtoserver("SORT/" + user+ "/"+msg+"/");
						}
						
					}
					// Phase d'enchere
					else if (cmd.equals("VALIDATION")) {
						System.out.println(" Phase d'enchere :votre enchere a été validé");
			
						
					} else if (cmd.equals("ECHEC")) {
						System.out.println("Phase d'enchere :Annulation votre enchere car incoherente avec celle de "
										+ args[1]);
	
					} else if (cmd.equals("NOUVELLEENCHERE")) {
						System.out.println(" Phase d'enchere :le joueur :"
								+ args[1] + " a mis une enchere de " + args[2]
								+ " coups");
						attendre(2);
						String msg = "Qui dit mieux !" ;
						sendtoserver("CHAT/" + user+ "/"+msg+"/");
					} else if (cmd.equals("FINENCHERE")) {
						System.out.println(" Phase d'enchere :Fin des encheres , le joueur actif est "
								+ args[1]
								+ " avec une enchere de "
								+ args[2]
								+ " coups !");
						System.out.println(args[1]);
						if (args[1].equals(user)){
							attendre(10);
							vue.popupSolutionResolution();
							String sol = genererSolution(Enchere);
							System.out.println("solution calculé= "+sol);
							sendtoserver("SOLUTION/" + user+ "/"+sol+"/");
					} }
					// Phase résolution
					else if (cmd.equals("SASOLUTION")) {
						System.out.println("Phase résolution : Solution proposé par "
								+ args[1] + " est " + args[2]);
						
					} else if (cmd.equals("BONNE")) {
						System.out.println("Phase résolution : Solution accepté , fin du tour ");
						sendtoserver("CHAT/" + user+ "/"+TricheurInfo.getMessageBIENJOUE()+"/");
					} else if (cmd.equals("MAUVAISE")) {
						System.out.println("Phase résolution : Solution refusee , passage au solution suivante de "
								+ args[1]);
						if (args[1].equals(user)){
							attendre(10);
							vue.popupSolutionResolution();
							String sol = genererSolution(Enchere);
							System.out.println("solution calculé= "+sol);
							sendtoserver("SOLUTION/" + user+ "/"+sol+"/");
							
						}else{
							attendre(2);
							sendtoserver("CHAT/" + user+ "/"+TricheurInfo.getMessageBIENJOUE()+"/");
						}
					} else if (cmd.equals("FINRESO")) {
						System.out.println("Phase résolution :Plus de joueurs restants, fin du tour");
					} else if (cmd.equals("TROPLONG")) {
						System.out.println("Phase résolution :Temps depasse, passage au solution suivante de "
								+ args[1]);
						if (args[1] == user){
							attendre(10);
							vue.popupSolutionResolution();
							String sol = genererSolution(Enchere);
							System.out.println("solution calculé= "+sol);
							sendtoserver("SOLUTION/" + user+ "/"+sol+"/");
						}
					} else if (cmd.equals("LISTEN")) {
						System.out.println("CHAT: "+args[1]+" : "+args[2]);
						} else {
						System.out.println("Commande inconnue ");

					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		private String convertBilan(String s) {
			// suppression de l'info du tour courant
			s = s.substring(2);
			String[] tab = s.split(",");
			String chaine = " Score du joueur "+tab[0]+" est : "+tab[1].charAt(0)+"\n";

			for (int i = 1; i < tab.length-1; i++) {
				chaine = chaine +" Score du joueur "
						+ tab[i].substring(3, tab[i].length()) + " est : "
						+ tab[i+1].charAt(0)+"\n";

			}
			return chaine;
		}


	

	private String genererSolution(int monEnchere) {
		Random rand = new Random(System.currentTimeMillis());
		String tmp = "";
		double alea; 
		for (int i = 0; i < monEnchere; i++) {
			alea = rand.nextDouble();
			if (alea < 0.25) {
				tmp += "R";
			}
			else if (alea < 0.5) {
				tmp += "B";
			}
			else if (alea < 0.75) {
				tmp += "J";
			}
			else {
				tmp += "V";
			}
			alea = rand.nextDouble();
			if (alea < 0.25) {
				tmp += "H";
			}
			else if (alea < 0.5) {
				tmp += "B";
			}
			else if (alea < 0.75) {
				tmp += "G";
			}
			else {
				tmp += "D";
			}
		}
		return tmp;
	}



}


