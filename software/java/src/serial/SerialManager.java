package serial;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Instancie toutes les s�ries, si on lui demande gentillement!
 * @author pierre
 *
 */
public class SerialManager 
{
	// Dépendances

	//Series a instancier
	public Serial serieAsservissement = null;

	//On stock les series dans une liste
	private Hashtable<String, Serial> series = new Hashtable<String, Serial>();

	//Pour chaque carte, on connait a l'avance son nom, son ping et son baudrate
	private SpecificationCard carteAsservissement = new SpecificationCard("serieAsservissement", 0, 38400);

	//On stock les cartes dans une liste
	private ArrayList <SpecificationCard> cards = new ArrayList <SpecificationCard>();

	//Liste pour stocker les series qui sont connectees au pc 
	private ArrayList<String> connectedSerial = new ArrayList<String>();

	//Liste pour stocker les baudrates des differentes serie

	private ArrayList<Integer> baudrate = new ArrayList<Integer>();

	/**
	 * Recuperation de toutes les cartes dans cards et des baudrates dans baudrate
	 */
	public SerialManager() throws SerialManagerException
	{

		cards.add(this.carteAsservissement);

		Iterator<SpecificationCard> e = cards.iterator();
		while (e.hasNext())
		{
			int baud = e.next().baudrate;
			if (!this.baudrate.contains(baud))
				this.baudrate.add(baud);
		}

		this.serieAsservissement = new Serial(this.carteAsservissement.name);

		this.series.put(this.carteAsservissement.name, this.serieAsservissement);

		checkSerial();
		createSerial();
	}

	/**
	 * Regarde toutes les series qui sont branchees dans /dev/ttyUSB*
	 */
	public  void checkSerial()
	{
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			this.connectedSerial.add(port.getName());
		}
	}
	/**
	 * Création des series (il faut au prealable faire un checkSerial())
	 */
	public void createSerial() throws SerialManagerException
	{
		int id = -1;
		//Liste des series deja attribues
		ArrayList<Integer> deja_attribues = new ArrayList<Integer>();
		String pings[] = new String[20];
		for (int baudrate : this.baudrate)
		{
			System.out.println("liste des pings pour le baudrate " + baudrate);

			for(int k = 0; k < this.connectedSerial.size(); k++)
			{
				if (!deja_attribues.contains(k))
				{
					//Creation d'une serie de test
					Serial serialTest = new Serial("carte de test de ping");

					serialTest.initialize(this.connectedSerial.get(k), baudrate);

					if (serialTest.ping() != null)
						id = Integer.parseInt(serialTest.ping());
					else 
					{
						serialTest.close();
						continue;
					}

					if(!isKnownPing(id))
					{
						serialTest.close();
						continue;
					}

					if (!goodBaudrate(baudrate, id))
					{
						serialTest.close();
						continue;
					}
					//On stock le port de la serie (connectedSerial) dans le tabeau à la case [id]
					pings[id] = this.connectedSerial.get(k);

					//Après les tests de pings sur la serie, on ferme la communication
					serialTest.close();

					deja_attribues.add(k);

					System.out.println(id + " sur: " + connectedSerial.get(k));

				}
			}
		}

		//Association de chaque serie a son port
		Iterator<SpecificationCard> e = cards.iterator();
		while (e.hasNext())
		{
			SpecificationCard serial = e.next();
			if(serial.id == 0 && pings[serial.id] != null)
			{
				this.serieAsservissement.initialize(pings[serial.id], serial.baudrate);
			}
			if (pings[serial.id] == null)
			{
				System.out.println("La carte " + serial.name + " n'est pas détectée");
				throw new SerialManagerException();
			}
		}
	}
	
	/**
	 * 
	 * @param baudrate
	 * @param id
	 * @return
	 */
	private boolean goodBaudrate(int baudrate, int id)
	{
		Iterator<SpecificationCard> e = cards.iterator();
		while(e.hasNext())
		{
			SpecificationCard serial = e.next();
			if((id == serial.id) && (baudrate == serial.baudrate))
				return true;
		}
		return false;
	}

	/**
	 * Permet de savoir si une carte a déjà été pingée, utilisé que par SerialManager
	 * @param id
	 * @return
	 */
	private boolean isKnownPing(int id)
	{
		Iterator<SpecificationCard> e = cards.iterator();
		while(e.hasNext())
		{
			if(id == e.next().id)
				return true;
		}
		return false;
	}

	/**
	 * Permet d'obtenir une série
	 * @param name
	 * 				Nom de la série
	 * @return
	 * 				L'instance de la série
	 */
	public Serial getSerial(String name)	throws SerialManagerException
	{
		if (this.series.containsKey(name))
		{
			return this.series.get(name);
		}
		else
		{
			throw new SerialManagerException("serie non trouvée");
		}
	}
}
