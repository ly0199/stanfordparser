package com.wiseweb.data.han;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.StringReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.*;


import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import org.apache.commons.io.FileUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

class ParserDemo {

    /**
     * The main method demonstrates the easiest way to load a parser.
     * Simply call loadModel and specify the path of a serialized grammar
     * model, which can be a file, a resource on the classpath, or even a URL.
     * For example, this demonstrates loading a grammar from the models jar
     * file, which you therefore need to include on the classpath for ParserDemo
     * to work.
     * <p>
     * Usage: {@code java ParserDemo [[model] textFile]}
     * e.g.: java ParserDemo edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz data/chinese-onesent-utf8.txt
     */
    public static void main(String[] args) {
        //String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        String parserModel = "E:/limengqi/chineseFactored.ser.gz";

        if (args.length > 0) {
            parserModel = args[0];
        }
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

        if (args.length == 0) {
            // demoDP(lp,"E:/limengqi/stanford-parser-full-2016-10-31/data/english-onesent.txt");
            demoDP(lp, "E:/bad_jieba.txt");
            // demoDP(lp,"D:/stanford-parser-full-2016-10-31/data/chinese-onesent-gb18030.txt");
            //demoDP(lp,"D:/stanfordparser/mouse_wheel_result.txt");
            //demoAPI(lp);
        } else {
            String textFile = (args.length > 1) ? args[1] : args[0];
            demoDP(lp, textFile);
        }
    }

    /**
     * demoDP demonstrates turning a file into tokens and then parse
     * trees.  Note that the trees are printed by calling pennPrint on
     * the Tree object.  It is also possible to pass a PrintWriter to
     * pennPrint if you want to capture the output.
     * This code will work with any supported language.
     */
    public static void demoDP(LexicalizedParser lp, String filename) {
        // This option shows loading, sentence-segmenting and tokenizing
        // a file using DocumentPreprocessor.
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = null;
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }
        // You could also create a tokenizer here (as below) and pass it
        // to DocumentPreprocessor

        List<String> lines = new ArrayList<String>();
        try {
            File file = new File(filename);
            lines = FileUtils.readLines(file, UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            Reader reader = new StringReader(line);
            int count = 0;
            for (List<HasWord> sentence : new DocumentPreprocessor(reader)) {
                System.out.println(count++);
                Tree parse = lp.apply(sentence);
                parse.pennPrint();
                // System.out.println();

                if (gsf != null) {
                    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                    Collection tdl = gs.typedDependencies();
                    //gs.typedDependenciesCCprocessed();
                    //System.out.println(tdl);
                    // System.out.println();
                    // System.out.println("11111111111");
                    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");

                    //System.out.println(tp.rootLabelOnlyFormat());
                    // System.out.println(tdl.toString());

                    FileWriter fw = null;
                    try {
                        //����ļ����ڣ���׷�����ݣ�����ļ������ڣ��򴴽��ļ�
                        File f = new File("E:/bad_jieba_outputChinese.txt");
                        fw = new FileWriter(f, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    PrintWriter pw = new PrintWriter(fw);
                    pw.println(tdl.toString());
                    //tp.print(tdl, true, pw);




                    pw.flush();
                    try {
                        fw.flush();
                        pw.close();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }


    }

    /**
     * demoAPI demonstrates other ways of calling the parser with
     * already tokenized text, or in some cases, raw text that needs to
     * be tokenized as a single sentence.  Output is handled with a
     * TreePrint object.  Note that the options used when creating the
     * TreePrint can determine what results to print out.  Once again,
     * one can capture the output by passing a PrintWriter to
     * TreePrint.printTree. This code is for English.
     */
    public static void demoAPI(LexicalizedParser lp) {
        // This option shows parsing a list of correctly tokenized words
        String[] sent = {"This", "is", "an", "easy", "sentence", "."};
        List<CoreLabel> rawWords = SentenceUtils.toCoreLabelList(sent);
        Tree parse = lp.apply(rawWords);
        parse.pennPrint();
        System.out.println();

        // This option shows loading and using an explicit tokenizer
        String sent2 = "Bell, a company which is based in LA, makes and distributes computer products.��,.";
        //String sent2 = "This is another sentence.";
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok =
                tokenizerFactory.getTokenizer(new StringReader(sent2));
        List<CoreLabel> rawWords2 = tok.tokenize();
        parse = lp.apply(rawWords2);

        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
        System.out.println();

        // You can also use a TreePrint object to print trees and dependencies

        TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
        tp.printTree(parse);
    }

    private ParserDemo() {
    } // static methods only

}
