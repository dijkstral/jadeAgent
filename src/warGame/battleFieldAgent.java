package warGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import jade.core.Agent;

/**
 * ���������壬�����ṩս����������Ϣ����������
 * */
public class battleFieldAgent extends Agent{
	//private static final long serialVersionUID = 3588692493424615462L;
	protected String[][] map;

	public void initMap(String mapFilePath) throws FileNotFoundException
	{
		/*��ͼ��ʼ�������Խ׶���ʱ����������������Ǹ�*/
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
	
	/**
	 * �з��������ݳ�ʼ��
	 * �з��������ݣ�
	 * 		���ӱ��GUID
	 * 		��������enemyName
	 * 		����������
	 * 		�����������
	 * 		���ӵ�ǰλ��
	 * 		����װ�ױ��
	 * 		����������
	 **/
	public void initEnemyForce(String mapFilePath) throws FileNotFoundException
	{

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
	
	/**
	 * �жϵ���˫�������Ƿ����ͨ��
	 * */
	public boolean isTongshi()
	{
		return false;
	}
	
	public boolean isZhishi()
	{
		/*�жϵ���˫�������Ƿ����ֱ��*/
		return false;
	}
	
	public String[] check()
	{
		/*���ص�ǰ��������������Ƿ���Է��ֵз�����*/
		return null;
	}
	
	public int[] checkRecon()
	{
		return null;		
	}
	
	public String[] checkPath()
	{/*���شӵ�ǰλ�õ�Ŀ�ĵصĵ�ͼ����·��*/
		return null;		
	}
	

	

}
