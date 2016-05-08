package warGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import acoWargame.AcoTsp;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class chiefAgent extends Agent
{
    public final static String ALLSOLDIERISOK = "ALLSOLDIERISOK";
    public final static String NEXTSTEPOK = "NEXTSTEPOK";
    public final static String ENEMYFROMFRIENDS = "ENEMYFROMFRIENDS";
    public final static int chiefAgentNum = 3;
    //public final static String GOODBYE = "GOODBYE";
    //public final static String INTRODUCE = "INTRODUCE";
    //public final static String RUMOUR = "RUMOUR";
    
    protected int agentCount = 0;    
	private boolean m_allAgentIsDone = false;			//所有智能体已经确定行动方案
	protected Vector<?> m_soldierAgents = new Vector<Object>();  	//麾下的智能体
	protected Vector<?> m_enemyInSight;
	protected int[] m_soliders;
	protected int[]	m_enemys;
	protected int[]	m_collision;
	protected int[] m_composetion;
	protected boolean allIsReady = false;//所有其他的chiefAgent都已经准备好
	protected boolean allEnemyReceive = false;//所以其他的chiefAgent的E列表都已经接到
	protected Vector<AID> m_allChiefAgentAID;
		
	/**
	 * 初始化列表，
	 * 测试阶段，暂时生成若干个己方算子代号和敌方算子代号
	 * */
	protected void init()
	{
		Random t_random = new Random();
		int soliderNum = t_random.nextInt(10);
		int enemyNum = t_random.nextInt(10);
		m_soliders = new int[soliderNum];
		m_soliders = (int[])getRandomList(soliderNum);//.clone();
		m_composetion = new int[soliderNum];

		m_enemys = new int[enemyNum];
		m_enemys = (int[])getRandomList(enemyNum);//.clone();
		m_collision = new int[enemyNum];	
		for(int i=0;i<enemyNum;i++)
			m_collision[i] = 0;
		
		m_allChiefAgentAID = new Vector<AID>();
	}
	
	/**
	 * 随机生成一个列表，等于随机指向目标算子和己方算子
	 * */
	protected int[] getRandomList(int n)
	{		
		if(n<=0)
			return null;
		Random r = new Random();	
		int randomCount = 0;
		int[] t_randomList = new int[20];	
		int[] t_list = new int[n];
		for(int i=0;i<20;i++)
			t_randomList[i] = i;
		
		for(int i = 0; i < n; i++)
		{
			randomCount = r.nextInt(20);
			while(t_randomList[randomCount]==0)
				randomCount = r.nextInt(20);
			t_list[i] = t_randomList[randomCount];
			t_randomList[randomCount] = 0;
			//System.out.print(a.targets[i]);
		}	
		return t_list;
	}
	
	/**
	 * 将整型列表转化成为字符串发出
	 * */
	protected String makeIntListToString(int[] i_list)
	{
		if(i_list.length<=0)
			return "";
		String t_String  = "";
		for(int i=0;i<i_list.length;i++)
			t_String +=String.valueOf(i_list[i]);
		return t_String;
	}
	
	/**
	 * 将字符串转化为整型列表
	 * */
	protected int[] makeStringToIntList(String i_String)
	{
		int[] t_list = new int[i_String.length()];
		for(int i=0;i<i_String.length();i++)
			t_list[i] = i_String.charAt(i)-48;
		return t_list;
	}
	
	private MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
			MessageTemplate.MatchOntology("presence") );
	
	/**
	 * 向所有单位发出自己准备好的信号
	 * 
	 * */
	protected void readyToGo()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(NEXTSTEPOK);
		for (int i = 0; i < chiefAgentNum; ++i) 
		{
			AID t_AID = new AID("chiefAgent" + i, AID.ISLOCALNAME);
			if(t_AID != getAID())
				msg.addReceiver(t_AID);
		} 
        send(msg);          
	}
	
	/**
	 * 向所有单位发出自己的E列表信号
	 * 
	 * */	
	protected void sendEnemyList()	
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology(ENEMYFROMFRIENDS);
		msg.setContent(makeIntListToString(m_enemys));
		for (int i = 0; i < chiefAgentNum; ++i) 
		{
			AID t_AID = new AID("chiefAgent" + i, AID.ISLOCALNAME);
			if(t_AID != getAID())
				msg.addReceiver(t_AID);
		} 
        send(msg);          
	}
	
	/**
	 * 更新冲突列表内的频值
	 * */	
	protected void updateCollisionList(int[] i_list)
	{
		for(int i=0;i<i_list.length;i++)
		{
			for(int j=0;j<m_enemys.length;j++)
			{
				if(i_list[i]==m_enemys[j])
					m_collision[j]++;
			}
		}
	} 
	
	/**
	 * 通过蚁群算法计算最佳组合
	 * */
	protected void getComposetion()
	{
		System.out.println(getAID().getName()+" in ACO.");
		String[] args = {"-i","F:/ACOTSPJava-master/tsp/d1291.tsp"};
		AcoTsp.main(args);
	}
	

	protected void setup()
	{	
		m_soldierAgents.clear();
		init();
		Object[] args = getArguments();
		System.out.println(getAID().getName()+" is ready.");
		System.out.println("soliderList is " +makeIntListToString(m_soliders)+"; enemyList is " +makeIntListToString(m_enemys));
			
		addBehaviour(new TickerBehaviour(this, 1000) //每隔0.3s，广播智能体是否做好准备
					{
						protected void onTick() 
						{								
							if(!allIsReady)
								readyToGo();
							else if(!allEnemyReceive)
								sendEnemyList();
							//else if(allIsReady&&allEnemyReceive)
																
							
							ACLMessage reply = receive();
							
							if (reply != null) 
							{
								//System.out.println(getAID().getName()+" get Message from " + reply.getSender().getName());
								if(!ENEMYFROMFRIENDS.equals(reply.getOntology()))
								{
									//System.out.println(getAID().getName());
									if (!allIsReady&&NEXTSTEPOK.equals( reply.getContent() )) //智能体回复准备好了
									{
										System.out.println(getAID().getName()+" get ReadyMessage from " + reply.getSender().getName());
										boolean AIDinVector = false;
										for(int i=0;i<m_allChiefAgentAID.size();i++)
										{
											if(reply.getSender() == m_allChiefAgentAID.get(i))
												AIDinVector = true;											
										}
										if(!AIDinVector&&reply.getSender()!=getAID())
											m_allChiefAgentAID.add(reply.getSender());
										
										if(m_allChiefAgentAID.size()==chiefAgentNum-1)
										{
											allIsReady = true;
											m_allChiefAgentAID.removeAllElements();
											System.out.println(getAID().getName()+" declear MINE is all ready");										
										}
									}
								}
								else if(!allEnemyReceive)
								{
									System.out.print(getAID().getName()+" get EnemyMessage from "+ reply.getSender().getName());
									String replyMessage = reply.getContent();
									
									System.out.println("Message says his EnemyList is " + replyMessage);
									int[] t_list = makeStringToIntList(replyMessage);
									updateCollisionList(t_list);
									
									boolean AIDinVector = false;
									for(int i=0;i<m_allChiefAgentAID.size();i++)
									{
										if(reply.getSender() == m_allChiefAgentAID.get(i))
											AIDinVector = true;											
									}
									if(!AIDinVector&&reply.getSender()!=getAID())
										m_allChiefAgentAID.add(reply.getSender());
									if(m_allChiefAgentAID.size()==chiefAgentNum)
									{
										allEnemyReceive = true;
										System.out.println(getAID().getName()+" declear MINE ENEMY is all ready");
										//m_allChiefAgentAID.removeAllElements();
										getComposetion();
									}
									
								}
							}
							else 
							{
				                // if no message is arrived, block the behaviour
									block();
							}
						}
					});		
		
/*
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive(template);
				if (msg != null) {
					System.out.println("Received QUERY_IF message from agent "+msg.getSender().getName());
					ACLMessage reply = msg.createReply();
					if ("alive".equals(msg.getContent())) {
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("alive");
					}
					else {
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("Unknown-content");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		} );*/
		
	}	
	
	
	
	/*
	protected void setup()
	{
		m_soldierAgents.clear();
		init();
		Object[] args = getArguments();
		try
		{
			Scanner in = new Scanner(new File((String) args[0]));
			while(in.hasNextLine())
			{				
				String str = in.nextLine();
				String[] soldierAgentInfo = splitStr(str);
				
				PlatformController container = getContainerController();				
				AgentController soldierAgent = container.createNewAgent(soldierAgentInfo[0], "soldierAgent", soldierAgentInfo);
				soldierAgent.start();	
				m_soldierAgents.addElement(soldierAgent);
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (ControllerException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addBehaviour(new TickerBehaviour(this, 60000) //每隔6s，广播智能体是否做好准备
		{
			protected void onTick() 
			{
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);//广播所有智能体，是否已经准备好下一步行动
	            msg.setContent(NEXTSTEPOK);
				for (int i = 0; i < m_soldierAgents.size(); ++i) 
				{
					msg.addReceiver((AID)m_soldierAgents.elementAt(i));
				} 
	            send(msg);			
			
				ACLMessage reply = receive();
				
		        if (reply != null) 
		        {
		        	if (NEXTSTEPOK.equals( reply.getContent() )) //智能体回复准备好了
		        	{
		                    // a guest has arrived
		        		agentCount++;
		        		if (agentCount == m_soldierAgents.size()) //所有智能体都已经准备好了
		        		{
		        			System.out.println( "All Agent are Ready, starting nextStep" );
		        			myAgent.addBehaviour(new sendNextStepSignal());//发出执行下一步命令
		        		}
		             }
		        	else if (RUMOUR.equals( msg.getContent() )) 
		        	{
		                    // count the agents who have heard the rumour
		                    incrementRumourCount();
		                }
		                else if (msg.getPerformative() == ACLMessage.REQUEST  &&  INTRODUCE.equals( msg.getContent() )) {
		                    // an agent has requested an introduction
		                    doIntroduction( msg.getSender() );
		                }
		        }
		        else 
		        {
		                // if no message is arrived, block the behaviour
		                block();
		         }
		        }

		});
	}*/	
	
	public String[] splitStr(String str)//解析字符串
	{
		String childrenStr = str.trim();
		String[] abc = childrenStr.split("[\\p{Space}]+");
        /*String GUID = abc[0];
        String str2 = abc[1];
        System.out.println(str1);
        System.out.println(str2);*/
        return abc;	
	}
	
	private class sendNextStepSignal extends Behaviour//发送通知，执行下一步命令
	{
		//private MessageTemplate mt; // The template to receive replies

		public void action() 
		{			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent(ALLSOLDIERISOK);
			for (int i = 0; i < m_soldierAgents.size(); ++i) 
			{
				msg.addReceiver((AID)m_soldierAgents.elementAt(i));
			} 
            send(msg);
            agentCount = 0;//重置智能体计数
		}

		@Override
		public boolean done() 
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private void ACO()
	{
		
	}
}
