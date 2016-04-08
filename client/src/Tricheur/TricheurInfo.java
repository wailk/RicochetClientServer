package Tricheur;

import java.util.Random;

public class TricheurInfo {
	
	private static final Random rand = new Random(System.currentTimeMillis());

	private static final String[] noms = 
		{"zak","salmen","david","joan"};

	public static String getName() {
		return noms[rand.nextInt(noms.length)];	
	}

	
	public static final String[] BIENVENUE = {"salut", "hii", "hello"};

	public static final String[] OK = {"ok","d'accord","nice"};

	

	public static final String[] DEBUTTOUR = { "allez les mecs","Trop facile","c'est dur"};



	public static final String[] BIENJOUE = { "Bien joue","pas mal"};

	


	public static final String[] MALJOUE = {"Dommage","nice try",};



	public static final String[] SORT = {"bye bye","a plus","au revoir"};

	public static String getMessageBienvenue(){
		return BIENVENUE[rand.nextInt(BIENVENUE.length)];
	}


	public static String getMessageOK(){
		return OK[rand.nextInt(OK.length)];	
	}

	public static String getMessageDEBUTTOUR(){
		return DEBUTTOUR[rand.nextInt(DEBUTTOUR.length)];	
	}

	public static String getMessageBIENJOUE(){
		return BIENJOUE[rand.nextInt(BIENJOUE.length)];	
	}

	public static String getMessageBienTente() {
		return MALJOUE[rand.nextInt(MALJOUE.length)];	
	}

	public static String getMessageBye(){
		return SORT[rand.nextInt(SORT.length)];	
	}

}


