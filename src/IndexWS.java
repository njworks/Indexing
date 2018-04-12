/*
Indexing for Web Search
Function:
    Reads two web pages and extract text from them, which is passed through series of processing
    such as sentence splitting, tokenisation, normalisation, part-of-speech tagging, frequency
    occurrences, removal of stopwords, stemming and lemmatisation. After all this processing,
    text is output to csv file Search.csv

Every process output is stored in each-stages.txt file to see differences after each stages.

References:
    [1]:  Ranks.nl. Stopwords. [online]
          Available at: https://www.ranks.nl/stopwords [Accessed 11 Feb. 2018].
    [2]:  Aly, G. (2011). Tagging text with Stanford POS Tagger in Java Applications - Galal Aly's.
          [online] Galal Aly's.
          Available at: http://new.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/
          [Accessed 11 Feb. 2018].
    [3]:  Stackoverflow.com. (2014). How to split a string into sentences that are strings?. [online]
          Available at: https://stackoverflow.com/questions/25097687/how-to-split-a-string-into-sentences-that-are-strings
          [Accessed 10 Feb. 2018].

libraries:
    jsoup: For web scraping the websites.
    Stanford CoreNLP: For POS tagging (including the english tagger file)
                      and Morphology stemming and lemmatisation.
 */

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.BreakIterator;
import java.util.*;

public class IndexWS {

    public static ArrayList<String> urlTitle = new ArrayList<>();       //Holds webpage title
    public static ArrayList<String> storeAtext = new ArrayList<>();     //Holds a href text
    public static ArrayList<String> storeHeadings = new ArrayList<>();  //Holds headings
    public static ArrayList<String> storeStyle = new ArrayList<>();     //Holds bold words
    public static SortedSet<String> sortedHeading = new TreeSet<>();    //Holds NN and NNP headings
    public static SortedSet<String> sortedStyle = new TreeSet<>();      //Holds NN and NNP bold text

    /*
    readHTML: Reads and stores webpage text and other attributes such as headings,
     bold text and a tag text.
     */
    private static String readHTML(String url, PrintWriter write, int urlID) {
        System.out.println("------------Reading website " + url+"-------------------");
        Document doc;
        String text = null;
        try {
            doc = Jsoup.connect(url).get();
            urlTitle.add(doc.title());
            Elements links = doc.getElementsByTag("a");
            write.println("\n");
            write.println("------------Href text from HTML url " + url);
            for (Element link : links) {
                String linkText = link.text();
                storeAtext.add(linkText);
                write.println(linkText);
            }
            write.println("\n");

            Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
            write.println("------------Headings from HTML url " + url);
            for (Element head : headings) {
                String value = Jsoup.parse(String.valueOf(head)).text();
                storeHeadings.add(urlID + "," + value);
                write.println(value);
            }
            write.println("\n");

            Elements style = doc.getElementsByTag("b");
            write.println("------------Bold tags from HTML url " + url);
            for (Element head : style) {
                String value = Jsoup.parse(String.valueOf(head)).text();
                storeStyle.add(urlID + "," + value);
                write.println(value);
            }
            write.println("\n");

            text = doc.body().text();
            write.println("------------Retrieved text from HTML url " + url);
            write.println(text);
            write.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    /*
    process: Takes body text and cuts it to sentence, tokens and removes punctuations
    Reference: [3] Breaking text to sentence example from stackoverflow by lebolo
     */
    private static ArrayList<String> process(String text, PrintWriter write) {
        System.out.println("Pre-processing body text for use");
        String textHold;
        ArrayList<String> store = new ArrayList<>();
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        it.setText(text);
        write.println("\n");
        write.println("------------Breaking text to sentences for tokenisation------------");
        int start = it.first();
        for (int i = it.next(); i != BreakIterator.DONE; start = i, i = it.next()) {
            write.println(text.substring(start, i));
            StringTokenizer s = new StringTokenizer(text.substring(start, i));
            while (s.hasMoreTokens()) {
                textHold = s.nextToken();
                textHold = textHold.replace("(?:--|[\\[\\]{}()+/\\\\])", " ")
                        .replaceAll("[^a-zA-Z0-9 ]", " ")
                        .toLowerCase();
                String hold[] = textHold.split(" ");
                for (String j : hold) {
                    if (!j.equals("")) {
                        store.add(j);
                    }
                }
            }
        }
        write.println("\n");
        write.println("------------Tokenised words------------");
        for (int j = 0; j < store.size(); j++) {
            write.print(store.get(j) + ", ");
        }
        write.println("\n");
        return store;
    }

    /*
    stopwordProcess: Removes bold and a tag text in tokenised text. Afterwards removes stopwords
    from tokenised text.
    Reference: [1] the stopwords list was created using words from Ranks NL
     */
    private static ArrayList<String> stopwordProcess(ArrayList<String> store, PrintWriter write) {
        write.println("\n");
        write.println("------------Removing bold words in body text------------");
        for (int s = 0; s < storeStyle.size(); s++) {
            String[] styleWord = storeStyle.get(s).split(",");
            for (String word : styleWord) {
                if (store.contains(word.toLowerCase())) {
                    write.print(word.toLowerCase() + ", ");
                    store.remove(store.indexOf(word.toLowerCase()));
                }
                if (store.contains(word.toLowerCase())) {
                    write.print(word.toLowerCase() + ", ");
                    store.remove(store.indexOf(word.toLowerCase()));
                }
            }
        }
        write.println("\n");
		write.println("\n");
        write.println("------------Removing href text from body text------------");
        for (int k = 0; k < storeAtext.size(); k++) {
            String[] words = storeAtext.get(k).replaceAll("[^a-zA-Z0-9 ]", " ")
                    .split(" ");
            for (String each : words) {
                for (int g = 0; g < store.size(); g++) {
                    if (store.get(g).equals(each.toLowerCase())) {
                        write.print(store.get(g) + ", ");
                        store.remove(g);
                    }
                }
            }
        }
        write.println("\n");

        System.out.println("Reading stopwords.csv and removing stopwords");
        write.println("------------After removing stopwords------------");
        List<String> stopwordList = new ArrayList<String>();
        File file = new File("lib\\stopwords.csv");
        try {
            Scanner input = new Scanner(file);
            while (input.hasNext()) {
                stopwordList.add(input.next().toLowerCase());
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < store.size(); i++) {
            if (stopwordList.contains(store.get(i))) {
                store.remove(i);
            }
        }

        for (int y = 0; y < store.size(); y++) {
            if (stopwordList.contains(store.get(y))) {
                store.remove(y);
            }
        }
        for (int j = 0; j < store.size(); j++) {
            write.print(store.get(j) + ", ");
        }
        write.println("\n");
        return store;
    }

    /*
    keywords: Using frequency of word occurrence, any word with 1 to 10+ occurrence removed.
    POS tagging used to get useful text for heading, bold text and body text.
    Lemmatisation and stemming performed with body text after POS tag pass.
    Reference: [2] The POS tag example from Galal Aly used to tag text.
     */
    private static ArrayList<String> keywords(ArrayList<String> store, String url, PrintWriter write, MaxentTagger tagger) {
        System.out.println("Selecting keywords");
        HashMap<String, Integer> frequency = new HashMap<String, Integer>();
        for (int i = 0; i < store.size(); i++) {
            String word = store.get(i);
            if (frequency.containsKey(word)) {
                int temp = frequency.get(word);
                temp++;
                frequency.put(word, temp);
            } else {
                frequency.put(word, 1);
            }
        }


        store.clear();
        write.println("------------Remaining words after word frequency used to remove unnecessary words------------");
        for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
            if (entry.getValue() > 1) {
                if (entry.getValue() < 10) {
                    write.print(entry.getKey() + ", ");
                    store.add(entry.getKey());
                }
            }
        }
        write.println("\n");

        ArrayList<String> temp = new ArrayList<>();
        System.out.println("POS Tagging");
        Morphology mor = new Morphology();

        write.println("------------Choosing heading with NN and NNP tags------------");
        for (int j = 0; j < storeHeadings.size(); j++) {
            String[] removeID = storeHeadings.get(j).split(",");
            String[] split = storeHeadings.get(j).replaceAll("[^a-zA-Z0-9 ]", " ").split(" ");
            for (int b = 0; b < split.length; b++) {
                if (b == 0) {
                    continue;
                }
                if (tagger.tagString(split[b]).contains("NN") || tagger.tagString(split[b]).contains("NNP")) {
                    sortedHeading.add(removeID[0] + "," + split[b]);
                    write.print(split[b] + ", ");
                }
            }
        }
        write.println("\n");

        write.println("------------Choosing bold words with NN and NNP tags------------");
        for (int j = 0; j < storeStyle.size(); j++) {
            String[] split = storeStyle.get(j).replaceAll("[^a-zA-Z0-9 ]", " ").split(" ");
            String[] removeID = storeStyle.get(j).split(",");
            for (int b = 0; b < split.length; b++) {
                if (b == 0) {
                    continue;
                }
                if (tagger.tagString(split[b]).contains("NN") || tagger.tagString(split[b]).contains("NNP")) {
                    write.print(split[b] + ", ");
                    sortedStyle.add(removeID[0] + "," + split[b]);
                }
            }
        }
        write.println("\n");

        write.println("------------POS tagging and word lemmatization and stem------------");
        for (int i = 0; i < store.size(); i++) {
            String tagged = tagger.tagString(store.get(i));
            String split[] = tagged.split("_");
            if (split[1].contains("NN") || split[1].contains("NNS") || split[1].contains("NNP") ||
                    split[1].contains("NNPS")) {
                temp.add(url + "," + split[0] + "," + mor.lemma(split[0], split[1]) + "," + mor.stem(split[0]));
                write.println("Tag: " + tagger.tagString(store.get(i)) +
                        "  Word: " + split[0] + "  Lemma: " + mor.lemma(split[0], split[1]) +
                        "  Stem: " + mor.stem(split[0]));
            }
        }
        write.println("\n");
        System.out.println("Morphological analysis completed" + "\n");
        return temp;
    }

    /*
    writeCSV: Writes sorted headings, bold text and body text to Search.csv
     */
    private static void writeCSV(ArrayList<String> input, ArrayList<String> inputTwo, PrintWriter write, ArrayList<String> url) {
        System.out.println("Writing to CSV");
        try {
            FileWriter writer = new FileWriter("Search.csv");
            writer.append("ID Key,URL");
            writer.append("\n");
            for (int k = 0; k < url.size(); k++) {
                writer.append(k + "," + url.get(k));
                writer.append("\n");
            }
            writer.append("ID, Word, Lemma, Stem, Weight ");
            writer.append("\n");
            for (int i = 0; i < urlTitle.size(); i++) {
                writer.append(i + "," + urlTitle.get(i) + ",,,1");
                writer.append("\n");
            }

            for (String n : sortedHeading) {
                String[] splitted = n.split(",");
                writer.append(splitted[0] + "," + splitted[1] + ",,," + "0.8");
                writer.append("\n");
            }

            for (String n : sortedStyle) {
                String[] splitted = n.split(",");
                writer.append(splitted[0] + "," + splitted[1] + ",,," + "0.6");
                writer.append("\n");
            }

            for (String i : inputTwo) {
//                String[] splitted = i.split(",");
//                writer.append(splitted[0] + "," + splitted[1] + "," + splitted[2] + "," + "0.5");
                writer.append(i + "," + "0.5");
                writer.append("\n");
            }
            write.close();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ArrayList<String> store = new ArrayList<String>();
        ArrayList<String> finish = new ArrayList<>();
        ArrayList<String> finishTwo = new ArrayList<>();
        ArrayList<String> url = new ArrayList();
        url.add("http://irsg.bcs.org/ksjaward.php");
        url.add("http://csee.essex.ac.uk/staff/udo/index.html");
        Boolean run = true;
        System.out.println("Enter URL and add space before clicking enter");
        System.out.println("To finish adding URLs enter DONE");
        while (run) {
            System.out.println("Enter URL: (enter space first then enter)");
            Scanner scan = new Scanner(System.in);
            String urls = scan.nextLine();
            if (urls.toLowerCase().equals("done")) {
                run = false;
                break;
            }
            try {
                Document temp = Jsoup.connect(urls).get();
                url.add(urls);
            } catch (Exception e) {
                System.out.println("Invalid URL");
            }

        }

        try {
            PrintWriter writeText = new PrintWriter("each-stages.txt", "UTF-8");
            MaxentTagger tagger = new MaxentTagger("lib\\english-left3words-distsim.tagger");
            System.out.println();
            for (int i = 0; i < url.size(); i++) {
                String html = readHTML(url.get(i), writeText, i);
                store = process(html, writeText);
                store = stopwordProcess(store, writeText);
                finish = keywords(store, Integer.toString(i), writeText, tagger);
                for (int j = 0; j < finish.size(); j++) {
                    finishTwo.add(finish.get(j));
                }

                store.clear();
            }
            writeCSV(finish, finishTwo, writeText, url);
            System.out.println("Complete");
            System.out.println("Open Search.csv for indexed words or each-stages.txt for each " +
                    "process output");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}