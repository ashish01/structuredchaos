java -cp berkeleyParser.jar edu.berkeley.nlp.PCFGLA.GrammarTrainer -path netflix_easy.sen -out netflix_easy2.gr -filter 1e-20 -SMcycles 2 -treebank SINGLEFILE
java -cp berkeleyParser.jar edu.berkeley.nlp.PCFGLA.GrammarTrainer -path netflix_easy.sen -out netflix_easy3.gr -filter 1e-20 -SMcycles 3 -treebank SINGLEFILE
java -cp berkeleyParser.jar edu.berkeley.nlp.PCFGLA.GrammarTrainer -path netflix_easy.sen -out netflix_easy4.gr -filter 1e-20 -SMcycles 4 -treebank SINGLEFILE
java -cp berkeleyParser.jar edu.berkeley.nlp.PCFGLA.GrammarTrainer -path netflix_easy.sen -out netflix_easy5.gr -filter 1e-20 -SMcycles 5 -treebank SINGLEFILE

