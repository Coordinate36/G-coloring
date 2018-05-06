import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Tabu {
    private final String dir = "/home/whb/program/cpp/algorithm/hw6/";
    private final String INFILE = dir + "data/DSJC500.5.col.txt";
    private final String OUTFILE = dir + "out.txt";
    private final int MAX_DELT = 1000;
    private int[][] graph;
    private int[][] tabuList;           // 禁忌表
    private int[][] adjColorTable;      // 邻接颜色表,标记每个顶点邻接某颜色的顶点数
    private int[] colors;               // 每个顶点对应的颜色
    private int[] vEdge;                  // 每个顶点的边数
    private int delt;                   // 移动增量
    private int conflictNum;            // 冲突数
    private int bestConflictNum;        // 历史最佳冲突数
    private int node;                   // 每次移动的节点
    private int color;                  // 每次移动的颜色
    private int iter;                   // 迭代次数
    private int N;                      // 顶点数
    private int K;                      // 颜色数
    private Random random;
    private int[][] equDelt;
    private int[][] equTabuDelt;

    void createGraph() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(INFILE)));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.charAt(0) == 'c') {
                continue;
            }
            String[] data = line.split(" ");
            if (line.charAt(0) == 'p') {
                N = Integer.parseInt(data[2]);
                graph = new int[N][N];
                colors = new int[N];
                vEdge = new int[N];
            } else {
                int v1 = Integer.parseInt(data[1]) - 1;
                int v2 = Integer.parseInt(data[2]) - 1;
                graph[v1][vEdge[v1]++] = v2;
                graph[v2][vEdge[v2]++] = v1;
            }
        }
        bufferedReader.close();
    }

    void initVertexColor() {
        for (int i = 0; i < N; ++i) {
            colors[i] = random.nextInt(K);
        }
        adjColorTable = new int[N][K];
        tabuList = new int[N][K];
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < vEdge[i]; ++j) {
                int adjColor = colors[graph[i][j]];
                adjColorTable[i][adjColor]++;
                if (adjColor == colors[i]) {
                    ++conflictNum;
                }
            }
        }
        conflictNum >>= 1;
        bestConflictNum = conflictNum;
        System.out.println("Init vertex color, number of conlicts:" + conflictNum);
    }

    void findBestMove() {
        delt = MAX_DELT;
        int tabuDelt = MAX_DELT;
        int count = 0, tabuCount = 0;
        for (int i = 0; i < N; ++i) {
            if (adjColorTable[i][colors[i]] > 0) {
                for (int j = 0; j < K; ++j) {
                    if (j != colors[i]) {
                        int tmp = adjColorTable[i][j] - adjColorTable[i][colors[i]];
                        if (tabuList[i][j] <= iter) {
                            // 非禁忌搜索
                            if (tmp <= delt) {
                                if (tmp < delt) {
                                    count = 0;
                                    delt = tmp;
                                }
                                equDelt[count][0] = i;
                                equDelt[count++][1] = j;
                            }
                        } else if (tmp <= tabuDelt) {
                            // 禁忌搜索
                            if (tmp < tabuDelt) {
                                tabuCount = 0;
                                tabuDelt = tmp;
                            }
                            equTabuDelt[tabuCount][0] = i;
                            equTabuDelt[tabuCount++][1] = j;
                        }
                    }
                }
            }
        }
        if (tabuDelt < (bestConflictNum - conflictNum) && tabuDelt < delt) {
            delt = tabuDelt;
            int rand = random.nextInt(tabuCount);
            node = equTabuDelt[rand][0];
            color = equTabuDelt[rand][1];
        } else {
            int rand = random.nextInt(count);
            node = equDelt[rand][0];
            color = equDelt[rand][1];
        }
    }

    void move() {
        conflictNum += delt;        // 更新冲突值
        if (conflictNum < bestConflictNum) {
            bestConflictNum = conflictNum;
        }
        int oldColor = colors[node];
        colors[node] = color;
        tabuList[node][oldColor] = iter + conflictNum + random.nextInt(10) + 1;
        for (int i = 0; i < vEdge[node]; ++i) {
            adjColorTable[graph[node][i]][oldColor]--;
            adjColorTable[graph[node][i]][color]++;
        }
    }

    void tabuSearch() {
        try {
            createGraph();
        } catch (IOException e) {
            System.out.println("Failed to create graph.");
            e.printStackTrace();
        }
        equDelt = new int[MAX_DELT][2];
        equTabuDelt = new int[MAX_DELT][2];
        Scanner scanner = new Scanner(System.in);
        while (true) {
            K = scanner.nextInt();
            random = new Random();
            initVertexColor();
            double start_time = System.currentTimeMillis();
            iter = 0;
            while (conflictNum > 0) {
                ++iter;
                findBestMove();
                move();
            }
            double end_time = System.currentTimeMillis();
            double elapsedTime = (end_time - start_time) / 1000;
            System.out.println("Success, iterations: " + iter + "elapsed time: " + elapsedTime + "frequence: " + (iter / elapsedTime));
        }
    }

    public static void main(String[] args) {
        Tabu tabu = new Tabu();
        tabu.tabuSearch();
    }
}
