# Практическая работа №3-4. Типы данных, арифметика и корректный расчёт показателей

Ахметгалиев Арслан Альбертович, ЭФБО-17-25. Вариант 4: ставка НДС 25 %.

Программа рассчитывает базовую стоимость заказа, скидку, стоимость после скидки, НДС и итог
в рублях с двумя знаками после запятой.

## Правила

- Количество — целое, от 1 до 10 000.
- Цена единицы — больше 0 и не более 5 000 000 руб.
- Скидка — от 0 до 30 %.
- НДС 25 % начисляется после скидки.
- При ошибке выводится сообщение, расчёт не выполняется.

## Структура

```
src/ru/edu/pr01/OrderCalculator.java   расчёт, проверки, ввод-вывод
src/ru/edu/pr01/SelfCheck.java         unit-проверки и прогон data/sample.csv
data/sample.csv                        quantity;price;discount;expected
diagrams/                              activity и class (.dot, .png)
DECISIONS.md                           принятые решения
```

## Сборка и запуск

```
javac -encoding UTF-8 -d out src/ru/edu/pr01/*.java
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr01.OrderCalculator
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr01.SelfCheck
java -Dstdout.encoding=UTF-8 -cp out ru.edu.pr01.SelfCheck csv
```

`SelfCheck` запускается из корня проекта (путь к `data/sample.csv` относительный).

Пример ввода: `10`, `1500.50`, `10` → итого к оплате 16880.63 руб.
