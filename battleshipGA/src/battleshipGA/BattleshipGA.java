package battleshipGA;

import java.util.*;

public class BattleshipGA {
    // ������ ����
    private static final int BOARD_SIZE = 8;
    // �������: ���������� � �� �������
    private static final int[] SHIP_SIZES = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
    // ��������� ������������� ���������
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;
    private static final double ELITISM_RATE = 0.1;

    // ������������� ����� (����������� ��������)
    static class Individual {
        List<Ship> ships = new ArrayList<>();
        int fitness;

        Individual() {
            // ��������� �������������
            for (int size : SHIP_SIZES) {
                ships.add(createRandomShip(size));
            }
            calculateFitness();
        }

        Individual(List<Ship> ships) {
            this.ships = new ArrayList<>(ships);
            calculateFitness();
        }

        // �������� ���������� �������
        private Ship createRandomShip(int size) {
            Random rand = new Random();
            boolean isHorizontal = rand.nextBoolean();
            int x, y;
            
            if (isHorizontal) {
            	//��� ������� �������� ��� �����������?
            	//+1 ��� ������ �������, ��� ���� ��� ���� �� ���������� ������ 
                x = rand.nextInt(BOARD_SIZE - size + 1);
                y = rand.nextInt(BOARD_SIZE);
            } else {
                x = rand.nextInt(BOARD_SIZE);
                y = rand.nextInt(BOARD_SIZE - size + 1);
            }
            
            return new Ship(x, y, size, isHorizontal);
        }

        // ���������� ����������������� (������ �������� = �����)
        void calculateFitness() {
            // ����� �� ����������� ��������
            int overlapPenalty = calculateOverlaps() * 100;
            
            // ����� �� ����� �� �������
            int outOfBoundsPenalty = calculateOutOfBounds() * 50;
            
            // ����� �� ��������������� ��������
            int adjacencyPenalty = calculateAdjacentShips() * 10;
            
            this.fitness = overlapPenalty + outOfBoundsPenalty + adjacencyPenalty;
        }

        // ������� ����������� ��������
        private int calculateOverlaps() {
            Set<String> occupied = new HashSet<>();
            int overlaps = 0;
            
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                	//���� �������������� - �������������� �� ����
                	//������������ - �� ������
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

        // ������� ������ �� ��������� �����
        private int calculateOutOfBounds() {
            int outOfBounds = 0;
            
            for (Ship ship : ships) {
            	//��� ����������� ������ ���� ����� ������?
                int endX = ship.isHorizontal ? ship.x + ship.size - 1 : ship.x;
                int endY = ship.isHorizontal ? ship.y : ship.y + ship.size - 1;
                
                if (endX >= BOARD_SIZE || endY >= BOARD_SIZE) {
                	//������ ����� ����� ����� ������� � �� ����� ������� �������
                	//��������� ������� ��������� ��������� + ��������� ���������
                	//�������� ����� ������� �������� �������� �������?
                    outOfBounds += ship.size;
                }
            }
            return outOfBounds;
        }

        // ������� ��������������� ��������
        private int calculateAdjacentShips() {
            boolean[][] board = new boolean[BOARD_SIZE][BOARD_SIZE];
            int adjacent = 0;
            
            // �������� ������� ������
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    //������ �� ������ ������ � ������ �� ������� �������
                    if (isWithinBoard(x,y)) {
                        board[x][y] = true;
                    }
                }
            }
            
            // ��������� �������� ������
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            
                            int nx = x + dx;
                            int ny = y + dy;
                            //��� ��������� �������� ������ ������ �� ��������� �����
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

        // ������� �����
        void mutate() {
            Random rand = new Random();
            
            for (int i = 0; i < ships.size(); i++) {
                if (rand.nextDouble() < MUTATION_RATE) {
                    // �������� ��������� ������� �� �����
                    ships.set(i, createRandomShip(ships.get(i).size));
                }
            }
            calculateFitness();
        }
        
        void printBoard() {
            // ������� � ��������� �����
            char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
            for (char[] row : board) Arrays.fill(row, '.');  // ������ ������
            
            // ��������� ������� (S - ship)
            for (Ship ship : ships) {
                for (int i = 0; i < ship.size; i++) {
                    int x = ship.isHorizontal ? ship.x + i : ship.x;
                    int y = ship.isHorizontal ? ship.y : ship.y + i;
                    if (isWithinBoard(x,y)) {
                        board[x][y] = 'S';
                    }
                }
            }

            // ����� ���������� �������� (A-J)
            System.out.print("   ");
            for (char c = 'A'; c < 'A' + BOARD_SIZE; c++) {
                System.out.print(c + " ");
            }
            System.out.println();

            // ����� ����� � �������� �����
            for (int y = 0; y < BOARD_SIZE; y++) {
                System.out.printf("%2d ", y);  // ����� ������ � �������������
                for (int x = 0; x < BOARD_SIZE; x++) {
                    System.out.print(board[x][y] + " ");
                }
                System.out.println();
            }
        }
    }

    // ����� ��� ������������� �������
    static class Ship {
        int x, y; // ������� ���� �������
        int size; // ������ �������
        boolean isHorizontal; // ����������

        Ship(int x, int y, int size, boolean isHorizontal) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.isHorizontal = isHorizontal;
        }
    }

    public static void main(String[] args) {
        // ������������� ���������
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Individual());
        }

        // �������� ���� ������������� ���������
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // ���������� �� �����������������
            population.sort(Comparator.comparingInt(ind -> ind.fitness));

            // �������� �� ��������� �������
            if (population.get(0).fitness == 0) {
                System.out.println("������� ������� � ��������� " + generation);
                break;
            }

            // ����� ������� ������
            List<Individual> newPopulation = new ArrayList<>();
            int eliteCount = (int)(POPULATION_SIZE * ELITISM_RATE);
            newPopulation.addAll(population.subList(0, eliteCount));

            // ���������� ��������� ���������
            while (newPopulation.size() < POPULATION_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);
                Individual child = crossover(parent1, parent2);
                child.mutate();
                newPopulation.add(child);
            }

            population = newPopulation;
        }

        // ����� ������� �������
        population.sort(Comparator.comparingInt(ind -> ind.fitness));
        Individual best = population.get(0);
        System.out.println("������ ����������� (�����: " + best.fitness + ")");
        best.printBoard();
    }

    // ��������� �����
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

            // ����������� ��������� �� ���� ���������
            boolean isHorizontal = rand.nextBoolean() ? p1Ship.isHorizontal : p2Ship.isHorizontal;
            int x = rand.nextBoolean() ? p1Ship.x : p2Ship.x;
            int y = rand.nextBoolean() ? p1Ship.y : p2Ship.y;

            // �����������, ��� ������� �� ������ �� ������� ����
            if (isHorizontal) {
                x = Math.min(x, BOARD_SIZE - p1Ship.size); // ������������ X ��� ��������������
            } else {
                y = Math.min(y, BOARD_SIZE - p1Ship.size); // ������������ Y ��� ������������
            }

            childShips.add(new Ship(x, y, p1Ship.size, isHorizontal));
        }

        return new Individual(childShips);
    }
}
