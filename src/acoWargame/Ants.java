package acoWargame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class Ants {
    /*
     * ################################################
     * ########## ACO algorithms for the TSP ##########
     * ################################################
     * Purpose: implementation of procedures for ants' behaviour
     */

	public final static int MAXTARGETS = 15;
	public final static int MAXSOLIDERS = 15;
	public final static float SWITCHLINES = (float) 0.2;
	
    static class ant_struct 
    {
		int[] tour;
		boolean[] visited;//禁忌表，显示城市是否被访问了
		int tour_length;
    }
    
    static class ant_struct_wargame//兵棋中蚂蚁的结构
    {
    	int[] targets;
    	//int[] ant_soliders;
    	//int[] ant_enemys;
    	//int[] ant_collision;
    	double pher;
    	double value;
    	//double[][] pherNew;
    	int[][] targetIn;
    	int valueNew = 0;
    	int riskNew = 0;
    }
        
    static int[][] targets;//目标
    static int[][] values;//点值
    static int[][] risks;//风险
    
    public static int targets_wargame;//假定敌方目标数量

    public static final int MAX_ANTS = 1024;
    public static final int MAX_NEIGHBOURS = 512;

    static ant_struct ant[];
    static ant_struct best_so_far_ant;
    static ant_struct restart_best_ant;
    
    static ant_struct_wargame ant_wargame[];//蚂蚁存放
    static int resultSummon[];//结果存放集

    static double pheromone[][];//设置二维数组，存放生物素，数组大小为城市数目x城市数目
    static double total[][];//通过启发函数和生物素，获得对应的总的概率，通过查询对应的最大的概率，选择下一步的移动方向
    static double total_wargame[];
    
    static double sumPoint = 0;//计算总共的点值--liuzhuan

    static double prob_of_selection[];

    static int n_ants; //蚂蚁数量
    static int nn_ants; /*
			 * length of nearest neighbor lists for the ants'
			 * solution construction
			 */

    static double rho; /* parameter for evaporation 生物素蒸发参数r*/
    static double alpha; /* importance of trail */
    static double beta; /* importance of heuristic evaluate 启发参数*/
    static double q_0; /* probability of best choice in tour construction */

    static boolean as_flag; /* ant system 普通蚁群系统*/
    static boolean eas_flag; /* elitist ant system 精英蚁群系统*/
    static boolean ras_flag; /* rank-based version of ant system 分等级的蚁群系统*/
    static boolean mmas_flag; /* MAX-MIN ant system 最大最小蚂蚁系统*/
    static boolean bwas_flag; /* best-worst ant system 最好最差蚁群系统*/
    static boolean acs_flag; /* ant colony system 蚁群聚类系统*/

    static int elitist_ants; /*
			      * additional parameter for elitist
			      * ant system, no. elitist ants
			      */

    static int ras_ranks; /*
			   * additional parameter for rank-based version
			   * of ant system
			   */

    static double trail_max; /* maximum pheromone trail in MMAS */
    static double trail_min; /* minimum pheromone trail in MMAS */
    static int u_gb; /* every u_gb iterations update with best-so-far ant */

    static double trail_0; /* initial pheromone level in ACS and BWAS */

    static double HEURISTIC(int m, int n) //启发函数
    {
    	return (1.0 / ((double) Tsp.instance.distance[m][n] + 0.1));
    }
    
    static double HEURISTIC_wargame(int m) 
    {
    	/*
    	 * 测试使用
    	 * 和对差的大小成反比
    	 * 对差越大，拟合率越小
    	 * */
    	return (ant_wargame[m].value*0.01 + 0.1);
    }

    static double HEURISTIC_wargameNew(int m) 
    {
    	return (ant_wargame[m].valueNew*0.01 + 100/ant_wargame[m].riskNew);
    	
    }
    
    public static final double EPSILON = 0.00000000000000000000000000000001;
    
    
    static void init_pheromone_trails_wargame(double initial_trail) //初始化生物素--liuzhuan
    {   
    		
		for(int i = 0;i<n_ants;i++)
		{
			ant_wargame[i].pher = initial_trail;
			/*for(int j=0;j<MAXSOLIDERS;j++)
			{
				for(int k=0;k<MAXTARGETS;k++)
				{
					if(targets[j][k]!=0)
						ant_wargame[i].pherNew[j][k] = initial_trail;//初始化蚂蚁自己携带的信息素列表
				}
			}*/
			
		}
		total_wargame = new double[n_ants];
    }
    
    static void allocate_ants_wargame() //初始化蚂蚁内存，分配基本参数--liuzhuan
    {
    	System.out.println("allocate_ants_wargame");
    	ant_wargame = new ant_struct_wargame[n_ants];
		for(int i = 0;i<n_ants;i++)
		{
			ant_wargame[i] = new ant_struct_wargame();
			ant_wargame[i].value = -1;
			ant_wargame[i].pher = 0;
			ant_wargame[i].targets = new int[8];
			//ant_wargame[i].pherNew = new double[MAXSOLIDERS][MAXTARGETS];//对信息素列表初始化
			ant_wargame[i].targetIn = new int[MAXSOLIDERS][MAXTARGETS];//对当前所在的目标列表初始化
		}
		try 
		{
			initTargetsValuesRisksNew();
		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

    static void allocate_ants()
    /*
     * FUNCTION: allocate the memory for the ant colony, the best-so-far and
     * the iteration best ant
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: allocation of memory for the ant colony and two ants that
     * store intermediate tours
     */
    {
		int i;
	
		ant = new ant_struct[n_ants];
	
		for (i = 0; i < n_ants; i++) 
		{
		    ant[i] = new ant_struct();
		    ant[i].tour = new int[Tsp.n + 1];
		    ant[i].visited = new boolean[Tsp.n];
		}
		best_so_far_ant = new ant_struct();
		best_so_far_ant.tour = new int[Tsp.n + 1];
		best_so_far_ant.visited = new boolean[Tsp.n];
	
		restart_best_ant = new ant_struct();
		restart_best_ant.tour = new int[Tsp.n + 1];
		restart_best_ant.visited = new boolean[Tsp.n];
	
		prob_of_selection = new double[nn_ants + 1];
		for (i = 0; i < nn_ants + 1; i++) 
		{
		    prob_of_selection[i] = Double.POSITIVE_INFINITY;
		}
    }
    
    static void calc_value_point_wargame(int n)//测试使用，计算第n只蚂蚁的对差点值--liuzhuan
    {
    	double point = 0;
    	int enemy[] = {1,2,3,4,5,6,7,8};
    	for(int i = 0; i < 8; i++)
    	{
    		//point += alpha*(ant_wargame[n].targets[i] - enemy[i]);
    		double tempPoint = ant_wargame[n].targets[i] - enemy[i];
    		if(tempPoint<0)
    			point += 0;
    		else if(tempPoint<1)
    			point += 1;
    		else if(tempPoint<5)
    			point += 2;
    		else if(tempPoint<7)
    			point += 3;
    		else
    			point += 4; 		
    		

    		//point += (ant_wargame[n].targets[i] - enemy[i]);
    	}
    	ant_wargame[n].value = point;
    	//System.out.print(n + ":");
    	//for(int i=0;i<ant_wargame[n].targets.length;i++)
    		//System.out.print(ant_wargame[n].targets[i]);
    	//System.out.print(":"+point);
    	//System.out.println(" ");
    }
    
    
    /**
     * 对当前的组合计算其打击效果和风险
     * 计算规则：
     * 1，每个目标受到两个打击时，打击效果只计算最大的一个，另一个忽略
     * 2，每个目标受到两个打击时，风险效果只计算最小的一个，另一个忽略
     * 
     * 程序复杂度过高，需要优化！
     * */
    static void calcValueRiskPointNew(int n)
    {
    	int value = 0;
    	int risk = 0;
    	for(int i=0;i<MAXSOLIDERS;i++)
    	{
			int maxValue = 0;
			int maxRisk = 0;
    		for(int j=0;j<MAXTARGETS;j++)
    		{
    			if(ant_wargame[n].targetIn[i][j]!=0)
    			{
    				if(!hasCount(n,i,j))
    				{
    					maxValue = values[i][j];
    					for(int k=i+1;k<MAXSOLIDERS;k++)
    					{
    						if(ant_wargame[n].targetIn[k][j]!=0)
    							if(maxValue<values[k][j])
    								maxValue = values[k][j];
    					}
    				}
    				if(!hasCount(n,i,j))
    				{
    					maxRisk = risks[i][j];
    					for(int k=i+1;k<MAXSOLIDERS;k++)
    					{
    						if(ant_wargame[n].targetIn[k][j]!=0)
    							if(maxRisk<risks[k][j])
    								maxRisk = risks[k][j];
    					}
    				}
    			}
    			value +=maxValue;
    			risk += maxRisk;
    			maxValue = 0;
    			maxRisk = 0;
    		}
    	}
    	ant_wargame[n].valueNew = value;
    	ant_wargame[n].riskNew = risk;
    }
    
    /**
     * 检查当前的是否已经被计数了
     * */
    static boolean hasCount(int n,int line,int row)
    {
    	int i;
    	for(i=0;i<MAXSOLIDERS;i++)
    	{
    		if(ant_wargame[n].targetIn[i][row]==1)
    			break;
    	}
    	if(line==i)
    		return false;
    	else 
    		return true;
    }
    
    
    static int find_best_wargame()//查找当前最好的蚂蚁--liuzhuan
    {    	
    	int k_best = 1;
    	double tempHighestScore = 0;   
    	
    	for(int i=0;i<total_wargame.length;i++)
    	{
    		if(tempHighestScore<total_wargame[i])
    		{
    			k_best = i;
    			tempHighestScore = total_wargame[i];
    		}/*
    		if(tempHighestScore<ant_wargame[i].value)
    		{
    			k_best = i;
    			tempHighestScore=ant_wargame[i].value;
    		}*/
    	}  

    	double highestPoint = 0;
    	/*for(int i = 0;i < n_ants; i++)
    	{
    		//ant_wargame[i].value = calc_value_point_wargame(i);
    		if(highestPoint <= ant_wargame[i].value)
    		{
    			k_best = i;
    			highestPoint = ant_wargame[i].value;
    		}
    	}*/
    	return k_best;
    }

    static int find_best()
    /*
     * 找到目前为止历程最小的蚂蚁，作为最优结果，返回其id号
     * FUNCTION: find the best ant of the current iteration
     * INPUT: none
     * OUTPUT: index of struct containing the iteration best ant
     * (SIDE)EFFECTS: none
     */
    {
		int min;
		int k, k_min;
	
		min = ant[0].tour_length;
		k_min = 0;
		for (k = 1; k < n_ants; k++)
		{
		    if (ant[k].tour_length < min) 
		    {
		    	min = ant[k].tour_length;
		    	k_min = k;
		    }
		}
		return k_min;
    }

    static int find_worst()
    /*
     * FUNCTION: find the worst ant of the current iteration
     * INPUT: none
     * OUTPUT: pointer to struct containing iteration best ant
     * (SIDE)EFFECTS: none
     */
    {
		int max;
		int k, k_max;
	
		max = ant[0].tour_length;
		k_max = 0;
		for (k = 1; k < n_ants; k++) 
		{
		    if (ant[k].tour_length > max) 
		    {
		    	max = ant[k].tour_length;
		    	k_max = k;
		    }
		}
		return k_max;
    }

    /************************************************************
     ************************************************************ 
     Procedures for pheromone manipulation
     ************************************************************ 
     ************************************************************/

    static void init_pheromone_trails(double initial_trail)
    /*
     * FUNCTION: initialize pheromone trails
     * INPUT: initial value of pheromone trails "initial_trail"
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromone matrix is reinitialized
     */
    {
		int i, j;
	
		/* Initialize pheromone trails */
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j <= i; j++) 
		    {
		    	pheromone[i][j] = initial_trail;
		    	pheromone[j][i] = initial_trail;
		    	total[i][j] = initial_trail;
		    	total[j][i] = initial_trail;
		    }
		}
    }
    
    /**
     * 对字符串进行解析
     * */
	public static String[] splitStrNew(String str)
	{
		String childrenStr = str.trim();
		String[] abc = childrenStr.split("[\\p{Space}]+");
        /*String GUID = abc[0];
        String str2 = abc[1];
        System.out.println(str1);
        System.out.println(str2);*/
        return abc;	
	}
    
    
    
    /**
     * 从文本读入目标、风险、点值
     * @throws FileNotFoundException 
     * */
    static int[][] readTxtFromFilesNew(String filePath) throws FileNotFoundException
    {
    	int[][] tempINT = new int[MAXSOLIDERS][MAXTARGETS];
    	try{
    
	    	//int[][] tempINT = null;
	    	/*for(int i=0;i<MAXSOLIDERS;i++)
	    	{
	    		for(int j=0;j<MAXTARGETS;j++)
	    			tempINT[i][j] = 0;
	    	}*/
	    	Scanner in = new Scanner(new File(filePath));
	    	int countLine = 0;
	    	while(in.hasNextLine())
			{				
				String str = in.nextLine();
				String[] targetsInfo = splitStrNew(str);
				for(int i=0;i<targetsInfo.length;i++)
				{
					tempINT[countLine][i] = Integer.valueOf(targetsInfo[i]).intValue();
				}
				countLine++;		
			}
	    	
    	}
    	catch(FileNotFoundException e)
		{
			e.printStackTrace();
		} 
    	return tempINT;
    }
    
    /**
     * @throws FileNotFoundException 
     * */    
    static void initTargetsValuesRisksNew() throws FileNotFoundException
    {
	    	System.out.println("初始化Targets/Values/Risks");
	/*    	for(int i=0;i<15;i++)
	    		for(int j=0;j<15;j++)
	    		{
	    			targets[i][j] = 0;
	    			values[i][j] = 0;
	    			risks[i][j] = 0;
	    		}  */
	    	targets = new int[MAXSOLIDERS][MAXTARGETS];
	    	values = new int[MAXSOLIDERS][MAXTARGETS];
	    	risks = new int[MAXSOLIDERS][MAXTARGETS];
	    	
	    	targets = readTxtFromFilesNew("D:/Program Files (x86)/targets.txt");
	    	values = readTxtFromFilesNew("D:/Program Files (x86)/values.txt");
	    	risks = readTxtFromFilesNew("D:/Program Files (x86)/risks.txt");
	    	//System.out.println(targets);
	    	//System.out.println(values);
	    	//System.out.println(risks);

    }
        
    static void evaporation_wargame()//--liuzhuan
    {
    	for(int i = 0;i < n_ants; i++)
    	{
    		ant_wargame[i].pher *= (1 - rho);
    	}
    }

    static void evaporation()
    /*
     * 生物素蒸发 
     * FUNCTION: implements the pheromone trail evaporation
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones are reduced by factor rho
     */
    {
		int i, j;	
		// TRACE ( System.out.println("pheromone evaporation\n"); );	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j <= i; j++) 
		    {
		    	pheromone[i][j] = (1 - rho) * pheromone[i][j];
		    	pheromone[j][i] = pheromone[i][j];
		    }
		}
    }

    static void evaporation_nn_list()
    /*
     * FUNCTION: simulation of the pheromone trail evaporation
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones are reduced by factor rho
     * REMARKS: if local search is used, this evaporation procedure
     * only considers links between a city and those cities
     * of its candidate list
     */
    {
		int i, j, help_city;
	
		// TRACE ( System.out.println("pheromone evaporation nn_list\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < nn_ants; j++) 
		    {
		    	help_city = Tsp.instance.nn_list[i][j];//？？？？
		    	
		    	pheromone[i][help_city] = (1 - rho) * pheromone[i][help_city];
		    }
		}
    }
    
    static void global_update_pheromone_wargame()//对所有蚂蚁的信息素进行更新，和Value值成正比
    {    	
		double d_tau;		
		for (int i = 0; i < n_ants; i++) 
		{
			d_tau = ant_wargame[i].value*0.01;
			ant_wargame[i].pher += d_tau;
		}	
    }
    
    
    /**
     * 根据value和risk对当前蚂蚁进行信息素更新
     * */
    static void global_update_pheromone_wargameNew()//对所有蚂蚁的信息素进行更新，和Value值成正比
    {    	
		double d_tau;		
		for (int i = 0; i < n_ants; i++) 
		{
			d_tau = ant_wargame[i].valueNew*0.01;//+100/ant_wargame[i].riskNew;
			ant_wargame[i].pher += d_tau;
		}	
    }

    static void global_update_pheromone(ant_struct a)
    /*
     * 对由j到h的生物素进行更新，更新增加的大小与tour_length成反比
     * FUNCTION: reinforces edges used in ant k's solution
     * INPUT: pointer to ant that updates the pheromone trail
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones of arcs in ant k's tour are increased
     */
    {
		int i, j, h;
		double d_tau;
	
		// TRACE ( System.out.println("global pheromone update\n"); );
	
		d_tau = 1.0 / (double) a.tour_length;
		for (i = 0; i < Tsp.n; i++) 
		{
		    j = a.tour[i];
		    h = a.tour[i + 1];
		    pheromone[j][h] += d_tau;
		    pheromone[h][j] = pheromone[j][h];
		}	
    }

    static void global_update_pheromone_weighted(ant_struct a, int weight)
    /*
     * FUNCTION: reinforces edges of the ant's tour with weight "weight"
     * INPUT: pointer to ant that updates pheromones and its weight
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones of arcs in the ant's tour are increased
     */
    {
		int i, j, h;
		double d_tau;
	
		// TRACE ( System.out.println("global pheromone update weighted\n"); );
	
		d_tau = (double) weight / (double) a.tour_length;
		for (i = 0; i < Tsp.n; i++) 
		{
		    j = a.tour[i];
		    h = a.tour[i + 1];
		    pheromone[j][h] += d_tau;
		    pheromone[h][j] = pheromone[j][h];
		}
    }
    
    static void compute_total_information_wargame()
    /*
     * 计算转移概率，由启发函数和生物素决定
     */
    {    	
		int i;	
		
		for (i = 0; i < n_ants; i++) 
		{
			total_wargame[i] = Math.pow(ant_wargame[i].pher, alpha) * Math.pow(HEURISTIC_wargame(i), beta);
		}
    }
    
    static void compute_total_information_wargameNew()
    {    	
		int i;	
		
		for (i = 0; i < n_ants; i++) 
		{
			//total_wargame[i] = Math.pow(ant_wargame[i].pher, alpha) * Math.pow(HEURISTIC_wargameNew(i), beta);
			total_wargame[i] = Math.pow(HEURISTIC_wargameNew(i), beta);
			
		}
    }

    static void compute_total_information()
    /*
     * FUNCTION: calculates heuristic info times pheromone for each arc
     * INPUT: none
     * OUTPUT: none
     */
    {
		int i, j;
	
		// TRACE ( System.out.println("compute total information\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < i; j++) 
		    {
		    	total[i][j] = Math.pow(pheromone[i][j], alpha) * Math.pow(HEURISTIC(i, j), beta);
		    	total[j][i] = total[i][j];
		    }
		}
    }

    static void compute_nn_list_total_information()
    /*
     * FUNCTION: calculates heuristic info times pheromone for arcs in nn_list
     * INPUT: none
     * OUTPUT: none
     */
    {
		int i, j, h;
	
		// TRACE ( System.out.println("compute total information nn_list\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < nn_ants; j++)
		    {
		    	h = Tsp.instance.nn_list[i][j];
		    	if (pheromone[i][h] < pheromone[h][i])
			    /* force pheromone trails to be symmetric as much as possible */
		    		pheromone[h][i] = pheromone[i][h];
		    	total[i][h] = Math.pow(pheromone[i][h], alpha) * Math.pow(HEURISTIC(i, h), beta);
		    	total[h][i] = total[i][h];
		    }
		}
    }

    /****************************************************************
     **************************************************************** 
     Procedures implementing solution construction and related things
     **************************************************************** 
     ****************************************************************/

    static void ant_empty_memory(ant_struct a)
    /*
     * FUNCTION: empty the ants's memory regarding visited cities
     * INPUT: ant identifier
     * OUTPUT: none
     * (SIDE)EFFECTS: vector of visited cities is reinitialized to Utilities.FALSE
     */
    {
		int i;
	
		for (i = 0; i < Tsp.n; i++)
		{
		    a.visited[i] = false;
		}
    }
    
    
    /**
     * 清空蚂蚁内容
     * 包括targetIn
     * */
    static void ant_empty_memoryNew(ant_struct_wargame a)
    {
		int i;
	
		for (i = 0; i < MAXSOLIDERS; i++)
		{
		    a.targetIn[i] = new int[MAXTARGETS];
		    a.pher = 0;
		    a.value = 0;
		    a.valueNew = 0;
		    a.riskNew = 0;
		}
    }
    
    static void place_ant(ant_struct a, int step)
    /*
     * FUNCTION: place an ant on a randomly chosen initial city
     * INPUT: pointer to ant and the number of construction steps
     * OUTPUT: none
     * (SIDE)EFFECT: ant is put on the chosen city
     */
    {
		int rnd;
	
		rnd = (int) (Utilities.ran01(Utilities.seed) * (double) Tsp.n); /* random number between 0 .. n-1 */
	
		a.tour[step] = rnd;
		a.visited[rnd] = true;
    }
   
    static void place_ant_wargame(ant_struct_wargame a)//随机将八个目标放入对应的序列中--liuzhuan
    {	
    	//System.out.println("place_ant_wargame");
		Random r = new Random();	
		int randomCount = 0;
		//rnd = (int) (Utilities.ran01(Utilities.seed) * (double) 8);
		int randomList[] = {1,2,3,4,5,6,7,8};
		
		for(int i = 0; i < 8; i++)
		{
			randomCount = r.nextInt(8);
			while(randomList[randomCount]==0)
				randomCount = r.nextInt(8);
			a.targets[i] = randomList[randomCount];
			randomList[randomCount] = 0;
			//System.out.print(a.targets[i]);
		}
		
    	/*for(int i=0;i<a.targets.length;i++)
    		System.out.print(a.targets[i]);
    	System.out.println(" ");*/
    }
    /**
     * 对每个己方算子选择一个目标，采用随机轮盘赌的方式
     * */
    static void place_ant_wargameNew(ant_struct_wargame a)
    {	
    	//System.out.println("place_ant_wargame");
		Random r = new Random();	
		for(int i=0;i<MAXSOLIDERS;i++)
		{
			int randomCount = r.nextInt(MAXTARGETS*2);
			int j=0;
			while(randomCount!=0)
			{
				if(j>=MAXTARGETS)
					j=0;
				if(targets[i][j]!=0)
					randomCount--;
				j++;	
			}
			if(j==0)
				j=MAXTARGETS-1;
			j--;
			a.targetIn[i][j] = 1;
		}
    }
    
    
    static void choose_best_next(ant_struct a, int phase)
    /*
     * 选择与目前城市最大概率的城市移动
     * FUNCTION: chooses for an ant as the next city the one with
     * maximal value of heuristic information times pheromone
     * INPUT: pointer to ant and the construction step
     * OUTPUT: none
     * (SIDE)EFFECT: ant moves to the chosen city
     */
    {
		int city, current_city, next_city;
		double value_best;
	
		next_city = Tsp.n;
		assert (phase > 0 && phase < Tsp.n);
		current_city = a.tour[phase - 1];
		value_best = -1.; /* values in total matrix are always >= 0.0 */
		for (city = 0; city < Tsp.n; city++) 
		{
		    if (a.visited[city])
		    	; /* city already visited, do nothing */
		    else
		    {
		    	/*找到最大概率，以及对应的城市*/
				if (total[current_city][city] > value_best) 
				{
				    next_city = city;
				    value_best = total[current_city][city];
				}
		    }
		}
		assert (0 <= next_city && next_city < Tsp.n);
		assert (value_best > 0.0);
		assert (a.visited[next_city] == false);
		/*移动到下一个城市*/
		a.tour[phase] = next_city;
		a.visited[next_city] = true;
    }

    static void neighbour_choose_best_next(ant_struct a, int phase)
    /*
     * 选择与目前城市最近的neighbour节点的最大生物素的城市移动
     * FUNCTION: chooses for an ant as the next city the one with
     * maximal value of heuristic information times pheromone
     * INPUT: pointer to ant and the construction step "phase"
     * OUTPUT: none
     * (SIDE)EFFECT: ant moves to the chosen city
     */
    {
		int i, current_city, next_city, help_city;
		double value_best, help;
	
		next_city = Tsp.n;
		assert (phase > 0 && phase < Tsp.n);
		current_city = a.tour[phase - 1];
		assert (0 <= current_city && current_city < Tsp.n);
		value_best = -1.; /* values in total matix are always >= 0.0 */
		for (i = 0; i < nn_ants; i++) 
		{
		    help_city = Tsp.instance.nn_list[current_city][i];
		    if (a.visited[help_city])
		    	; /* city already visited, do nothing */
		    else 
		    {
				help = total[current_city][help_city];
				if (help > value_best)
				{
				    value_best = help;
				    next_city = help_city;
				}
		    }
		}
		if (next_city == Tsp.n)
		    /* all cities in nearest neighbor list were already visited */
		    choose_best_next(a, phase);
		else {
		    assert (0 <= next_city && next_city < Tsp.n);
		    assert (value_best > 0.0);
		    assert (a.visited[next_city] == false);
		    a.tour[phase] = next_city;
		    a.visited[next_city] = true;
		}
    }

    static void choose_closest_next(ant_struct a, int phase)
    /*
     * 选择距离目前城市最近的下一个城市移动
     * FUNCTION: Chooses for an ant the closest city as the next one
     * INPUT: pointer to ant and the construction step "phase"
     * OUTPUT: none
     * (SIDE)EFFECT: ant moves to the chosen city
     */
    {
		int city, current_city, next_city, min_distance;
	
		next_city = Tsp.n;
		assert (phase > 0 && phase < Tsp.n);
		current_city = a.tour[phase - 1];
		min_distance = Integer.MAX_VALUE; /* Search shortest edge */
		for (city = 0; city < Tsp.n; city++) 
		{
		    if (a.visited[city])
		    	; /* city already visited */
		    else 
		    {
				if (Tsp.instance.distance[current_city][city] < min_distance) 
				{
				    next_city = city;
				    min_distance = Tsp.instance.distance[current_city][city];
				}
		    }
		}
		assert (0 <= next_city && next_city < Tsp.n);
		a.tour[phase] = next_city;
		a.visited[next_city] = true;
    }

    static void neighbour_choose_and_move_to_next(ant_struct a, int phase)
    /*
     * FUNCTION: Choose for an ant probabilistically a next city among all
     * unvisited cities in the current city's candidate list.
     * If this is not possible, choose the closest next
     * INPUT: pointer to ant the construction step "phase"
     * OUTPUT: none
     * (SIDE)EFFECT: ant moves to the chosen city
     */
    {
		int i, help;
		int current_city;
		double rnd, partial_sum = 0., sum_prob = 0.0;
		/* double *prob_of_selection; *//*
						 * stores the selection probabilities
						 * of the nearest neighbor cities
						 */
		double prob_ptr[];
	
		if ((q_0 > 0.0) && (Utilities.ran01(Utilities.seed) < q_0)) 
		{
		    /*
		     * 存在一定可能性，正好当前的路径既是最佳的路径
		     * with a probability q_0 make the best possible choice
		     * according to pheromone trails and heuristic information
		     */
		    /*
		     * we first check whether q_0 > 0.0, to avoid the very common case
		     * of q_0 = 0.0 to have to compute a random number, which is
		     * expensive computationally
		     */
		    neighbour_choose_best_next(a, phase);
		    return;
		}
	
		/*如果当前的路径不是最佳的路径*/
		prob_ptr = prob_of_selection;//根据生物素的计算结果，选择最佳的下一步移动目标
	
		current_city = a.tour[phase - 1]; /* current_city city of ant k */
		assert (current_city >= 0 && current_city < Tsp.n);
		for (i = 0; i < nn_ants; i++) 
		{
		    if (a.visited[Tsp.instance.nn_list[current_city][i]])
		    	prob_ptr[i] = 0.0; /* city already visited */
		    else 
		    {
		    	assert (Tsp.instance.nn_list[current_city][i] >= 0 && Tsp.instance.nn_list[current_city][i] < Tsp.n);
		    	prob_ptr[i] = total[current_city][Tsp.instance.nn_list[current_city][i]];
		    	sum_prob += prob_ptr[i];
		    }
		}
	
		if (sum_prob <= 0.0) 
		{
		    /* All cities from the candidate set are tabu */
		    choose_best_next(a, phase);
		} 
		else 
		{
		    /*
		     * at least one neighbor is eligible, chose one according to the
		     * selection probabilities
		     */
		    rnd = Utilities.ran01(Utilities.seed);
		    rnd *= sum_prob;
		    i = 0;
		    partial_sum = prob_ptr[i];
		    /* This loop always stops because prob_ptr[nn_ants] == HUGE_VAL */
		    while (partial_sum <= rnd) 
		    {
		    	i++;
		    	partial_sum += prob_ptr[i];
		    }
		    /*
		     * This may very rarely happen because of rounding if rnd is
		     * close to 1.
		     */
		    if (i == nn_ants) 
		    {
		    	neighbour_choose_best_next(a, phase);
		    	return;
		    }
		    assert (0 <= i && i < nn_ants);
		    assert (prob_ptr[i] >= 0.0);
		    help = Tsp.instance.nn_list[current_city][i];
		    assert (help >= 0 && help < Tsp.n);
		    assert (a.visited[help] == false);
		    a.tour[phase] = help; /* Tsp.instance.nn_list[current_city][i]; */
		    a.visited[help] = true;
		}
    }

    static void neighbour_choose_and_move_to_next_wargame()//--liuzhuan
    {    	
    	Random tempRandom = new Random();
    	int bestMove = find_best_wargame();
    	
    	for(int i = 0; i<n_ants; i++)
    	{
	    	//System.out.print("当前蚂蚁是：");
	    	//for(int j=0;j<Ants.ant_wargame[i].targets.length;j++)
	    		//System.out.print(Ants.ant_wargame[i].targets[j]);	
    		
    		 /* 与最好的组合随机交换两个目标，进行变异*/
    		if(i != bestMove)
    		{
    			int first = tempRandom.nextInt(8);
    			int second= tempRandom.nextInt(8);
    			while(first == second)
    				second= tempRandom.nextInt(8);
    			
    			int temp = ant_wargame[i].targets[first];
    			ant_wargame[i].targets[first] = ant_wargame[bestMove].targets[first];
    	    	for(int j=0;j<ant_wargame[i].targets.length;j++)
    	    	{
    	    		if(ant_wargame[i].targets[j]==ant_wargame[bestMove].targets[first]&&first!=j)
    	    			ant_wargame[i].targets[j] = temp;
    	    	}	 	
    	    	
    			temp = ant_wargame[i].targets[second];
    			ant_wargame[i].targets[second] = ant_wargame[bestMove].targets[second];
    	    	for(int j=0;j<ant_wargame[i].targets.length;j++)
    	    	{
    	    		if(ant_wargame[i].targets[j]==ant_wargame[bestMove].targets[second]&&second!=j)
    	    			ant_wargame[i].targets[j] = temp;
    	    	}
    	    	
    	    	//System.out.print("移动后蚂蚁是：");
    	    	//for(int j=0;j<Ants.ant_wargame[i].targets.length;j++)
    	    		//System.out.print(Ants.ant_wargame[i].targets[j]);
    	    	    	    	
    			/*
    			 * 1 2 6 5 4 8 3 7
    			 * 2 5 6 4 7 1 3 8 
    			 * */
    		}  
    		//System.out.println(" ");
       	}
    }
    
    /**
     * 蚂蚁进行移动
     * 移动规则如下：
     * 1，采用轮盘赌的方式移动
     * 2，随机交换10%的组合
     * */
    static void neighbour_choose_and_move_to_next_wargameNew()
    {
    	Random tempRandom = new Random();
    	int bestMove = find_best_wargame();
    	int[] randomNumbers = new int[MAXSOLIDERS];
    	for(int i=0;i<MAXSOLIDERS;i++)
    		randomNumbers[i] = i;
    	
    	for(int i = 0; i<n_ants; i++)
    	{	    	 		  		
    		if(i != bestMove)    			
    		{    			
    			int[] tempRN = randomNumbers.clone();
    			for(int j=0;j<Math.ceil(SWITCHLINES*MAXSOLIDERS);j++)
    			{    		
    				//System.out.print(Math.ceil(SWITCHLINES*MAXSOLIDERS)); 		
    				int switchLine = tempRandom.nextInt(MAXSOLIDERS);
    				while(tempRN[switchLine]==MAXSOLIDERS)
    				{
    					switchLine++;
    					if(switchLine>=MAXSOLIDERS)
    						switchLine = 0;    					
    				}
    				ant_wargame[i].targetIn[tempRN[switchLine]] = ant_wargame[bestMove].targetIn[tempRN[switchLine]];
    				tempRN[switchLine] = MAXSOLIDERS;
    			}
    		}
       	} 
    	
    	int self = tempRandom.nextInt(MAXSOLIDERS);
    	ant_wargame[bestMove].targetIn[self] = new int[MAXTARGETS];
    	int randomCount = tempRandom.nextInt(MAXTARGETS*2);
    	for(int i=0;i<10;i++)
    	{
	    	int j=0;
	    	while(randomCount!=0)
	    	{
	    		if(j>=MAXTARGETS)
	    			j=0;
	    		if(targets[self][j]!=0)
	    			randomCount--;
	    		j++;	
	    	}
	    	if(j==0)
	    		j=MAXTARGETS-1;
	    	j--;
	    	ant_wargame[bestMove].targetIn[self][j] = 1; 
    	}   	
    }
    
    
    
    static void swap_wargame(int num,int[] toBeSwap,int[] standar)//交换两个列表
    {
    	int temp = toBeSwap[num];
    	toBeSwap[num] = standar[num];
    	for(int i=0;i<toBeSwap.length;i++)
    	{
    		if(toBeSwap[i]==standar[num]&&num!=i)
    			toBeSwap[i] = temp;
    	}	 	
    }
    
    
    /****************************************************************
     **************************************************************** 
     Procedures specific to MAX-MIN Ant System
     **************************************************************** 
     ****************************************************************/

    static void mmas_evaporation_nn_list()
    /*
     * FUNCTION: simulation of the pheromone trail evaporation for MMAS
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones are reduced by factor rho
     * REMARKS: if local search is used, this evaporation procedure
     * only considers links between a city and those cities
     * of its candidate list
     */
    {
		int i, j, help_city;
	
		// TRACE ( System.out.println("mmas specific evaporation on nn_lists\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < nn_ants; j++) 
		    {
				help_city = Tsp.instance.nn_list[i][j];
				pheromone[i][help_city] = (1 - rho) * pheromone[i][help_city];
				if (pheromone[i][help_city] < trail_min)
				    pheromone[i][help_city] = trail_min;
		    }
		}
    }

    static void check_pheromone_trail_limits()
    /*
     * FUNCTION: only for MMAS without local search:
     * keeps pheromone trails inside trail limits
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones are forced to interval [trail_min,trail_max]
     */
    {
		int i, j;
	
		// TRACE ( System.out.println("mmas specific: check pheromone trail limits\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < i; j++) 
		    {
				if (pheromone[i][j] < trail_min) 
				{
				    pheromone[i][j] = trail_min;
				    pheromone[j][i] = trail_min;
				} 
				else if (pheromone[i][j] > trail_max) 
				{
				    pheromone[i][j] = trail_max;
				    pheromone[j][i] = trail_max;
				}
		    }
		}
    }

    static void check_nn_list_pheromone_trail_limits()
    /*
     * FUNCTION: only for MMAS with local search: keeps pheromone trails
     * inside trail limits
     * INPUT: none
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones are forced to interval [trail_min,trail_max]
     * COMMENTS: currently not used since check for trail_min is integrated
     * mmas_evaporation_nn_list and typically check for trail_max
     * is not done (see FGCS paper or ACO book for explanation
     */
    {
		int i, j, help_city;
	
		// TRACE ( System.out.println("mmas specific: check pheromone trail limits nn_list\n"); );
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    for (j = 0; j < nn_ants; j++) 
		    {
				help_city = Tsp.instance.nn_list[i][j];
				if (pheromone[i][help_city] < trail_min)
				    pheromone[i][help_city] = trail_min;
				if (pheromone[i][help_city] > trail_max)
				    pheromone[i][help_city] = trail_max;
		    }
		}
    }

    /****************************************************************
     **************************************************************** 
     Procedures specific to Ant Colony System
     **************************************************************** 
     ****************************************************************/

    static void global_acs_pheromone_update(ant_struct a)
    /*
     * FUNCTION: reinforces the edges used in ant's solution as in ACS
     * INPUT: pointer to ant that updates the pheromone trail
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones of arcs in ant k's tour are increased
     */
    {
		int i, j, h;
		double d_tau;
	
		// TRACE ( System.out.println("acs specific: global pheromone update\n"); );
	
		d_tau = 1.0 / (double) a.tour_length;
	
		for (i = 0; i < Tsp.n; i++) 
		{
		    j = a.tour[i];
		    h = a.tour[i + 1];
	
		    pheromone[j][h] = (1. - rho) * pheromone[j][h] + rho * d_tau;
		    pheromone[h][j] = pheromone[j][h];
	
		    total[h][j] = Math.pow(pheromone[h][j], alpha) * Math.pow(HEURISTIC(h, j), beta);
		    total[j][h] = total[h][j];
		}
    }
    
    
    static void local_acs_pheromone_update_wargame(ant_struct a)
    {
/*		int h, j;
	
		assert (phase > 0 && phase <= Tsp.n);
		j = a.tour[phase];
	
		h = a.tour[phase - 1];
		assert (0 <= j && j < Tsp.n);
		assert (0 <= h && h < Tsp.n);
		 still additional parameter has to be introduced 
		pheromone[h][j] = (1. - 0.1) * pheromone[h][j] + 0.1 * trail_0;
		pheromone[j][h] = pheromone[h][j];
		total[h][j] = Math.pow(pheromone[h][j], alpha) * Math.pow(HEURISTIC(h, j), beta);
		total[j][h] = total[h][j];*/
    }
    

    static void local_acs_pheromone_update(ant_struct a, int phase)
    /*
     * FUNCTION: removes some pheromone on edge just passed by the ant
     * INPUT: pointer to ant and number of constr. phase
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones of arcs in ant k's tour are increased
     * COMMENTS: I did not do experiments with with different values of the parameter
     * xi for the local pheromone update; therefore, here xi is fixed to 0.1
     * as suggested by Gambardella and Dorigo for the TSP. If you wish to run
     * experiments with that parameter it may be reasonable to use it as a
     * commandline parameter
     */
    {
		int h, j;
	
		assert (phase > 0 && phase <= Tsp.n);
		j = a.tour[phase];
	
		h = a.tour[phase - 1];
		assert (0 <= j && j < Tsp.n);
		assert (0 <= h && h < Tsp.n);
		/* still additional parameter has to be introduced */
		pheromone[h][j] = (1. - 0.1) * pheromone[h][j] + 0.1 * trail_0;
		pheromone[j][h] = pheromone[h][j];
		total[h][j] = Math.pow(pheromone[h][j], alpha) * Math.pow(HEURISTIC(h, j), beta);
		total[j][h] = total[h][j];
    }

    /****************************************************************
     **************************************************************** 
     Procedures specific to Best-Worst Ant System
     **************************************************************** 
     ****************************************************************/

    static void bwas_worst_ant_update(ant_struct a1, ant_struct a2)
    /*
     * FUNCTION: uses additional evaporation on the arcs of iteration worst
     * ant that are not shared with the global best ant
     * INPUT: pointer to the worst (a1) and the best (a2) ant
     * OUTPUT: none
     * (SIDE)EFFECTS: pheromones on some arcs undergo additional evaporation
     */
    {
		int i, j, h, pos, pred;
		int pos2[]; /* positions of cities in tour of ant a2 */
	
		// TRACE ( System.out.println("bwas specific: best-worst pheromone update\n"); );
	
		pos2 = new int[Tsp.n];
		for (i = 0; i < Tsp.n; i++) {
		    pos2[a2.tour[i]] = i;
		}
	
		for (i = 0; i < Tsp.n; i++) {
		    j = a1.tour[i];
		    h = a1.tour[i + 1];
		    pos = pos2[j];
		    if (pos - 1 < 0)
			pred = Tsp.n - 1;
		    else
			pred = pos - 1;
		    if (a2.tour[pos + 1] == h)
			; /* do nothing, edge is common with a2 (best solution found so far) */
		    else if (a2.tour[pred] == h)
			; /* do nothing, edge is common with a2 (best solution found so far) */
		    else { /* edge (j,h) does not occur in ant a2 */
			pheromone[j][h] = (1 - rho) * pheromone[j][h];
			pheromone[h][j] = (1 - rho) * pheromone[h][j];
		    }
		}
    }

    static void bwas_pheromone_mutation()
    /*
     * FUNCTION: implements the pheromone mutation in Best-Worst Ant System
     * INPUT: none
     * OUTPUT: none
     */
    {
		int i, j, k;
		int num_mutations;
		double avg_trail = 0.0, mutation_strength = 0.0, mutation_rate = 0.3;
	
		// TRACE ( System.out.println("bwas specific: pheromone mutation\n"); );
	
		/* compute average pheromone trail on edges of global best solution */
		for (i = 0; i < Tsp.n; i++) 
		{
		    avg_trail += pheromone[best_so_far_ant.tour[i]][best_so_far_ant.tour[i + 1]];
		}
		avg_trail /= (double) Tsp.n;
	
		/* determine mutation strength of pheromone matrix */
		/*
		 * FIXME: we add a small value to the denominator to avoid any
		 * potential division by zero. This may not be fully correct
		 * according to the original BWAS.
		 */
		if (InOut.max_time > 0.1)
		    mutation_strength = 4. * avg_trail * (Timer.elapsed_time() - InOut.restart_time)
			    / (InOut.max_time - InOut.restart_time + 0.0001);
		else if (InOut.max_tours > 100)
		    mutation_strength = 4. * avg_trail * (InOut.iteration - InOut.restart_iteration)
			    / (InOut.max_tours - InOut.restart_iteration + 1);
		else
		    System.out.println("apparently no termination condition applied!!\n");
	
		/* finally use fast version of matrix mutation */
		mutation_rate = mutation_rate / Tsp.n * nn_ants;
		num_mutations = (int) (Tsp.n * mutation_rate / 2);
		/* / 2 because of adjustment for symmetry of pheromone trails */
	
		if (InOut.restart_iteration < 2)
		    num_mutations = 0;
	
		for (i = 0; i < num_mutations; i++) 
		{
		    j = (int) (Utilities.ran01(Utilities.seed) * (double) Tsp.n);
		    k = (int) (Utilities.ran01(Utilities.seed) * (double) Tsp.n);
		    if (Utilities.ran01(Utilities.seed) < 0.5) 
		    {
		    	pheromone[j][k] += mutation_strength;
		    	pheromone[k][j] = pheromone[j][k];
		    } 
		    else 
		    {
		    	pheromone[j][k] -= mutation_strength;
		    	if (pheromone[j][k] <= 0.0) 
		    	{
		    		pheromone[j][k] = EPSILON;
		    	}
		    	pheromone[k][j] = pheromone[j][k];
		    }
		}
    }

    /**************************************************************************
     ************************************************************************** 
     Procedures specific to the ant's tour manipulation other than construction
     *************************************************************************** 
     **************************************************************************/

    static void copy_from_to(ant_struct a1, ant_struct a2) 
    {
	/*
	 * FUNCTION: copy solution from ant a1 into ant a2
	 * INPUT: pointers to the two ants a1 and a2
	 * OUTPUT: none
	 * (SIDE)EFFECTS: a2 is copy of a1
	 */
		int i;
	
		a2.tour_length = a1.tour_length;
		for (i = 0; i < Tsp.n; i++) {
		    a2.tour[i] = a1.tour[i];
		}
		a2.tour[Tsp.n] = a2.tour[0];
	    }
	
	    static int nn_tour()
	    /*
	     * FUNCTION: generate some nearest neighbor tour and compute tour length
	     * INPUT: none
	     * OUTPUT: none
	     * (SIDE)EFFECTS: needs ant colony and one statistic ants
	     */
	    {
		int phase, help;
	
		ant_empty_memory(ant[0]);
	
		phase = 0; /* counter of the construction steps */
		place_ant(ant[0], phase);
	
		while (phase < Tsp.n - 1) {
		    phase++;
		    choose_closest_next(ant[0], phase);
		}
		phase = Tsp.n;
		ant[0].tour[Tsp.n] = ant[0].tour[0];
		if (LocalSearch.ls_flag != 0) {
		    LocalSearch.two_opt_first(ant[0].tour);
		}
		InOut.n_tours += 1;
		/* copy_from_to( &ant[0], best_so_far_ant ); */
		ant[0].tour_length = Tsp.compute_tour_length(ant[0].tour);//????
	
		help = ant[0].tour_length;
		ant_empty_memory(ant[0]);
		return help;
    }

    static int distance_between_ants(ant_struct a1, ant_struct a2)
    /*
     * FUNCTION: compute the distance between the tours of ant a1 and a2
     * INPUT: pointers to the two ants a1 and a2
     * OUTPUT: distance between ant a1 and a2
     */
    {
	int i, j, h, pos, pred;
	int distance;
	int[] pos2; /* positions of cities in tour of ant a2 */

	pos2 = new int[Tsp.n];
	for (i = 0; i < Tsp.n; i++) {
	    pos2[a2.tour[i]] = i;
	}

	distance = 0;
	for (i = 0; i < Tsp.n; i++) {
	    j = a1.tour[i];
	    h = a1.tour[i + 1];
	    pos = pos2[j];
	    if (pos - 1 < 0)
		pred = Tsp.n - 1;
	    else
		pred = pos - 1;
	    if (a2.tour[pos + 1] == h)
		; /* do nothing, edge is common with best solution found so far */
	    else if (a2.tour[pred] == h)
		; /* do nothing, edge is common with best solution found so far */
	    else { /* edge (j,h) does not occur in ant a2 */
		distance++;
	    }
	}
	return distance;
    }

}
