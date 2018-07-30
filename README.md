# RestaurantSimulator
Projekt z Programowania Obiektowego (PUT, Semestr 3) 

Aplikacja symulująca proces zamawiania (w losowych odstępach czasu) posiłków w restauracji. Po stronie użytkownika leży proces dodawania klientów jak i dostawców (=pojazdów), możliwość wykonania zamówienia w imieniu klientów oraz zawrócenia dostawców do restauracji, a także usunięcia zarówno klientów jak i dostawców. Użytkownik może także zapisać aktualny stan symulatora lub odczytać ostatnio zapisany.

Bugi / braki:
-błędnie obliczany możliwy dystans do pokonania za jednym razem (paliwo czasem schodzi na wartości ujemne),
-po odczycie stanu symulatora, wszystkie pojazdy są "skierowane" w prawo, bez względu na kierunek ruchu (sytuacja wraca do normy przy pierwszym manewrze skrętu),
-brak wyświetlania szczegółów zamówień oraz możliwych posiłków do zamówienia.
