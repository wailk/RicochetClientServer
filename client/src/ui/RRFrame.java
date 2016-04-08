package ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import Structures.Couleur;
import Structures.Mur;
import Structures.Robot;
import Structures.Side;
import Client.ModelClient;
import Client.RRTimer;

public class RRFrame extends JFrame implements ChangeListener {

	private static final long serialVersionUID = 1L;
	/** JPanel contentPane = le contenu de la fenetre */
	private JPanel contentPane;
	/**
	 * modelClient modelClient = le modelCliente qui gère les comportements de
	 * l'itf
	 */
	private ModelClient modelClient;
	/** RRTimer rt = le thread chronomètre de la phase reflexion */
	public RRTimer rt;
	/** RRTimer et = le thread chronomètre de la phase enchere */
	public RRTimer et;
	/** RRTimer resot = le thread chronomètre de la phase resolution */
	public RRTimer resot;
	/** JPanel timer = affichage du timer */
	private JPanel timer;
	/** JLabel time = étiquette du timer */
	private JLabel time;
	/** JTextArea chatArea = la zone d'affichage du chat */
	private JTextArea chatArea;
	/** JTextArea serverArea = la zone d'affichage du serveur */
	private JTextArea serverArea;
	/** JTextField message = la zone ou l'utilisateur tape des msg */
	private JTextField message;
	private JTextField  messagecmd ;
	/** JGridPanel GridPanel = la zone de dessin */
	// public GridPanel GridPanel;
	/** JSplitPane splitPanelChat = le split pour écrire des messages */
	public JSplitPane splitPanelChat;
	/** JLabel perso = affichage privé */
	public JLabel perso;

	
	public ArrayList<Mur> Murs ;
	
	public	ArrayList<Robot> robots ;
	
	public int xc ,yc ;
	
	public Couleur ciblecouleur ;
	
	
	public JSplitPane splitPanelCmd ;
	// GridPanel GridPanel ;
	/**
	 * Constructeur
	 */
	public RRFrame() {
		perso = new JLabel("Attente de connexion de tous les clients...");
		modelClient = null;

		/*
		 * INITIALISATION
		 */
		setTitle("ROBOTS");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(300, 150, 1000, 700);
		//setResizable(false);

		/* initialisation du contentPane */
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		/*
		 * LE splitPanelChat EN BAS
		 */
		/* initialisation */
		splitPanelChat = new JSplitPane();
		splitPanelChat.setPreferredSize(new Dimension(50, 30));
		splitPanelChat.setBorder(BorderFactory.createEmptyBorder());
		contentPane.add(splitPanelChat, BorderLayout.SOUTH);
		
		
		

		/* la zone de texte pour le chat */
		message = new JTextField();
		splitPanelChat.setLeftComponent(message);
		message.setColumns(26);
		message.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = message.getText();
				if (!msg.equals(null) && !msg.equals("")) {
					modelClient.sendtoserver("CHAT/" + modelClient.getUser()
							+ "/" + msg + "/");
					message.setText("");
				}
			}
		});
		
	//	splitPanelCmd = new JSplitPane();
		
	    
		/* la zone de texte pour le cmd */
	//	messagecmd = new JTextField();
	//	messagecmd.setColumns(36);
	//	splitPanelCmd.setLeftComponent(messagecmd);
		//splitPanelChat.setRightComponent(splitPanelCmd);	
		/* la zone des boutons */
		JPanel boutonsSend = new JPanel();

//	* le bouton d'envoi par défaut : vers le chat (CHAT/message/) */
	JButton envoyerButton = new JButton("Envoyer");
		envoyerButton.setMnemonic(KeyEvent.VK_ENTER);
		envoyerButton.setPreferredSize(new Dimension(100, 30));

	envoyerButton.addActionListener(new ActionListener() {
		@Override
			public void actionPerformed(ActionEvent e) {
				String msg = message.getText();
				if (!msg.equals(null) && !msg.equals("")) {
					modelClient.sendtoserver("CHAT/" + modelClient.getUser()
							+ "/" + msg + "/");
					message.setText("");
				}
			}
		});

		/* ajout des boutons */
	boutonsSend.add(envoyerButton);
	splitPanelChat.setRightComponent(boutonsSend);
	splitPanelChat.getRightComponent().setMinimumSize(new Dimension(40, 40));
		
		
		timer = new JPanel();
		time = new JLabel("00:00");
		rt = new RRTimer(this.time);
		resot = new RRTimer(this.time);
		et = new RRTimer(this.time);
		timer.setBorder(BorderFactory.createTitledBorder("Temps restant"));
		timer.setPreferredSize(new Dimension(200, 50));
		timer.add(time);

		/*--------------------------------------------------------------------*/

		/*
		 * LE PANEL A GAUCHE
		 */
		/* initialisation */
		JPanel chats = new JPanel();
		chats.setPreferredSize(new Dimension(300, 50));

		/* la zone de texte qui vient du serveur */
		serverArea = new JTextArea();
		serverArea.setEditable(false);
		serverArea.setForeground(Color.red);
		serverArea.setBorder(BorderFactory.createTitledBorder("Serveur"));
		JScrollPane scrollPaneServer = new JScrollPane(serverArea);
		scrollPaneServer
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneServer.setPreferredSize(new Dimension(300, 250));
		scrollPaneServer.setBorder(BorderFactory.createEmptyBorder());
		/* la zone de texte pour le chat */
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setBorder(BorderFactory.createTitledBorder("Chat"));

		JScrollPane scrollPaneChat = new JScrollPane(chatArea);
		scrollPaneChat
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneChat.setPreferredSize(new Dimension(300, 200));
		scrollPaneChat.setBorder(BorderFactory.createEmptyBorder());

		/* ajout des zones de texte */
		chats.add(scrollPaneServer, BorderLayout.NORTH);
		chats.add(scrollPaneChat, BorderLayout.SOUTH);
		contentPane.add(chats, BorderLayout.WEST);
		/*
		 * 
		 */

		/*--------------------------------------------------------------------*/

		/*
		 * LE GridPanel AU MILIEU
		 */


		
		/**
		 * 
		 */

		/*
		 * LA BOITE A OUTILS EN HAUT
		 */
		/* initialisation */
		Box mybox = Box.createHorizontalBox();
		mybox.setName("tools");

	

		/* le panel des boutons option */
		JPanel boutons = new JPanel();
		boutons.setBorder(BorderFactory.createTitledBorder("Options"));

		/* le bouton de l'historique */
		JButton historique = new JButton("Historique");
		historique.setMaximumSize(new Dimension(150, 150));
		// to complete

		/* le bouton pour quitter */
		JButton exit = new JButton("Quitter");
		exit.setMaximumSize(new Dimension(150, 150));
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					modelClient.sendtoserver("SORT/" + modelClient.getUser()
							+ "/");
					dispose();
				} catch (Exception e) {
					dispose();
				}
			}
		});

		/* ajout des bouton option */
		boutons.add(historique);
		boutons.add(exit);

		/* ajout des outils dans la boite */
		mybox.add(boutons);
		mybox.add(timer);
		contentPane.add(mybox, BorderLayout.NORTH);
	}
	
	
	
	
	public void setGrid(){
		
		
		JPanel game = new GridPanel(Murs,this.robots,xc,yc,ciblecouleur);
		
		
		contentPane.add(game, BorderLayout.CENTER);
	}

	/**
	 * Methode qui modifie le temps de la phase reflexion
	 * 
	 * @param sec
	 *            Le nombre de secondes de la la phase reflexion
	 */
	public void setrtTime(int sec) {
		timer.setVisible(true);
		rt.setTime(sec);
		if (!rt.isAlive())
			rt.start();
	}

	/**
	 * Methode qui modifie le temps de la phase enchere
	 * 
	 * @param sec
	 *            Le nombre de secondes de la la phase enchere
	 */
	public void setetTime(int sec) {
		timer.setVisible(true);
		et.setTime(sec);
		if (!et.isAlive())
			et.start();
	}

	/**
	 * Methode qui modifie le temps de la phase enchere
	 * 
	 * @param sec
	 *            Le nombre de secondes de la la phase enchere
	 */
	public void setresotTime(int sec) {
		timer.setVisible(true);
		resot.setTime(sec);
		if (!resot.isAlive())
			resot.start();
	}

	/**
	 * Modifie le modelCliente
	 * 
	 * @param m
	 *            Le modelCliente
	 */
	public void setmodelClient(ModelClient m) {
		modelClient = m;
	}

	/**
	 * Recupere le modelCliente
	 * 
	 * @return modelClient Le modelCliente
	 */
	public ModelClient getmodelClient() {
		return modelClient;
	}

	/**
	 * Ajoute une ligne au chat
	 * 
	 * @param s
	 *            La phrase qu'on ajoute au chat
	 */
	public void ajouteAuChat(String s) {
		try {
			chatArea.getDocument().insertString(0, s + "\n", null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ajoute une ligne aux messages du serveur
	 * 
	 * @param s
	 *            La phrase qu'on ajoute a la fenêtre du serveur
	 */
	public void ajouteAuServeur(String s) {
		serverArea.append(s + "\n");
		serverArea.setCaretPosition(serverArea.getDocument().getLength());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {

	}

	public void popupEnchere() {
		String rep = JOptionPane.showInputDialog("Mettre enchere");

		if (rep != null) {
			if (et.isAlive()) {
				modelClient.sendtoserver("ENCHERE/" + modelClient.getUser()
						+ "/" + rep + "/");
				popupEnchere();
			} else
				JOptionPane.showMessageDialog(this, "trop tard !");
		}
	}

	public void popupSolutionReflexion() {
		String rep = JOptionPane
				.showInputDialog("Reflexion : Entrez le nombre de coup");

		if (rep != null) {

			if (rt.isAlive()) {
				rt.setTime(0);
				modelClient.sendtoserver("TROUVE/" + modelClient.getUser()
						+ "/" + rep + "/");
			} else
				JOptionPane.showMessageDialog(this, "trop tard !");

		}
	}

	public void popupSolutionResolution() {
		String rep = JOptionPane
				.showInputDialog("Resolution : Entrez les deplacements");

		if (rep != null) {
			if (resot.isAlive()) {
				resot.setTime(0);
				rep = rep.toUpperCase();
				if (isValidMoves(rep))
					modelClient.sendtoserver("SOLUTION/"
							+ modelClient.getUser() + "/" + rep + "/");
				else {
					JOptionPane.showMessageDialog(this,
							"Faute de frappe re-saisir les déplacement !");
					popupSolutionResolution();
				}
			} else
				JOptionPane.showMessageDialog(this, "trop tard !");
		}
	}

	private boolean isValidMoves(String rep) {
		Pattern p = Pattern.compile("^([RJVB][DGHB])+$") ;      
		Matcher m = p.matcher(rep) ;    
		  return m.matches() ;
		
	}

	public void popupConnexion() {
		String rep = JOptionPane.showInputDialog("Entrez votre nom");

		if (rep != null) {
			rep = rep.toUpperCase();
			modelClient.sendtoserver("CONNEXION/" + rep + "/");

		}
	}

	public void alert(String s) {
		JOptionPane.showMessageDialog(this, s);
	}
	// 
	public void nouvelleSession() {
		
		
	}
	/*  réinitialiser le timer du tour   */
	public void nouveauTour() {
		
		
		rt = new RRTimer(this.time);
		resot = new RRTimer(this.time);
		et = new RRTimer(this.time);
		
		
	}

	public void setListMurs(ArrayList<Mur> listmurs) {
		this.Murs =listmurs ;
		
	}

	public void setListRobots(ArrayList<Robot> robots) {
		this.robots = robots; 
		
	}
}