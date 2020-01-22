package com.sentienhq.zeno.dataprovider.simpleprovider;

import androidx.annotation.VisibleForTesting;

import com.sentienhq.zeno.pojo.SearchPojo;
import com.sentienhq.zeno.searcher.Searcher;
import com.sentienhq.zeno.utils.calculator.Calculator;
import com.sentienhq.zeno.utils.calculator.Result;
import com.sentienhq.zeno.utils.calculator.ShuntingYard;
import com.sentienhq.zeno.utils.calculator.Tokenizer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorProvider extends SimpleProvider {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    final Pattern P;
    private final NumberFormat LOCALIZED_NUMBER_FORMATTER = NumberFormat.getInstance();

    public CalculatorProvider() {
        //This should try to match as much as possible without going out of the expression,
        //even if the expression is not actually a computable operation.
        P = Pattern.compile("^[\\-.,\\d+*/^'()]+$");
    }

    @Override
    public void requestResults(String query, Searcher searcher) {
        // Now create matcher object.
        Matcher m = P.matcher(query.replaceAll("\\s+", ""));
        if (m.find()) {
            String operation = m.group();

            Result<ArrayDeque<Tokenizer.Token>> tokenized = Tokenizer.tokenize(operation);
            String readableResult;

            if (tokenized.syntacticalError) {
                return;
            } else if (tokenized.arithmeticalError) {
                return;
            } else {
                Result<ArrayDeque<Tokenizer.Token>> posfixed = ShuntingYard.infixToPostfix(tokenized.result);

                if (posfixed.syntacticalError) {
                    return;
                } else if (posfixed.arithmeticalError) {
                    return;
                } else {
                    Result<BigDecimal> result = Calculator.calculateExpression(posfixed.result);

                    if (result.syntacticalError) {
                        return;
                    } else if (result.arithmeticalError) {
                        return;
                    } else {
                        String localizedNumber = LOCALIZED_NUMBER_FORMATTER.format(result.result);
                        readableResult = " = " + localizedNumber;
                    }
                }
            }

            String queryProcessed = operation + readableResult;
            SearchPojo pojo = new SearchPojo("calculator://", queryProcessed, "", SearchPojo.CALCULATOR_QUERY);

            pojo.relevance = 19;
            searcher.addResult(pojo);
        }
    }
}
