package ru.edu.pr01;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Проверки OrderCalculator.
 * Без аргументов: unit-проверки + data-driven; "csv": только data-driven (data/sample.csv).
 */
public class SelfCheck {

    private static final String CSV_PATH = "data/sample.csv";
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "all";
        if (!mode.equals("csv")) {
            unitChecks();
        }
        dataDriven();
        System.out.println("Итого: PASS " + passed + ", FAIL " + failed);
    }

    // ---------- Unit-проверки ----------

    private static void unitChecks() {
        try {
            checkFormulas();
            checkVatAfterDiscount();
            checkQuantityBounds();
            checkPriceBounds();
            checkDiscountBounds();
            checkParse();
        } catch (RuntimeException e) {
            report("unexpected exception", false, e.getMessage());
        }
        System.out.println("Unit-проверок выполнено: " + (passed + failed));
    }

    private static void checkFormulas() {
        // типовое значение 10 × 1500.50, скидка 10 %
        expectMoney("calculateBase", 15005.00, OrderCalculator.calculateBase(10, 1500.50));
        expectMoney("calculateDiscountAmount", 1500.50, OrderCalculator.calculateDiscountAmount(15005.00, 10));
        expectMoney("applyDiscount", 13504.50, OrderCalculator.applyDiscount(15005.00, 10));
        expectMoney("calculateVat 25 %", 3376.13, OrderCalculator.calculateVat(13504.50, OrderCalculator.VAT_RATE));
        expectMoney("calculateTotal", 16880.63, totalFor(10, 1500.50, 10));
        expectMoney("скидка 0 % не меняет сумму", 1000.00, OrderCalculator.applyDiscount(1000.00, 0));
    }

    private static void checkVatAfterDiscount() {
        // НДС считается от суммы после скидки: 1000 − 20 % = 800, НДС 200, а не 250
        double after = OrderCalculator.applyDiscount(1000.00, 20);
        expectMoney("НДС после скидки", 200.00, OrderCalculator.calculateVat(after, OrderCalculator.VAT_RATE));
        expectMoney("итог 1 × 1000, скидка 20 %", 1000.00, totalFor(1, 1000.00, 20));
    }

    private static void checkQuantityBounds() {
        expectValid("количество 0 – исключено", false, 0, 100, 5);
        expectValid("количество 1 – включено", true, 1, 100, 5);
        expectValid("количество 10000 – включено", true, 10_000, 100, 5);
        expectValid("количество 10001 – исключено", false, 10_001, 100, 5);
    }

    private static void checkPriceBounds() {
        expectValid("цена 0 – исключена", false, 1, 0, 5);
        expectValid("цена 0.01 – включена", true, 1, 0.01, 5);
        expectValid("цена 5000000 – включена", true, 1, 5_000_000, 5);
        expectValid("цена 5000000.01 – исключена", false, 1, 5_000_000.01, 5);
        expectValid("цена NaN – исключена", false, 1, Double.NaN, 5);
    }

    private static void checkDiscountBounds() {
        expectValid("скидка -0.01 – исключена", false, 1, 100, -0.01);
        expectValid("скидка 0 – включена", true, 1, 100, 0);
        expectValid("скидка 30 – включена", true, 1, 100, 30);
        expectValid("скидка 30.01 – исключена", false, 1, 100, 30.01);
    }

    private static void checkParse() {
        expectInt("parseQuantity типовое", 10, OrderCalculator.parseQuantity("10"));
        expectMoney("parseAmount запятая", 1500.50, OrderCalculator.parseAmount("1500,50", "цена"));
        expectError("parseQuantity пусто", "quantity", "", "значение не задано");
        expectError("parseQuantity null", "quantity", null, "значение не задано");
        expectError("parseQuantity abc", "quantity", "abc", "не является целым числом");
        expectError("parseQuantity дробное", "quantity", "5.5", "не является целым числом");
        expectError("parseQuantity два значения", "quantity", "10 20", "не является целым числом");
        expectError("parseAmount пусто", "amount", "  ", "значение не задано");
        expectError("parseAmount 5%", "amount", "5%", "не является числом");
        expectError("parseAmount 1e5", "amount", "1e5", "не является числом");
    }

    private static double totalFor(int quantity, double unitPrice, double discountPercent) {
        double base = OrderCalculator.calculateBase(quantity, unitPrice);
        double after = OrderCalculator.applyDiscount(base, discountPercent);
        double vat = OrderCalculator.calculateVat(after, OrderCalculator.VAT_RATE);
        return OrderCalculator.calculateTotal(after, vat);
    }

    private static void expectValid(String name, boolean expected, int q, double p, double d) {
        boolean actual = OrderCalculator.isValid(q, p, d);
        report(name, expected == actual, "ожидалось " + expected + ", получено " + actual);
    }

    private static void expectError(String name, String parser, String text, String fragment) {
        try {
            if (parser.equals("quantity")) {
                OrderCalculator.parseQuantity(text);
            } else {
                OrderCalculator.parseAmount(text, "цена");
            }
            report(name, false, "исключение не выброшено");
        } catch (IllegalArgumentException e) {
            report(name, e.getMessage().contains(fragment), e.getMessage());
        }
    }

    private static void expectInt(String name, int expected, int actual) {
        report(name, expected == actual, "ожидалось " + expected + ", получено " + actual);
    }

    private static void expectMoney(String name, double expected, double actual) {
        String e = money(expected);
        String a = money(actual);
        report(name, e.equals(a), "ожидалось " + e + ", получено " + a);
    }

    private static String money(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static void report(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name + ": " + detail);
        }
    }

    // ---------- Data-driven (data/sample.csv) ----------

    private static void dataDriven() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(CSV_PATH));
        } catch (IOException e) {
            System.out.println("FAIL data: не удалось прочитать " + CSV_PATH);
            failed++;
            return;
        }
        int n = 0;
        int ok = 0;
        for (String row : lines) {
            if (row.isEmpty() || row.startsWith("#") || row.startsWith("quantity")) {
                continue;
            }
            n++;
            String problem = checkRow(row);
            if (problem == null) {
                ok++;
                passed++;
                System.out.println("PASS data/" + n + " (" + row + ")");
            } else {
                failed++;
                System.out.println("FAIL data/" + n + " (" + row + "): " + problem);
            }
        }
        System.out.println("Data-driven: " + ok + "/" + n + " passed");
    }

    /** Строка: quantity;price;discount;expected, где expected – итог или ERROR:фрагмент. */
    private static String checkRow(String row) {
        String[] cols = row.split(";", -1);
        if (cols.length != 4) {
            return "ожидалось 4 колонки, получено " + cols.length;
        }
        String expected = cols[3].trim();
        try {
            int q = OrderCalculator.parseQuantity(cols[0]);
            double p = OrderCalculator.parseAmount(cols[1], "цена");
            double d = OrderCalculator.parseAmount(cols[2], "скидка");
            String error = OrderCalculator.validate(q, p, d);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            String actual = money(totalFor(q, p, d));
            if (expected.startsWith("ERROR:")) {
                return "ожидалась ошибка, получен итог " + actual;
            }
            return actual.equals(expected) ? null : "ожидалось " + expected + ", получено " + actual;
        } catch (IllegalArgumentException e) {
            if (!expected.startsWith("ERROR:")) {
                return "неожиданная ошибка: " + e.getMessage();
            }
            return e.getMessage().contains(expected.substring(6)) ? null
                    : "ожидалось «" + expected.substring(6) + "», сообщение: " + e.getMessage();
        }
    }
}
