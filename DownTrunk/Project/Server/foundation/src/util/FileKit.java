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
	/** 库信息 */
	public static final String toString = "slib.util.FileKit@v1.00";

	/** 一行文字的长度限制 */
	public static final int LEN_LIMIT = 80;

	/** 简单crc32校验的文件限制长度 */
	public static final int SIMPLE_CRC_FILE_LIMIT_SIZE = 4096;

	/** 简单crc32校验的文件头尾取值长度 */
	public static final int SIMPLE_CRC_LENGTH = 1024;

	/* constructors */
	private FileKit() {
	}

	/* static methods */
	/** 将一个字符串写入到指定的文本文件中，自动创建目录，默认为重写方式 */
	public static void string2File(String fileName, String str)
			throws IOException {
		string2File(fileName, str, false);
	}

	/** 将一个字符串写入到指定的文本文件中，自动创建目录，参数append为是否追加方式 */
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

	/** 将一个文本文件中的内容读出，返回一个字符串，每行之间用换行符“\n”隔开 */
	public static String file2String(String fileName) throws IOException {
		return file2String(fileName, true);
	}

	/** 将一个文本文件中的内容读出，返回一个字符串，separator表示是否每行之间用换行符“\n”隔开 */
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

	/** 将一个文本文件中的内容读出，返回一个字符串数组，字符串数组中的每一个元素为文件的一行 */
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

	/** 将一个文件以二进制数据方式读出 */
	public static byte[] file2ByteArray(String fileName) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			// 得到文件大小
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

	/** 将二进制数据方式写入到指定的文件，自动创建目录，默认为重写方式 */
	public static void byteArray2File(String fileName, byte[] data)
			throws IOException {
		byteArray2File(fileName, data, 0, data.length, false);
	}

	/** 将二进制数据方式写入到指定的文件，自动创建目录，默认为重写方式 */
	public static void byteArray2File(String fileName, byte[] data, int offset,
			int len) throws IOException {
		byteArray2File(fileName, data, offset, len, false);
	}

	/** 将二进制数据方式写入到指定的文件，自动创建目录，参数append为是否追加方式 */
	public static void byteArray2File(String fileName, byte[] data,
			boolean append) throws IOException {
		byteArray2File(fileName, data, 0, data.length, append);
	}

	/** 将二进制数据方式写入到指定的文件，自动创建目录，参数append为是否追加方式 */
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
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录）
	 */
	public static String[] listFiles(String directory) {
		return listFiles(directory, true);
	}

	/**
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录） 参数b表示返回的数组中是否包含目录
	 */
	public static String[] listFiles(String directory, boolean b) {
		return listFiles(directory, b, null);
	}

	/**
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录） 参数b表示返回的数组中是否包含目录
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

	/** 遍历指定目录中所有的文件和目录，结果存放到向量中 */
	private static void listFiles(File directory, String path,
			ArrayList<String> fileList, boolean b, String nofile) {
		listFilesFilter(directory, path, fileList, b, nofile);
	}

	// /** 遍历指定目录中所有的文件和目录，结果存放到向量中 */
	// private static void listFiles(File directory, String path,
	// ArrayList fileList, boolean b)
	// {
	// listFilesFilter(directory, path, fileList, b, null);
	// }

	/**
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录）
	 */
	public static String[] listFiles(String directory, String nofile) {
		return listFiles(directory, true, nofile);
	}

	/**
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录）
	 */
	public static String[] listFiles(String directory, String fileter,
			String nofile) {
		return listFiles(directory, true, nofile);
	}

	/** 遍历指定目录中所有的文件和目录，结果存放到向量中 */
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
	 * 遍历指定目录中所有的文件和目录， 返回的字符串数组包含该目录中所有文件和目录的相对地址（相对于该目录） 参数b表示返回的数组中是否包含目录
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

	/** 遍历指定目录中所有的文件和目录，结果存放到向量中 */
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
