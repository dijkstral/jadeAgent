package warGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import jade.core.Agent;

public class battleFieldAgent extends Agent{
	/*环境智能体，负责提供战场环境等信息其他智能体*/

	//private static final long serialVersionUID = 3588692493424615462L;
	protected String[][] map;

	public void initMap(String mapFilePath) throws FileNotFoundException
	{
		/*地图初始化，测试阶段暂时用正方格而不用六角格*/
		int count = 0;
		Scanner in = new Scanner(new File(mapFilePath));
		while(in.hasNextLine())
		{				
			String str = in.nextLine();
			String height = str.trim();
			String[] heights = height.split("[\\p{Space}]+");
			map[count] = heights.clone();		
		}
	}
	
	public void initEnemyForce(String mapFilePath) throws FileNotFoundException
	{
		/*敌方算子数据初始化
		 * 敌方算子内容：
		 * 		算子编号GUID
		 * 		算子名称enemyName
		 * 		算子种类编号
		 * 		算子武器编号
		 * 		算子当前位置
		 * 		算子装甲编号
		 * 		算子侦查距离
		 * 		*/
		int count = 0;
		Scanner in = new Scanner(new File(mapFilePath));
		while(in.hasNextLine())
		{				
			String str = in.nextLine();
			String height = str.trim();
			String[] heights = height.split("[\\p{Space}]+");
			map[count] = heights.clone();		
		}
	}
	
	public boolean isTongshi()
	{
		/*判断敌我双方算子是否可以通视*/
		return false;
	}
	
	public boolean isZhishi()
	{
		/*判断敌我双方算子是否可以直视*/
		return false;
	}
	
	public String[] check()
	{
		/*返回当前坐标和侦查距离内是否可以发现敌方算子*/
		return null;
	}
	
	public int[] checkRecon()
	{
		return null;		
	}
	
	public String[] checkPath()
	{/*返回从当前位置到目的地的地图坐标路径*/
		return null;		
	}
	

	

}
