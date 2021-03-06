package test;

import util.DataSaver;
import hexapode.Deplacement;

/**
 * Classe abstraite � h�riter pour coder les tests.
 * @author Stud
 * @author pf
 *
 */

public abstract class Test {
	
	protected long nbIteration;				//Nombre de tests � executer
	protected double consecutiveLearnTime;	//Temps de test entre chaque pause en seconde
	protected double pauseTime;				//Temps d'arr�t par pause en seconde
	protected Deplacement deplacement;
	protected MarkovNCoups markov;
	protected boolean restartMarkov;
	protected boolean validation;
	protected String etat_actuel;
	protected String etat_suivant;
	protected int note;
	
	//variables utilis�es pour la sauvegarde
	protected int result;
	
	public Test(Deplacement deplacement, long nbIteration, double consecutiveLearnTime, double pauseTime, boolean restartMarkov)
	{
		this.deplacement = deplacement;
		this.nbIteration = nbIteration;
		this.consecutiveLearnTime = consecutiveLearnTime;
		this.pauseTime = pauseTime;
		this.restartMarkov = restartMarkov;
		validation = false;
	}
	
	public Test(Deplacement deplacement)
	{
	    this.deplacement = deplacement;
        nbIteration = 500;
        consecutiveLearnTime = 60;
        pauseTime = 10;
        restartMarkov = false;
	    validation = true;
	}

	public abstract void onStart();		//Au d�part de chaque test
	public abstract void onBreak();		//Pendant la pause
	public abstract void proceedTest();	//Lancement de chaque test
	public abstract void validTest();	//Routine de validation des tests (sert � refaire les tests sans apprentissage, pour valider les r�sultats)
	public abstract void init();		//Au lancement de tous les tests
	public abstract void updateNote();  //Met à jour la note après proceedTest
	
	/**
	 * Méthode appelée quelque fois. Debug purpose.
	 */
	public boolean sometimes()
	{
	    System.out.println("Pourcentage de convergence: "+100*markov.getConvergence());
	    boolean out = markov.getConvergence() == 1f;
	    markov.razConvergence();
	    return out;
	}
	
	public void onExit()				//A la fin de chaque test
	{
        updateNote();
        if(!validation)
            markov.updateMatrix(note, markov.string2index(etat_actuel), markov.string2index(etat_suivant));
//		if(!validation)
//			sauvegarde_matrice(false);
		//DataSaver.sauvegarder_test(etat_suivant, result);
	}

	public void terminate()				//Fin de tous les tests
	{
		if(!validation)
		{
			sauvegarde_matrice(true);		
//			System.out.println(markov.toString());
		}
	}
	
	public long getNbIteration()			//Nombre d'it�rations � effectuer
	{
		return nbIteration;
	}

	public boolean isValidation()		//En routine de validation ou non
	{
		return validation;
	}
	
	public double getConsecutiveLearnTime()	//Temps depuis la derni�re pause
	{
		return consecutiveLearnTime;
	}

	public double getPauseTime()
	{
		return pauseTime;
	}
	
	protected void sauvegarde_matrice(boolean sauvegarde_intermediaire)
	{
		DataSaver.sauvegarder_matrice(markov, sauvegarde_intermediaire);
	}
	
	protected MarkovNCoups chargement_matrice()
	{
		return chargement_matrice("markov.dat");
	}
	
	protected MarkovNCoups chargement_matrice(String filename)
	{
		return MarkovNCoups.charger_matrice(filename);
	}
}
