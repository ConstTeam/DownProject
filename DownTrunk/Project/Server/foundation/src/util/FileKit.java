package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileKit {
	/** ����Ϣ */
	public static final String toString = "slib.util.FileKit@v1.00";

	/** һ�����ֵĳ������� */
	public static final int LEN_LIMIT = 80;

	/** ��crc32У����ļ����Ƴ��� */
	public static final int SIMPLE_CRC_FILE_LIMIT_SIZE = 4096;

	/** ��crc32У����ļ�ͷβȡֵ���� */
	public static final int SIMPLE_CRC_LENGTH = 1024;

	/* constructors */
	private FileKit() {
	}

	/* static methods */
	/** ��һ���ַ���д�뵽ָ�����ı��ļ��У��Զ�����Ŀ¼��Ĭ��Ϊ��д��ʽ */
	public static void string2File(String fileName, String str)
			throws IOException {
		string2File(fileName, str, false);
	}

	/** ��һ���ַ���д�뵽ָ�����ı��ļ��У��Զ�����Ŀ¼������appendΪ�Ƿ�׷�ӷ�ʽ */
	@SuppressWarnings("resource")
	public static void string2File(String fileName, String str, boolean append)
			throws IOException {
		File file = new File(fileName);
		String parent = file.getParent();
		if (parent != null) {
			File tree = new File(parent);
			if (!tree.exists())
				tree.mkdirs();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName, append);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(str);
			bw.flush();
		} finally {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** ��һ���ı��ļ��е����ݶ���������һ���ַ�����ÿ��֮���û��з���\n������ */
	public static String file2String(String fileName) throws IOException {
		return file2String(fileName, true);
	}

	/** ��һ���ı��ļ��е����ݶ���������һ���ַ�����separator��ʾ�Ƿ�ÿ��֮���û��з���\n������ */
	public static String file2String(String fileName, boolean separator)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				if (separator)
					sb.append("\n");
			}
			if (sb.length() > 0)
				sb.setLength(sb.length() - 1);
			return sb.toString();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** ��һ���ı��ļ��е����ݶ���������һ���ַ������飬�ַ��������е�ÿһ��Ԫ��Ϊ�ļ���һ�� */
	public static String[] file2StringArray(String fileName) throws IOException {
		List<String> strList = new ArrayList<String>();
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null)
				strList.add(line);
			String[] strs = new String[strList.size()];
			strList.toArray(strs);
			return strs;
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** ��һ���ļ��Զ��������ݷ�ʽ���� */
	public static byte[] file2ByteArray(String fileName) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			// �õ��ļ���С
			int len = bis.available();
			byte[] buffer = new byte[len];
			bis.read(buffer);
			return buffer;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** �����������ݷ�ʽд�뵽ָ�����ļ����Զ�����Ŀ¼��Ĭ��Ϊ��д��ʽ */
	public static void byteArray2File(String fileName, byte[] data)
			throws IOException {
		byteArray2File(fileName, data, 0, data.length, false);
	}

	/** �����������ݷ�ʽд�뵽ָ�����ļ����Զ�����Ŀ¼��Ĭ��Ϊ��д��ʽ */
	public static void byteArray2File(String fileName, byte[] data, int offset,
			int len) throws IOException {
		byteArray2File(fileName, data, offset, len, false);
	}

	/** �����������ݷ�ʽд�뵽ָ�����ļ����Զ�����Ŀ¼������appendΪ�Ƿ�׷�ӷ�ʽ */
	public static void byteArray2File(String fileName, byte[] data,
			boolean append) throws IOException {
		byteArray2File(fileName, data, 0, data.length, append);
	}

	/** �����������ݷ�ʽд�뵽ָ�����ļ����Զ�����Ŀ¼������appendΪ�Ƿ�׷�ӷ�ʽ */
	@SuppressWarnings("resource")
	public static void byteArray2File(String fileName, byte[] data, int offset,
			int len, boolean append) throws IOException {
		if (offset < 0 || offset >= data.length)
			throw new IllegalArgumentException(
					"FileKit byteArray2File, invalid offset:" + offset);
		if (len <= 0 || offset + len > data.length)
			throw new IllegalArgumentException(
					"FileKit byteArray2File, invalid length:" + len);
		File file = new File(fileName);
		String parent = file.getParent();
		if (parent != null) {
			File tree = new File(parent);
			if (!tree.exists())
				tree.mkdirs();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, append);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(data, offset, len);
			bos.flush();
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼��
	 */
	public static String[] listFiles(String directory) {
		return listFiles(directory, true);
	}

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼�� ����b��ʾ���ص��������Ƿ����Ŀ¼
	 */
	public static String[] listFiles(String directory, boolean b) {
		return listFiles(directory, b, null);
	}

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼�� ����b��ʾ���ص��������Ƿ����Ŀ¼
	 */
	public static String[] listFiles(String directory, boolean b, String nofile) {
		File file = new File(directory);
		if (!file.exists())
			return null;
		if (!file.isDirectory())
			return null;
		ArrayList<String> fileList = new ArrayList<String>();
		listFiles(file, "", fileList, b, nofile);
		String[] strs = new String[fileList.size()];
		fileList.toArray(strs);
		return strs;
	}

	/** ����ָ��Ŀ¼�����е��ļ���Ŀ¼�������ŵ������� */
	private static void listFiles(File directory, String path,
			ArrayList<String> fileList, boolean b, String nofile) {
		listFilesFilter(directory, path, fileList, b, nofile);
	}

	// /** ����ָ��Ŀ¼�����е��ļ���Ŀ¼�������ŵ������� */
	// private static void listFiles(File directory, String path,
	// ArrayList fileList, boolean b)
	// {
	// listFilesFilter(directory, path, fileList, b, null);
	// }

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼��
	 */
	public static String[] listFiles(String directory, String nofile) {
		return listFiles(directory, true, nofile);
	}

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼��
	 */
	public static String[] listFiles(String directory, String fileter,
			String nofile) {
		return listFiles(directory, true, nofile);
	}

	/** ����ָ��Ŀ¼�����е��ļ���Ŀ¼�������ŵ������� */
	private static void listFilesFilter(File directory, String path,
			ArrayList<String> fileList, boolean b, String nofile) {
		File[] files = directory.listFiles();
		if (files == null)
			return;
		String name;
		for (int i = 0; i < files.length; i++) {
			name = path + files[i].getName();
			if (nofile != null && name.indexOf(nofile) > 0)
				continue;
			if (files[i].isDirectory()) {
				if (b)
					fileList.add(name);
				listFiles(files[i], name + File.separator, fileList, b, nofile);
			} else
				fileList.add(name);
		}
	}

	/**
	 * ����ָ��Ŀ¼�����е��ļ���Ŀ¼�� ���ص��ַ������������Ŀ¼�������ļ���Ŀ¼����Ե�ַ������ڸ�Ŀ¼�� ����b��ʾ���ص��������Ƿ����Ŀ¼
	 */
	public static File[] getFiles(String directory, boolean b) {
		File file = new File(directory);
		if (!file.exists())
			return null;
		if (!file.isDirectory())
			return null;
		ArrayList<File> fileList = new ArrayList<File>();
		getFiles(file, fileList, b);
		File[] files = new File[fileList.size()];
		fileList.toArray(files);
		return files;
	}

	/** ����ָ��Ŀ¼�����е��ļ���Ŀ¼�������ŵ������� */
	private static void getFiles(File directory, ArrayList<File> fileList,
			boolean b) {
		File[] files = directory.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				if (b)
					fileList.add(files[i]);
				getFiles(files[i], fileList, b);
			} else
				fileList.add(files[i]);
		}
	}

}
