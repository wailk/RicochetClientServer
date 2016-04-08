package Starter;


import Client.ModelClient;
import ui.RRFrame;


public class Luncher {

	/**
	 * Methode main du client
	 * @param args La ligne de commande
	 */
	public static void main(String[] args){
		RRFrame fenetre = new RRFrame();
		fenetre.setVisible(true);
		final ModelClient modeleClient = new ModelClient(fenetre);


		fenetre.setmodelClient(modeleClient);
		modeleClient.connexion();
		modeleClient.receivefromserver();
	}
}