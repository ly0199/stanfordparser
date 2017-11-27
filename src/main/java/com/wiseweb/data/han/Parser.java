package com.wiseweb.data.han;

import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.*;
import edu.stanford.nlp.trees.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Parser {

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

    /**
     * @param parserModel 模型文件路径
     * @param sourceFile  源文件路径
     * @param outputFile  输出文件路径
     */
    public static void parser(String parserModel, String sourceFile, String outputFile) {

        // 加载模型
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

        // 进行处理
        demoDP(lp, sourceFile, outputFile);
        //demoAPI(lp);
    }


    /**
     * demoDP demonstrates turning a file into tokens and then parse
     * trees.  Note that the trees are printed by calling pennPrint on
     * the Tree object.  It is also possible to pass a PrintWriter to
     * pennPrint if you want to capture the output.
     * This code will work with any supported language.
     */
    public static void demoDP(LexicalizedParser lp, String filename, String outputFile) {

        // This option shows loading, sentence-segmenting and tokenizing a file using DocumentPreprocessor.
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        GrammaticalStructureFactory gsf = null;
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        // 构建读取 sourceFile 的 list，对每一行进行单独处理
        List<String> lines = readLines(filename);

        File outFile = new File(outputFile);

        // 循环每一行，单独进行分析
        for (String line : lines) {

            // You could also create a tokenizer here (as below) and pass it to DocumentPreprocessor
            Reader reader = new StringReader(line);
            for (List<HasWord> sentence : new DocumentPreprocessor(reader)) {
                Tree parse = lp.apply(sentence);
                parse.pennPrint();

                if (gsf != null) {
                    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                    Collection tdl = gs.typedDependencies();
                    //gs.typedDependenciesCCprocessed();
                    //System.out.println(tdl);
                    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
                    //System.out.println(tp.rootLabelOnlyFormat());
                    if(CollectionUtils.isEmpty(tdl)) continue;;

                    try {
                        FileUtils.writeStringToFile(outFile, tdl.toString()+"\n", UTF_8, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //tp.print(tdl, true, pw);
                }
            }
        }
    }

    /**
     * 读取文件的每一行数据
     *
     * @param filename
     * @return
     */
    private static List<String> readLines(String filename) {
        List<String> lines = Lists.newArrayList();
        try {
            File file = new File(filename);
            lines = FileUtils.readLines(file, UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
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

    private Parser() {
    } // static methods only

}
