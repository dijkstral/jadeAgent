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
	private int m_location;//��ǰλ��
	private int m_nextLocation;//��һ���ƶ�����λ��
	private boolean m_isReady = false;
	
	private int m_adviceMovePoint;
	private float[] m_adviceMoveDirection;
	private float[] m_primaryTargets;
	private boolean m_isEngaged;//�Ƿ��Լ�����˽���
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
		System.out.println("��ʼ����սAgent��");
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
                    if (chiefAgent.ALLSOLDIERISOK.equals( msg.getContent())) //�յ�chief��������Ϣ��ȷ�����Խ�����һ���ж�
                    { 
                        moveToNext();//������ս����ȷ��·����ִ����һ��
                    }
                    else if (chiefAgent.NEXTSTEPOK.equals( msg.getContent())) //�յ�chief������ѯ����Ϣ���ж��Ƿ���Խ�����һ���ж�
                    {
                    	if(m_isReady)
                    	{
                    		//moveToNext();//�������ִ�У��򷵻ؿ��Խ��е��ź�
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
		/*���µ�ǰ�Լ��ĸ���ָ�꣬Ϊ��һ���ľ����ṩ׼������
		 * 
		 * */
		
		
	}	
	

}






