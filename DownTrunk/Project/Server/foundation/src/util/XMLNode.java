/**
 * ½âÎöxml£¬xml½Úµã
 */
package util;

import java.util.ArrayList;

public final class XMLNode {

	public String getAttributeValue(String s) {
		String s1 = "";
		if (attributes != null) {
			int i = 0;
			do {
				if (i >= attributes.size())
					break;
				if (attributes.get(i)[0].equals(s)) {
					s1 = attributes.get(i)[1];
					break;
				}
				i++;
			} while (true);
		}
		return s1;
	}

	@Override
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer(1024);
		for (int i = 0; i < level; i++)
			stringbuffer.append('\t');

		stringbuffer.append('<');
		stringbuffer.append(name);
		if (attributes != null && attributes.size() > 0) {
			int j = attributes.size();
			for (int l = 0; l < j; l++) {
				stringbuffer.append(' ');
				String as[] = attributes.get(l);
				stringbuffer.append(as[0]);
				stringbuffer.append("=\"");
				stringbuffer.append(as[1]);
				stringbuffer.append('"');
			}

		}
		stringbuffer.append('>');
		if (subNodes != null) {
			stringbuffer.append("\r\n");
			int k = subNodes.size();
			for (int i1 = 0; i1 < k; i1++) {
				XMLNode xmlnode = subNodes.get(i1);
				xmlnode.setLevel(level + 1);
				String s = xmlnode.toString();
				stringbuffer.append(s);
				stringbuffer.append("\r\n");
			}

			for (int j1 = 0; j1 < level; j1++)
				stringbuffer.append('\t');

		} else {
			stringbuffer.append(data);
		}
		stringbuffer.append("</");
		stringbuffer.append(name);
		stringbuffer.append('>');
		return stringbuffer.toString();
	}

	public XMLNode() {
		this("root");
	}

	public XMLNode(String s) {
		name = "";
		data = "";
		level = 0;
		name = s;
	}

	public XMLNode getSubNode(String s) {
		if (s == null || s.length() == 0)
			return null;
		if (subNodes == null)
			return null;
		for (int i = 0; i < subNodes.size(); i++) {
			XMLNode xmlnode = subNodes.get(i);
			if (s.equals(xmlnode.getName()))
				return xmlnode;
		}

		return null;
	}

	public XMLNode getSubNode(int i) {
		if (i < 0 || i >= subNodes.size())
			return null;
		else
			return subNodes.get(i);
	}

	protected void removeAttribute(int i) {
		if (attributes != null)
			if (i >= 0 && i < attributes.size())
				attributes.remove(i);
	}

	public ArrayList<String[]> getAttributes() {
		return attributes;
	}

	protected void setAttributes(ArrayList<String[]> arraylist) {
		attributes = arraylist;
	}

	public String getData() {
		if (data != null)
			return data;
		else
			return "";
	}

	public void setData(String s) {
		if (s != null && s.startsWith("<![CDATA[")) {
			s = s.substring(9);
			if (s.endsWith("]]>"))
				s = s.substring(0, s.length() - 3);
		}
		data = s;
	}

	public void addAttribute(String s, String s1) {
		if (attributes == null)
			attributes = new ArrayList<String[]>(3);
		attributes.add(new String[] { s, s1 });
	}

	protected void removeSubNode(int i) {
		if (subNodes != null)
			if (i >= 0 && i < subNodes.size())
				subNodes.remove(i);
	}

	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	public ArrayList<XMLNode> getSubNodes() {
		return subNodes;
	}

	protected void setSubNodes(ArrayList<XMLNode> arraylist) {
		subNodes = arraylist;
	}

	public int getLevel() {
		return level;
	}

	protected void setLevel(int i) {
		level = i;
	}

	public void addSubNode(XMLNode xmlnode) {
		if (subNodes == null)
			subNodes = new ArrayList<XMLNode>(5);
		if (xmlnode.getLevel() == 0)
			xmlnode.setLevel(level + 1);
		subNodes.add(xmlnode);
	}

	private String name;

	private String data;

	private ArrayList<String[]> attributes;

	private ArrayList<XMLNode> subNodes;

	private int level;
}
