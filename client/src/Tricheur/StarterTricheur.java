package Tricheur;



public class StarterTricheur {
	
	public static void main(String[] args){
		
		//int status = 0;
		final Tricheur tricheur = new Tricheur();


		tricheur.connexion();
		tricheur.receivefromserver();
	}

}
