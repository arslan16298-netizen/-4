package ru.edu.pr01;

import java.util.Locale;
import java.util.Scanner;

/**
 * Расчёт стоимости заказа: базовая сумма, скидка, НДС после скидки, итог.
 * Вариант 4: ставка НДС 25 %.
 */
public class OrderCalculator {

    static final int MIN_QUANTITY = 1;
    static final int MAX_QUANTITY = 10_000;
    static final double MAX_UNIT_PRICE = 5_000_000.0;
    static final double MIN_DISCOUNT = 0.0;
    static final double MAX_DISCOUNT = 30.0;
    static final double VAT_RATE = 25.0;

    private static final String QUANTITY_REGEX = "^[0-9]+$";
    private static final String AMOUNT_REGEX = "^-?[0-9]{1,9}([.,][0-9]{1,2})?$";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in, "UTF-8");
        try {
            int quantity = parseQuantity(readValue(in, "Количество единиц: "));
            double unitPrice = parseAmount(readValue(in, "Цена единицы, руб.: "), "цена");
            double discountPercent = parseAmount(readValue(in, "Скидка, %: "), "скидка");
            String error = validate(quantity, unitPrice, discountPercent);
            if (error != null) {
                System.out.println(error);
                return;
            }
            double base = calculateBase(quantity, unitPrice);
            double discountAmount = calculateDiscountAmount(base, discountPercent);
            double afterDiscount = applyDiscount(base, discountPercent);
            double vat = calculateVat(afterDiscount, VAT_RATE);
            double total = calculateTotal(afterDiscount, vat);
            printMoney("Базовая стоимость", base);
            printMoney("Скидка", discountAmount);
            printMoney("Стоимость после скидки", afterDiscount);
            printMoney("НДС " + (int) VAT_RATE + " %", vat);
            printMoney("Итого к оплате", total);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- Расчёт ----------

    public static double calculateBase(int quantity, double unitPrice) {
        return quantity * unitPrice;
    }

    public static double calculateDiscountAmount(double base, double discountPercent) {
        return base * discountPercent / 100.0;
    }

    public static double applyDiscount(double base, double discountPercent) {
        return base - calculateDiscountAmount(base, discountPercent);
    }

    public static double calculateVat(double amountAfterDiscount, double vatRate) {
        return amountAfterDiscount * vatRate / 100.0;
    }

    public static double calculateTotal(double amountAfterDiscount, double vat) {
        return amountAfterDiscount + vat;
    }

    // ---------- Проверка диапазонов ----------

    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        return validate(quantity, unitPrice, discountPercent) == null;
    }

    /** Возвращает сообщение об ошибке или null, если все значения в допустимых диапазонах. */
    public static String validate(int quantity, double unitPrice, double discountPercent) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            return "Ошибка: количество – должно быть от " + MIN_QUANTITY + " до " + MAX_QUANTITY
                    + ", получено " + quantity;
        }
        if (!(unitPrice > 0) || unitPrice > MAX_UNIT_PRICE) {
            return "Ошибка: цена – должна быть больше 0 и не более " + (long) MAX_UNIT_PRICE
                    + " руб., получено " + format(unitPrice);
        }
        if (!(discountPercent >= MIN_DISCOUNT) || discountPercent > MAX_DISCOUNT) {
            return "Ошибка: скидка – должна быть от " + (int) MIN_DISCOUNT + " до " + (int) MAX_DISCOUNT
                    + " %, получено " + format(discountPercent);
        }
        return null;
    }

    // ---------- Разбор ввода ----------

    public static int parseQuantity(String text) {
        String value = requireValue(text, "количество");
        if (!value.matches(QUANTITY_REGEX)) {
            throw new IllegalArgumentException("Ошибка: количество – «" + value + "» не является целым числом");
        }
        if (value.length() > 9) {
            throw new IllegalArgumentException("Ошибка: количество – должно быть от " + MIN_QUANTITY
                    + " до " + MAX_QUANTITY + ", получено " + value);
        }
        return Integer.parseInt(value);
    }

    public static double parseAmount(String text, String field) {
        String value = requireValue(text, field);
        if (!value.matches(AMOUNT_REGEX)) {
            throw new IllegalArgumentException("Ошибка: " + field + " – «" + value
                    + "» не является числом (пример: 1500.50)");
        }
        return Double.parseDouble(value.replace(',', '.'));
    }

    private static String requireValue(String text, String field) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: " + field + " – значение не задано");
        }
        return text.trim();
    }

    private static String readValue(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : null;
    }

    // ---------- Вывод ----------

    private static void printMoney(String label, double amount) {
        System.out.printf(Locale.ROOT, "%s: %.2f руб.%n", label, amount);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
