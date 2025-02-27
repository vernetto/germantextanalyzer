
package com.example.germantextanalyzer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TextAnalyzerController {

    private final POSModel posModel;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public TextAnalyzerController() throws Exception {
        try (var modelStream = getClass().getResourceAsStream("/opennlp-de-ud-gsd-pos-1.2-2.5.0.bin")) {
            posModel = new POSModel(modelStream);
        }
    }

    @GetMapping("/")
    public String showForm() {
        return "form";
    }

    @PostMapping("/analyze")
    public String analyzeText(@RequestParam("text") String text, Model model) {
        String analyzedText = analyzeLine(text);
        model.addAttribute("analyzedText", analyzedText);
        return "result";
    }

    private String analyzeLine(String line) {
        POSTaggerME tagger = new POSTaggerME(posModel);
        String[] tokens = tokenizer.tokenize(line);
        String[] tags = tagger.tag(tokens);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String tag = tags[i];
            String grammarTag = determineGrammarTag(tokens[i], tag);
            String color = getColorForCase(grammarTag);
            result.append("<span style='color:").append(color).append("'>").append(tokens[i]).append("</span> ");
        }
        return result.toString();
    }

    private String determineGrammarTag(String word, String posTag) {
        if (posTag.startsWith("NN")) {
            return "Nominative";
        } else if (posTag.startsWith("ADJ")) {
            return "Accusative";
        }
        return "Dative";
    }

    private String getColorForCase(String grammarCase) {
        return switch (grammarCase) {
            case "Nominative" -> "green";
            case "Accusative" -> "yellow";
            case "Dative" -> "red";
            case "Genitive" -> "blue";
            default -> "black";
        };
    }
}
