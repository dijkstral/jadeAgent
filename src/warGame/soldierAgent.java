package warGame;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import party.HostAgent;

public class soldierAgent extends Agent{
	private AID m_GUID;
	private int m_movePoint;
	private int m_armor;
	private int[] m_weapons;
	private String m_agentName;
	private int m_location;//当前位置
	private int m_nextLocation;//下一步移动到的位置
	private boolean m_isReady = false;
	
	private int m_adviceMovePoint;
	private float[] m_adviceMoveDirection;
	private float[] m_primaryTargets;
	private boolean m_isEngaged;//是否以及与敌人交火
	private float[] m_engageResult;
	private float[] m_underAttackPossibility;
	
	private void init(AID GUID,int movePoint,int armor,int[] weapons,String agentName)
	{
		m_GUID = GUID;
		m_movePoint = movePoint;
		m_armor = armor;
		m_weapons = weapons.clone();
		m_agentName = agentName;		
		
		m_adviceMovePoint = m_movePoint;
		m_adviceMoveDirection = new float[4];
		for(int i=0;i<m_adviceMoveDirection.length;i++)
			m_adviceMoveDirection[i] = 1/4;
		
		m_isEngaged = false;		
	}
	
	protected void setup() {
		System.out.println("初始化作战Agent：");
		Object[] args = getArguments();
		//init();	
		
		addBehaviour(new CyclicBehaviour(this)
		{
            public void action() 
            {
                // listen if a greetings message arrives
                ACLMessage msg = receive( MessageTemplate.MatchPerformative( ACLMessage.INFORM ) );

                if (msg != null) 
                {
                    if (chiefAgent.ALLSOLDIERISOK.equals( msg.getContent())) //收到chief发来的信息，确定可以进行下一步行动
                    { 
                        moveToNext();//所有作战算子确定路径，执行下一步
                    }
                    else if (chiefAgent.NEXTSTEPOK.equals( msg.getContent())) //收到chief发来的询问信息，判断是否可以进行下一步行动
                    {
                    	if(m_isReady)
                    	{
                    		//moveToNext();//如果可以执行，则返回可以进行的信号
                    		ACLMessage reply = msg.createReply();
                    		reply.setContent(chiefAgent.NEXTSTEPOK);
                    		myAgent.send(reply);
                    	}
                    }
                    
                   /* else if (msg.getContent().startsWith( HostAgent.INTRODUCE )) 
                    {
                        // I am being introduced to another guest
                        introducing( msg.getContent().substring( msg.getContent().indexOf( " " ) ) );
                    }
                    else if (msg.getContent().startsWith( HostAgent.HELLO )) 
                    {
                        // someone saying hello
                        passRumour( msg.getSender() );
                    }
                    else if (msg.getContent().startsWith( HostAgent.RUMOUR )) 
                    {
                        // someone passing a rumour to me
                        hearRumour();
                    }
                    else 
                    {
                        System.out.println( "Guest received unexpected message: " + msg );
                    }*/
                }
                else 
                {
                    // if no message is arrived, block the behaviour
                    block();
                }
            }
        } );
		
	}
	
	public void moveToNext()
	{
		
	}
	
	public void updateNextStep()
	{
		
	}
	
	public void updateCurrentCharacter()
	{
		/*更新当前自己的各项指标，为下一步的决策提供准备工作
		 * 
		 * */
		
		
	}	
	

}






