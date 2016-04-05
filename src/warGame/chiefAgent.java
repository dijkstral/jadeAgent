package warGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public class chiefAgent extends Agent
{
    public final static String ALLSOLDIERISOK = "ALLSOLDIERISOK";
    public final static String NEXTSTEPOK = "NEXTSTEPOK";
    //public final static String THANKS = "THANKS";
    //public final static String GOODBYE = "GOODBYE";
    //public final static String INTRODUCE = "INTRODUCE";
    //public final static String RUMOUR = "RUMOUR";
    
    protected int agentCount = 0;    
	private boolean m_allAgentIsDone = false;			//所有智能体已经确定行动方案
	protected Vector m_soldierAgents = new Vector();  	//麾下的智能体
	protected Vector m_enemyInSight;
	
	protected void setup()
	{
		m_soldierAgents.clear();
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
	/*	        	else if (RUMOUR.equals( msg.getContent() )) 
		        	{
		                    // count the agents who have heard the rumour
		                    incrementRumourCount();
		                }
		                else if (msg.getPerformative() == ACLMessage.REQUEST  &&  INTRODUCE.equals( msg.getContent() )) {
		                    // an agent has requested an introduction
		                    doIntroduction( msg.getSender() );
		                }*/
		        }
		        else 
		        {
		                // if no message is arrived, block the behaviour
		                block();
		         }
		        }

		});
	}	
	
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
