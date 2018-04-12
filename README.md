# Indexing
The system takes HTML page URL as input and process the webpage and 
output an index of terms in the document.

# USE:
PC with Java installed.
Run the <b> IndexWS.java </b> file.

# PROGRAM: 
Asks user for URL of webpage to be web scrape and will contain asking until
"DONE" is entered. The text is retrieved from the webpage and is split down
to sentances. The sentances are tokenized and punctuations are removed. 
Afterwards stopwords are removed and depending on word frequency any word with
one or more than ten are removed. Then the remaining words are passed to POS
tagging and words which are NN (noun), NNS (noun plural), NNP (proper noun
singular) and NNPS (noun plural) are taken and are stemmed and lemmatized.
Finally these words are wrote into the Search.csv file.

# OUTPUT:
Search.csv: Contains the indexed words along with its document ID.
each-stages.txt: Shows each process in pipeline.

# LIBRARIES:
-Jsoup: For web scraping 
-Stanford CoreNLP: For Part-of-Speech Tagging and Morphology process in pipeline.
