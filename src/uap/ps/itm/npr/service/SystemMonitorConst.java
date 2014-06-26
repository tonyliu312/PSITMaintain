package uap.ps.itm.npr.service;

/**
 *
 * <code>SystemColor<code>
 * <strong></strong>
 * <p>˵����
 * <li></li>
 * </p>
 * @since NC6.3
 * @version 2013-3-11 ����03:57:47
 * @author tangxx
 */

public interface SystemMonitorConst {

	//cpuһ����ƽ��ʹ������ɫ
	public static final int CPU_AVERAGE = 0xff0000;
	//�ڴ�һ����ƽ��ʹ������ɫ
	public static final int MEM_AVERAGE = 0x00ff00;
	//��������������ʹ������ɫ
	public static final int IF_IN_FLOW = 0x0000ff;
	//������������ʹ���ʵ���ɫ
	public static final int IF_OUT_FLOW = 0x00ffff;

	public static final String CPUAVA = "cpuload%";

	public static final String MENAVA = "memoryused%";

	public static final String IFNETAVA = "networkload%";

	public static final String HOSTNAME = "hostname";

	public static final String HANDLER = "handler";

	public static final String IFINOCTS = "ifinocts";

	public static final String IFOUTOCTS = "ifoutocts";

	public static final String IFSPEED = "ifspeed";
}
