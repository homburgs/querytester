package com.hsofttec.intellij.querytester.utils;

import java.util.ArrayList;
import java.util.List;

public class NqlLiterals {
    public enum Category {KEYWORD, OPERATOR, CONSTANT, CONTEXT_VARIABLE, FUNCTION}

    public record Literal(String text, Category category) {
        public boolean isFunction() {
            return category == Category.FUNCTION;
        }
    }

    public static final List<Literal> LITERALS = new ArrayList<>();

    static {
        // --- Clauses / keywords (§1, §3, §13) ---
        kw("select");
        kw("where");
        kw("in");
        kw("between");
        kw("like");
        kw("is null");
        kw("is not null");
        kw("order by");
        kw("orderby");
        kw("desc");
        kw("asc");
        kw("scope");
        kw("scope onelevel");
        kw("scope subtree");
        kw("hidden");
        kw("count");
        kw("paging");
        kw("exists");
        kw("not exists");
        kw("and");
        kw("or");
        kw("not");
        kw("null");
        kw("distinct");
        kw("countDistinct");
        kw("hits");
        kw("tuples");

        // --- Operators (§3) ---
        op("=");
        op("!=");
        op(">");
        op(">=");
        op("<");
        op("<=");
        op("@=");
        op("~=");
        op("%=");
        op("%~");
        op("*=");
        op("soundexEquals");
        op("similarTo");
        op("equalsIgnoreCase");
        op("likeIgnoreCase");
        op("matches");

        // --- Context variables (§11) ---
        ctx("%currentUserPrincipalId");
        ctx("%currentDefaultPositionPrincipalId");
        ctx("%currentPositionPrincipalIds");
        ctx("%currentGroupPrincipalIds");
        ctx("%currentProxiedOrgEntityPrincipalIds");
        ctx("%currentHeadedOrgEntityPrincipalIds");
        ctx("%currentManagedOrgEntityPrincipalIds");
        ctx("%currentOwnedOrgEntityPrincipal");
        ctx("%actAsPositionIds");
        ctx("%agentPrincipalId");
        ctx("%currentWorkPositionPrincipalId");
        ctx("%today");
        ctx("%now");
        ctx("%todayAtMidnight");
        ctx("%tomorrowAtMidnight");
        ctx("%clientHost");
        ctx("%clientIPAddress");
        ctx("%clientApplication");
        ctx("%clientLocale");
        ctx("%clientToken");
        ctx("%similarityThreshold");
        ctx("%systemId");

        // --- Convenience & boolean constants (§10, §10.2) ---
        con("true");
        con("false");
        con("$folder");
        con("$document");
        con("$link");
        con("$annotation");
        con("$note");
        con("$rendition");
        con("$container");
        con("$addition");
        con("$indexed");
        con("$finalized");
        con("$toBeArchived");
        con("$archived");
        con("$error");
        con("$none");
        con("$logicallyDeleted");
        con("$automaticallyDeleted");
        con("$softlink");
        con("$hardlink");
        con("$xsap");
        con("$mail");
        con("$vcard");
        con("$calendar");
        con("$partition");
        con("$office");
        con("$lta");
        con("$file");
        con("$signature");
        con("$sapilm");
        con("$user");
        con("$position");
        con("$group");

        // --- Convert functions (§12.1) ---
        fn("toString");
        fn("str");
        fn("toInteger");
        fn("toLong");
        fn("toDouble");
        fn("toBoolean");
        fn("toDate");
        fn("toDatetime");
        fn("toBlob");
        fn("toAQI");
        fn("baseConvert");

        // --- Mathematical functions (§12.2) ---
        fn("plus");
        fn("sum");
        fn("minus");
        fn("product");
        fn("times");
        fn("quotient");
        fn("div");
        fn("modulo");
        fn("mod");
        fn("sqrt");
        fn("pow");
        fn("log");
        fn("floor");
        fn("ceil");
        fn("ceiling");
        fn("round");
        fn("abs");
        fn("signum");
        fn("sign");
        fn("sinus");
        fn("sin");
        fn("avg");

        // --- String functions (§12.3) ---
        fn("concat");
        fn("substring");
        fn("substr");
        fn("instring");
        fn("instr");
        fn("replace");
        fn("replaceChars");
        fn("trim");
        fn("ltrim");
        fn("rtrim");
        fn("toLower");
        fn("low");
        fn("toUpper");
        fn("up");
        fn("capitalize");
        fn("reverse");
        fn("replicate");
        fn("pad");
        fn("base64");
        fn("hash");
        fn("stringFormat");
        fn("soundex");
        fn("soundexDifference");
        fn("editDistance");
        fn("similarity");
        fn("colognePhonetic");

        // --- Date/Timestamp functions (§12.4) ---
        fn("year");
        fn("month");
        fn("day");
        fn("hour");
        fn("minute");
        fn("second");
        fn("dateExtract");
        fn("dateRound");
        fn("dateFormat");
        fn("localeDateFormat");
        fn("dateAdd");
        fn("dateDiff");

        // --- List functions (§12.5) ---
        fn("concatLists");
        fn("listEntry");
        fn("entry");
        fn("listIndex");
        fn("listLength");
        fn("sublist");
        fn("listEntries");
        fn("entries");
        fn("toFlat");
        fn("tokenize");
        fn("uniqueList");
        fn("sortList");
        fn("reduceList");
        fn("toList");

        // --- Other functions (§12.6) ---
        fn("nullIf");
        fn("coalesce");
        fn("length");
        fn("len");
        fn("max");
        fn("min");
        fn("case");
        fn("switch");
        fn("random");
        fn("rnd");
        fn("translate");
        fn("pidResolve");
        fn("groupResolve");
        fn("competenceResolve");
        fn("refResolve");
        fn("valueSetResolve");
        fn("keyGen");
        fn("bitAnd");
        fn("bitOr");
        fn("logicalAnd");
        fn("logicalOr");
        fn("logicalXOr");
        fn("isNumeric");
        fn("ascii");
        fn("char");
        fn("customFunction");
    }

    private static void kw(String text) {
        LITERALS.add(new Literal(text, Category.KEYWORD));
    }

    private static void op(String text) {
        LITERALS.add(new Literal(text, Category.OPERATOR));
    }

    private static void con(String text) {
        LITERALS.add(new Literal(text, Category.CONSTANT));
    }

    private static void ctx(String text) {
        LITERALS.add(new Literal(text, Category.CONTEXT_VARIABLE));
    }

    private static void fn(String text) {
        LITERALS.add(new Literal(text, Category.FUNCTION));
    }
}
