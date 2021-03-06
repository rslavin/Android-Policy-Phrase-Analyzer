package edu.utsa.cs.sefm;

import edu.utsa.cs.sefm.mapping.APIMapper;
import edu.utsa.cs.sefm.utils.HTMLParser;
import edu.utsa.cs.sefm.utils.Text;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Driver {

    private static final boolean VERBOSE = false;
    private static String apiLogs = "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\1000policiesAPIS";
    private static String policyFiles = "C:\\Users\\James\\Dropbox\\android privacy\\analysis\\1000policies";
    private static String phraseFile = "phrases.txt";
    private static String synonymFile = "synonyms.txt";
    private static String associatorFile = "associators.txt";
    private static String googleDocFile = "googleDocs.csv";
    private static String outFile = "out.txt";

    private static Semaphore workerThreadCtr = new Semaphore(Runtime.getRuntime().availableProcessors(), false);

    public static void main(String args[]) {
        List<String> phrases;
        List<String> associators; // for example, such as, etc
        try {
            phrases = Text.getWordList(phraseFile);
            associators = Text.getWordList(associatorFile);
        } catch (IOException e) {
            System.err.println("File not found");
            e.printStackTrace();
            return;
        }

        doMappings(phrases, googleDocFile);
  /*      try {
            doAssociations(phrases, associators);
            //System.setOut(new PrintStream(new File("out.html")));
            //convertAssociatorOutputToHTML(phrases, associators, "C:\\Users\\Rocky\\Dropbox\\Research\\android privacy\\analysis\\associators_output.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }*/


    }

    private static void convertAssociatorOutputToHTML(List<String> phrases, List<String> associators, String file) throws IOException {
        String fileContents = new String(Files.readAllBytes(new File(file).toPath()));
        Text fileText = new Text(fileContents, associators);
        fileText.markAssociators("blue");
        for (String phrase : phrases)
            fileText.markWord(phrase, "green");
        System.out.println(fileText.toHTML());
    }

    /**
     * Extracts sentences from policies where a phrase appears followed by an associator (e.g.,
     * for example, such as, etc).
     * Each policy is parsed in its own thread.
     *
     * @param phrases
     * @param associators
     * @throws InterruptedException 
     */
    private static void doAssociations(List<String> phrases, List<String> associators) 
    		throws InterruptedException {
    	doAssociations(System.out, phrases, associators);
    }
    
	private static void doAssociations(PrintStream output, List<String> phrases, List<String> associators) 
			throws InterruptedException {
        List<String> associated = new ArrayList<>();
        File[] policies = new File(policyFiles).listFiles();
        int numFiles = policies.length;
        AssociationWorker[] threadPool = new AssociationWorker[numFiles];
        
        for (int i = 0; i < numFiles; i++) {
        	threadPool[i] = new Driver().new AssociationWorker(
        			policies[i], associators, phrases);
        	threadPool[i].start();
        }
        for (int i = 0; i < numFiles; i++) {
        	threadPool[i].join();
        	System.err.println("Joined thread " + (i + 1) + "/" + numFiles);
        }
        for (int i = 0; i < numFiles; i++) {
        	associated.addAll(threadPool[i].getResults());
        }
        // remove duplicates
		Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(associated);
        associated = new ArrayList<>(set);

        for (String sentence : associated)
            output.println(sentence);

    }
    
	/**
	 * A Thread which extracts sentences of interest (as described in doAssociations())
	 * from a single policy file.
	 * After running the thread, call getResults() for the ArrayList of results.
	 */
    class AssociationWorker extends Thread {

    	private File fileEntry;
    	private List<String> associators, phrases;
    	private ArrayList<String> results = new ArrayList<>();
    	
    	/**
    	 * 
    	 * @param fileEntry the policy file
    	 * @param associators a list of associator phrases ("like", "such as", etc.)
    	 * @param phrases a list of phrases associated with data being collected ("information", "udid", etc.)
    	 */
    	public AssociationWorker(File fileEntry, List<String> associators, List<String> phrases) {
    		super();
    		this.fileEntry = fileEntry;
    		this.associators = associators;
    		this.phrases = phrases;
    	}
    	
    	/**
    	 * Called when the thread starts: safely acquire/release the semaphore
    	 * and start working.
    	 */
		@Override
		public void run() {
			try {
				workerThreadCtr.acquire();
				work();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				workerThreadCtr.release();
			}
		}
    	
		/**
		 * Extracts the sentences; unchanged from original unthreaded version.
		 * @throws IOException if error reading policy file
		 */
		public void work() throws IOException {
            String fileContents = new String(Files.readAllBytes(fileEntry.toPath()));
            HTMLParser html = new HTMLParser(fileContents);
            fileContents = html.removeHTMLTags();
            Text text = new Text(fileContents, associators);

            // for each line, check if it contains a phrase from the list
            for (String phrase : phrases) {
                List<String> found = text.findAssociated(phrase);
                results.addAll(found);
            }
		}
		
		public ArrayList<String> getResults() {
			return results;
		}
    	
    }


    /**
     * Maps APIs to phrases and phrases to APIs. Outputs data in csv files.
     * @param phrases
     */
    private static void doMappings(List<String> phrases, String googleDocFile) {
        try {
            System.setOut(new PrintStream(new File(outFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            APIMapper mapper = new APIMapper(phrases, policyFiles, apiLogs, VERBOSE, parseSynonyms());
            if (googleDocFile != null)
                mapper.readClassDoc(googleDocFile);
            mapper.apiPhraseFrequency(VERBOSE);
            mapper.apiMap();
            mapper.apiMappingsToCSV("phraseToApis");
            mapper.phraseMappingsToCSV("apiToPhrases");
            System.out.println("\n" + mapper.getStats());
            //     mapper.equivalenceFinder(10, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a List of Lists of synonyms based on input file.
     *
     * @return
     * @throws IOException
     */
    private static ArrayList<ArrayList<String>> parseSynonyms() throws IOException {
        File file = new File(synonymFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<ArrayList<String>> synonymList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            ArrayList<String> synonyms = new ArrayList<>();
            synonyms.addAll(Arrays.asList(line.replace("\n", "").replace("\r", "").toLowerCase().split(",")));
            while (synonyms.remove(" ")) ;
            for (int i = 0; i < synonyms.size(); i++)
                synonyms.set(i, synonyms.get(i).trim());
            if (synonyms.size() > 1)
                synonymList.add(synonyms);
        }
        br.close();
        return synonymList;
    }



}
