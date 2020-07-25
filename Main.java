import java.util.Random;
//import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

/*CLASSES AND INTERFACES DESCRIPTION:
 * Main: Contains the main function
 * ConsoleIO: Handles Input/output and the console interface
 * Player: Contains the implementation for player threads
 * Moderator: Contains the implementation for moderator thread
 * GameStats: Contains some flags, variables and helper functions which act as shared resources between players and moderator
 * 
 * Participant: Component Interface for the Decorator Design Pattern (Both the players and the moderator are taken as participants)
 * BasicParticipant: Component implementation for the Decorator Design Pattern  
 * ParticipantDecorator: Decorator Class for the Decorator Design Pattern  
 */

//Moderator Class has been implemented keeping both the Singleton and the Decorator Design Patterns in Mind
//Player Class has been implemented keeping the Decorator Design Pattern in Mind


public class Main {

	public static void main(String[] args) throws IOException{
	
		ConsoleIO.printMyIntro();
		int NUMBER_OF_PLAYERS = ConsoleIO.readNumberOfPlayers();
		boolean IS_DRAW_ALLOWED = ConsoleIO.readDrawAllowed();
		int NUMBERS_TO_MATCH = 10; //take any number between 1 to 10 as the numbers to match for being selected as the winner
		
		
		GameStats stats = new GameStats(NUMBER_OF_PLAYERS, IS_DRAW_ALLOWED, NUMBERS_TO_MATCH);
		
		//SINGLETON DESIGN PATTERN AND DECORATOR DESIGN PATTERN
		Participant mod = Moderator.getInstance(new BasicParticipant(),stats); 
		Participant[] playerList = new Participant[NUMBER_OF_PLAYERS];
		Thread[] playerThreads = new Thread[NUMBER_OF_PLAYERS];
		
		
		ConsoleIO.printHeader("Moderator: DECORATOR DESIGN PATTERN DEMO");
		ConsoleIO.printDetails(mod);
		
		ConsoleIO.printHeader("Player: DECORATOR DESIGN PATTERN DEMO");
		for(int i = 0; i < NUMBER_OF_PLAYERS; i++) {
			playerList[i] = new Player(new BasicParticipant(),stats, i);
			ConsoleIO.printDetails(playerList[i]);
		}
		
		//INITIALIZING PLAYER THREADS
		for(int i = 0; i < NUMBER_OF_PLAYERS; i++) {
			//DOWNCASTING DONE BECAUSE THE PLAYER CLASS IMPLEMENTS RUNNABLE
			playerThreads[i] = new Thread((Player)playerList[i]);
		}
		
		//INITIALIZING MODERATOR THREAD
		//DOWNCASTING DONE BECAUSE THE MODERATOR CLASS IMPLEMENTS RUNNABLE
		Thread modThread = new Thread((Moderator)mod);
		
		ConsoleIO.printHeader("GAME STARTED");
		modThread.start();
		for(int i = 0; i < NUMBER_OF_PLAYERS; i++) {
			playerThreads[i].start();
		}	
	}
}

//-------------------------------CLASS FOR CONSOLE INTERFACE-------------------------------- 
class ConsoleIO{
	private static InputStreamReader isr = new InputStreamReader(System.in);
	private static BufferedReader br = new BufferedReader(isr);
	public ConsoleIO() {
		
	}
	public static int readInteger(String message) {
		int input = -1;
		try {
			System.out.print(message);
			input = Integer.parseInt(br.readLine());
		} catch(Exception e) {
			System.out.println(e);
		}
		return input;
	}
	public static String readString(String message) {
		String input = "";
		try {
			System.out.print(message);
			input = br.readLine();
		} catch(Exception e) {
			System.out.println(e);
		}
		return input;
	}
	public static int readNumberOfPlayers(){
		int NUMBER_OF_PLAYERS = -1;
		while(NUMBER_OF_PLAYERS <= 0) {
			NUMBER_OF_PLAYERS = ConsoleIO.readInteger("Enter the Number of Players: ");
		}
		return NUMBER_OF_PLAYERS;
	}
	public static boolean readDrawAllowed(){
		boolean ans = false;
		String input = "";
		input = ConsoleIO.readString("Do you want the game to allow multiple winners? (y/n) ");
		if(input.startsWith("y") || input.startsWith("Y")) {
			ans = true;
		}
		return ans;
	}
	public static void printMyIntro() {
		System.out.println("LAKSHYA KWATRA | 2017A3PS0365P | Object Oriented Programming\n" + 
				"\nCONCEPTS IMPLEMENTED:\n" + 
				"1. Multithreading\r\n" + 
				"2. Generics and collections\r\n" + 
				"3. Arrays\r\n" + 
				"4. Exception handling\r\n" + 
				"5. Input/Output\r\n" + 
				"6. Design patterns - SINGLETON and DECORATOR\n");
	}
	public static void printDetails(Participant p) {
		p.showDetails();
	}
	public static void printHeader(String s) {
		System.out.println("\n---------------"+s+"---------------\n");
	}
	public static void printFooter() {
		System.out.println("____________________________________________________");
	}
	public static void printNumberMatchedMessage(int id, int number, int totalMatches) {
		System.out.println("Player-"+ (id + 1) +" got "+ number +" matched. Total Matches: " + totalMatches);
	}
	public static void printWinner(int id) {
		System.out.println("\nPLAYER-"+ (id + 1) + " HAS WON THE GAME!");
	}

	// USING GENERIC METHOD FOR PRINTING A MESSAGE WITH VALUE
	public static <E> void printMessageWithValue(String message, E value) {
		try {
			System.out.println(message+ ": " + value);
		} catch(Exception e){
			System.out.println(message+ ": " + e);
		}
	}
	
}

//-----------------------------------DECORATOR DESIGN PATTERN IMPLEMENTATION----------------------------

//COMPONENT INTERFACE
interface Participant{
	public void showDetails();
}

//COMPONENT IMPLEMENTATION
class BasicParticipant implements Participant{
	public void showDetails() {
		System.out.println("Basic Participant");
	}
}

//DECORATOR CLASS
class ParticipantDecorator implements Participant{
	//MADE PROTECTED SO THAT THIS IS ACCESSIBLE TO THE CHILD DECORATOR CLASSES
	protected Participant participant; 
	
	public ParticipantDecorator(Participant p) {
		this.participant = p;
	}
	
	public void showDetails() {
		this.participant.showDetails();
	}
}


//-----------------------------------PLAYER-----------------------------------------
class Player extends ParticipantDecorator implements Runnable{
	private int id;
	private int numbersMatched;
	private GameStats stats;
	private final static int NOS_ON_TICKET = 10;
	private int[] ticket = new int[NOS_ON_TICKET];
	
	public Player(Participant p, GameStats stats, int id) {
		super(p);
		this.id = id; 			
		this.numbersMatched = 0;
		this.stats = stats;
		
		//GENERATE TICKET NUMBERS FOR THE PLAYER
		for(int i = 0; i < NOS_ON_TICKET; i++) {
			int r = GameStats.randomInt(i*5 + 1, (i+1) * 5);
			ticket[i] = r;
		}
	}
	
	
	public void run() {
		synchronized(stats.lock) {
			
			//LOOP UNTIL GAME IS NOT OVER
			while(!stats.gameOver) {
				
				//WAIT UNTIL THE NUMBER IS NOT ANNOUNCED OR THE CURRENT TURN OF THE PLAYER IS COMPLETE
				while(!stats.numberAnnouncedFlag || stats.checkedFlag[id]) {
					try {
						stats.lock.wait();
					} catch (InterruptedException e) {
						System.out.println(e);
					}
				}
				
				//IF GAME IS NOT OVER, THEN CHECK FOR THE ANNOUNCED NUMBER ON THE TICKET
				if(!stats.gameOver) {
					try {
						int numberToCheck = stats.announcedNumbers.get(stats.announcedNumbers.size() - 1);
						
						//CHECK WHETHER THE ANNOUNCED NUMBER HAS BEEN ANNOUNCED FOR THE FIRST TIME
						//IF YES, THEN CHECK FOR THE ANNOUNCED NUMBER ON THE TICKET
						if(stats.announcedNumbers.indexOf(numberToCheck) == stats.announcedNumbers.size() - 1) {
							for(int i = 0; i < NOS_ON_TICKET; i++) {
								
								if( numberToCheck == ticket[i]) {
									numbersMatched++;
									ConsoleIO.printNumberMatchedMessage(id, ticket[i], numbersMatched);
									break;
								}
							}
						}
					} catch(ArrayIndexOutOfBoundsException e){
						System.out.println(e);
					}
					
					//IF 3 OR MORE NUMBERS MATCH, SET VICTORY FLAG TO TRUE
					if(numbersMatched >= this.stats.numbersToMatch) {
						stats.victoryFlag[this.id] = true;
						if(stats.winnerId == -1) {
							stats.winnerId = this.id;
						}
					}
					
					stats.checkedFlag[id] = true;
					stats.lock.notifyAll();
				}	
			}
		}
	}
	
	//IMPLEMENTED AS A PART OF THE DECORATOR DESIGN PATTERN
	public void showDetails() {
		ConsoleIO.printMessageWithValue("Player-"+ (this.id+1) + "\nPlayer Ticket", Arrays.toString(ticket));
		ConsoleIO.printFooter();
	}
}

//-----------------------------------MODERATOR-----------------------------------------
class Moderator extends ParticipantDecorator implements Runnable{
	private int numberAnnounced; 
	private GameStats stats;
	private int TOTAL_NOS = 50;
	private int SLEEP_TIME = 500;
	
	
	//--------------------------SINGLETON DESIGN PATTERN IMPLEMENTATION-------------------------------
	private static Moderator uniqueInstance;
	private Moderator(Participant p,GameStats stats) {
		super(p);
		this.stats = stats;
	}
	public static synchronized Moderator getInstance(Participant p,GameStats stats) {
		if(uniqueInstance == null) {
			uniqueInstance = new Moderator(p,stats);
		}
		return uniqueInstance;
	}
	
	public void run() {
		synchronized(this.stats.lock){
			
			//LOOP UNTIL WINNER IS NOT ANNOUNCED
			while(this.stats.checkVictoryCondition()) {
				
				//SET NUMBER ANNOUNCED FLAG AND PLAYER CHECKED FLAGS TO FALSE
				stats.numberAnnouncedFlag = false;
				for(int i = 0; i < this.stats.numPlayers; i++) {
					this.stats.checkedFlag[i] = false;
				}
				
				//ANNOUNCE NUMBER AND SET THE NUMBER ANNOUNCED FLAG TO TRUE
				this.announceNumber();
				System.out.println("Moderator Generated: " + numberAnnounced);
				stats.numberAnnouncedFlag = true;
				
				//WAIT FOR SOME SLEEP TIME
				try {
		            Thread.sleep(SLEEP_TIME);
		        } catch (Exception e) {
		            System.out.println(e);
		        }
				
				//NOTIFY ALL OTHERS WAITING ON LOCK
				stats.lock.notifyAll();
				
				//WAIT UNTIL ALL THE PLAYERS HAVE CHECKED THE NUMBER AND ARE DONE WITH THEIR TURN
				while(this.stats.checkCheckedCondition()) {
					try {
						stats.lock.wait(); 
					} catch (InterruptedException e) {
						System.out.println(e);
					}
				}
			}
			
			//PRINT THE WINNER AND SET GAME OVER TO TRUE
			this.stats.printWinner();
			stats.gameOver = true;
			
			//NOTIFY ALL OTHERS WHO MIGHT STILL BE WAITING ON THE LOCK JUST IN CASE
			stats.lock.notifyAll();
		}
	}
	
	public void announceNumber() {
		numberAnnounced = GameStats.randomInt(1,TOTAL_NOS);
		stats.announcedNumbers.add(this.numberAnnounced);
	}
	
	
	//IMPLEMENTED AS A PART OF THE DECORATOR DESIGN PATTERN
	public void showDetails() {
		ConsoleIO.printMessageWithValue("Numbers Announced as of now", this.stats.announcedNumbers);
		ConsoleIO.printFooter();
	}
}

//-----------------------------------GAME STATS-----------------------------------------
//SHARED RESOURCE BETWEEN PLAYER AND MODERATOR THREADS
class GameStats{
	//using ArrayList from Collections
	public ArrayList<Integer> announcedNumbers = new ArrayList<Integer>(); 
	public int winnerId = -1;
	public boolean drawAllowed;
	public boolean gameOver = false;
	public boolean numberAnnouncedFlag = false;
	public boolean[] victoryFlag;
	public boolean[] checkedFlag;
	public int numPlayers;
	public int numbersToMatch = 3; // default value for numbers to match set to 3
	public Object lock = new Object();
	
	
	public GameStats(int numPlayers) {
		this.numPlayers = numPlayers;
		this.victoryFlag = new boolean[numPlayers];
		this.checkedFlag = new boolean[numPlayers];
		this.drawAllowed = false;
	}
	public GameStats(int numPlayers, boolean drawAllowed) {
		this.numPlayers = numPlayers;
		this.victoryFlag = new boolean[numPlayers];
		this.checkedFlag = new boolean[numPlayers];
		this.drawAllowed = drawAllowed;
	}
	public GameStats(int numPlayers, boolean drawAllowed, int numbersToMatch) {
		this.numPlayers = numPlayers;
		this.victoryFlag = new boolean[numPlayers];
		this.checkedFlag = new boolean[numPlayers];
		this.drawAllowed = drawAllowed;
		this.numbersToMatch = numbersToMatch;
	}
	
	//HELPER METHOD TO CHECK IF ANY WINNERS HAVE BEEN DECIDED
	public boolean checkVictoryCondition() {
		boolean ans = true ;
		for(int i = 0; i < this.numPlayers; i++) {
			ans = ans && !this.victoryFlag[i];
		}
		return ans;
	}
	
	// HELPER METHOD TO CHECK IF ANY PLAYER IS STILL LEFT TO COMPLETE HIS TURN
	public boolean checkCheckedCondition() {
		boolean ans = false ;
		for(int i = 0; i < this.numPlayers; i++) {
			ans = ans || !this.checkedFlag[i];
		}
		return ans;
	}
	
	// HELPER METHOD TO PRINT THE WINNER
	public void printWinner(){
		
		if(this.drawAllowed) {
			for(int i = 0; i < numPlayers; i++) {
				if(victoryFlag[i]) {
					ConsoleIO.printWinner(i);
				}
			}	
		}
		else {
			ConsoleIO.printWinner(this.winnerId);
		}
	}
	
	//METHOD TO GENERATE RANDOM NUMBERS
	public static int randomInt(int min, int max) {	
	    Random r = new Random();
	    int randNum = r.nextInt(max-min+1) + min;
	    return randNum;
	}
}