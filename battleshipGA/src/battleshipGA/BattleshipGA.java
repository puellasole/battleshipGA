package battleshipGA;

import java.util.*;

public class BattleshipGA {
    // Размер поля
    private static final int BOARD_SIZE = 8;
    // Корабли: количество и их размеры
    private static final int[] SHIP_SIZES = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    // Параметры генетического алгоритма
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;
    private static final double ELITISM_RATE = 0.1;

    // Представление особи (расстановки кораблей)
    static class Individual {
        List<Ship> ships = new ArrayList<>();
        int fitness;

        Individual() {
            // Случайная инициализация
            for (int size : SHIP_SIZES) {
                ships.add(createRandomShip(size));
            }
            calculateFitness();
        }

        Individual(List<Ship> ships) {
            this.ships = new ArrayList<>(ships);
            calculateFitness();
        }

        // Создание случайного корабля
        private Ship createRandomShip(int size) {
            Random rand = new Random();
            boolean isHorizontal = rand.nextBoolean();
            int x, y;
            
            if (isHorizontal) {
            	//как заранее понимать эти ограничения?
            	//+1 это начало корабля, без него это была бы предыдущая клетка 
                x = rand.nextInt(BOARD_SIZE - size + 1);
                y = rand.nextInt(BOARD_SIZE);
            } else {
                x = rand.nextInt(BOARD_SIZE);
                y = rand.nextInt(BOARD_SIZE - size + 1);
            }
            
            return new Ship(x, y, size, isHorizontal);
        }

        // Вычисление приспособленности (меньше значение = лучше)
        void calculateFitness() {
            // Штраф за пересечения кораблей
            int overlapPenalty = calculateOverlaps() * 100;
            
            // Штраф за выход за границы
            int outOfBoundsPenalty = calculateOutOfBounds() * 50;
            
            // Штраф за соприкосновение кораблей
            int adjacencyPenalty = calculateAdjacentShips() * 10;
            
            this.fitness = overlapPenalty + outOfBoundsPenalty + adjacencyPenalty;
        }

        // Подсчет пересечений кораблей
        private int calculateOverlaps() {
            Set<String> occupied = new HashSet<>();
            int overlaps = 0;
            
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                	//если горизонтальный - инкрементируем по иксу
                	//вертикальный - по игреку
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    String key = x + "," + y;
                    
                    if (occupied.contains(key)) {
                        overlaps++;
                    }
                    occupied.add(key);
                }
            }
            return overlaps;
        }

        // Подсчет клеток за пределами доски
        private int calculateOutOfBounds() {
            int outOfBounds = 0;
            
            for (Ship ship : ships) {
            	//эту добавленную клетку надо потом отнять?
                int endX = ship.isHorizontal ? ship.x + ship.size - 1 : ship.x;
                int endY = ship.isHorizontal ? ship.y : ship.y + ship.size - 1;
                
                if (endX >= BOARD_SIZE || endY >= BOARD_SIZE) {
                	//почему штраф равен всему размеру а не части которая выходит
                	//выходящий корабль полностью невалиден + ускорение алгоритма
                	//алгоритм может терпеть частично валидные корабли?
                    outOfBounds += ship.size;
                }
            }
            return outOfBounds;
        }

        // Подсчет соприкосновений кораблей
        private int calculateAdjacentShips() {
            boolean[][] board = new boolean[BOARD_SIZE][BOARD_SIZE];
            int adjacent = 0;
            
            // Помечаем занятые клетки
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    //защита от ложных клеток и выхода за пределы массива
                    if (isWithinBoard(x,y)) {
                        board[x][y] = true;
                    }
                }
            }
            
            // Проверяем соседние клетки
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            
                            int nx = x + dx;
                            int ny = y + dy;
                            //для избежания проверки ложных клеток за пределами доски
                            if (isWithinBoard(nx,ny)) {
                                if (board[nx][ny] && !isPartOfShip(ship, nx, ny)) {
                                    adjacent++;
                                }
                            }
                        }
                    }
                }
            }
            return adjacent;
        }

        private boolean isPartOfShip(Ship ship, int x, int y) {
            if (ship.isHorizontal) {
                return y == ship.y && x >= ship.x && x < ship.x + ship.size;
            } else {
                return x == ship.x && y >= ship.y && y < ship.y + ship.size;
            }
        }
        
        private boolean isWithinBoard(int x, int y) {
        	return (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE);
        }

        // Мутация особи
        void mutate() {
            Random rand = new Random();
            
            for (int i = 0; i < ships.size(); i++) {
                if (rand.nextDouble() < MUTATION_RATE) {
                    // Заменяем случайный корабль на новый
                    ships.set(i, createRandomShip(ships.get(i).size));
                }
            }
            calculateFitness();
        }
        
        void printBoard() {
            // Создаем и заполняем доску
            char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
            for (char[] row : board) Arrays.fill(row, '.');  // Пустые клетки
            
            // Размещаем корабли (S - ship)
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    if (isWithinBoard(x,y)) {
                        board[x][y] = 'S';
                    }
                }
            }

            // Вывод заголовков столбцов (A-J)
            System.out.print("   ");
            for (char c = 'A'; c < 'A' + BOARD_SIZE; c++) {
                System.out.print(c + " ");
            }
            System.out.println();

            // Вывод доски с номерами строк
            for (int y = 0; y < BOARD_SIZE; y++) {
                System.out.printf("%2d ", y);  // Номер строки с выравниванием
                for (int x = 0; x < BOARD_SIZE; x++) {
                    System.out.print(board[x][y] + " ");
                }
                System.out.println();
            }
        }
    }

    // Класс для представления корабля
    static class Ship {
        int x, y; // Позиция носа корабля
        int size; // Размер корабля
        boolean isHorizontal; // Ориентация

        Ship(int x, int y, int size, boolean isHorizontal) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.isHorizontal = isHorizontal;
        }
    }

    public static void main(String[] args) {
        // Инициализация популяции
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Individual());
        }

        // Основной цикл генетического алгоритма
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // Сортировка по приспособленности
            population.sort(Comparator.comparingInt(ind -> ind.fitness));

            // Проверка на найденное решение
            if (population.get(0).fitness == 0) {
                System.out.println("Найдено решение в поколении " + generation);
                break;
            }

            // Отбор элитных особей
            List<Individual> newPopulation = new ArrayList<>();
            int eliteCount = (int)(POPULATION_SIZE * ELITISM_RATE);
            newPopulation.addAll(population.subList(0, eliteCount));

            // Заполнение популяции потомками
            while (newPopulation.size() < POPULATION_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);
                Individual child = crossover(parent1, parent2);
                child.mutate();
                newPopulation.add(child);
            }

            population = newPopulation;
        }

        // Вывод лучшего решения
        population.sort(Comparator.comparingInt(ind -> ind.fitness));
        Individual best = population.get(0);
        System.out.println("Лучшая расстановка (штраф: " + best.fitness + ")");
        best.printBoard();
    }

    // Турнирный отбор
    static Individual tournamentSelection(List<Individual> population) {
        Random rand = new Random();
        Individual best = null;
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            Individual candidate = population.get(rand.nextInt(population.size()));
            if (best == null || candidate.fitness < best.fitness) {
                best = candidate;
            }
        }
        return best;
    }

    static Individual crossover(Individual parent1, Individual parent2) {
        Random rand = new Random();
        List<Ship> childShips = new ArrayList<>();

        for (int i = 0; i < SHIP_SIZES.length; i++) {
            Ship p1Ship = parent1.ships.get(i);
            Ship p2Ship = parent2.ships.get(i);

            // Комбинируем параметры от двух родителей
            boolean isHorizontal = rand.nextBoolean() ? p1Ship.isHorizontal : p2Ship.isHorizontal;
            int x = rand.nextBoolean() ? p1Ship.x : p2Ship.x;
            int y = rand.nextBoolean() ? p1Ship.y : p2Ship.y;

            // Гарантируем, что корабль не выйдет за границы поля
            if (isHorizontal) {
                x = Math.min(x, BOARD_SIZE - p1Ship.size); // Корректируем X для горизонтальных
            } else {
                y = Math.min(y, BOARD_SIZE - p1Ship.size); // Корректируем Y для вертикальных
            }

            childShips.add(new Ship(x, y, p1Ship.size, isHorizontal));
        }

        return new Individual(childShips);
    }
}
