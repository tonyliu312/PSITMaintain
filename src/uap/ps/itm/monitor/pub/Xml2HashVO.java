package uap.ps.itm.monitor.pub;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * �������ǽ�xml�ļ��Ľ����ת��ΪhashVO.����xml���ֶ�����ת��ΪHASHVO.
 * ��Ϊ����ת�������ԣ����Բ�֧�����ת����
 */
public class Xml2HashVO {

	public static HashVO[] convertToVO(Node node, String nodeName)
			throws Exception {
		if (nodeName == null)
			throw new Exception("nodename can't be null");
		// �õ��ӽڵ㡣
		NodeList nodelist = node.getChildNodes();
		ArrayList<HashVO> al = new ArrayList<HashVO>();
		HashVO vo = null;
		String name = null;
		String value = null;

		// ���ڵ�
		if (node.getNodeName().equalsIgnoreCase(nodeName)) {
			vo = new HashVO();
			for (int i = 0; i < nodelist.getLength(); i++) {
				name = null;
				value = null;
				if (nodelist.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;
				name = nodelist.item(i).getNodeName();
				if (nodelist.item(i).getChildNodes().getLength() > 0)
					value = nodelist.item(i).getChildNodes().item(0)
							.getNodeValue();
				vo.setAttributeValue(name, value);
			}
			return new HashVO[] { vo };
		}
		for (int i = 0; i < nodelist.getLength(); i++) {
			// �������е��ӽڵ㡣������ֵ����hashVO.
			Node childnode = nodelist.item(i);
			if (!childnode.getNodeName().equalsIgnoreCase(nodeName)
					&& nodeName != null)
				continue;
			vo = new HashVO();
			al.add(vo);
			NodeList cclist = childnode.getChildNodes();
			for (int j = 0; j < cclist.getLength(); j++) {
				name = null;
				value = null;
				if (cclist.item(j).getNodeType() != Node.ELEMENT_NODE)
					continue;
				name = cclist.item(j).getNodeName();
				if (cclist.item(j).getChildNodes().getLength() > 0)
					value = cclist.item(j).getChildNodes().item(0)
							.getNodeValue();
				vo.setAttributeValue(name, value);
			}
		}
		return (HashVO[]) al.toArray(new HashVO[al.size()]);
	}

	public static HashVO[] convertToVO(String fileURL, String nodeName)
			throws Exception {
		InputStream is = null;
		try {
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			is = new FileInputStream(fileURL);
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();
			return convertToVO(root, nodeName);
		} finally {
			try {
				is.close();
			} catch (Exception e) {

			}
		}
	}

	public static HashVO[] convertToVO(InputStream is, String nodeName)
			throws Exception {
		try {
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();
			return convertToVO(root, nodeName);
		} finally {
			try {
				is.close();
			} catch (Exception e) {

			}
		}
	}

	public static final void main(String[] args) {
		try {
			HashVO[] vos = convertToVO("d:/monitor/server/conf/services.xml",
					"servicecenter");
			System.out.println(vos.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
