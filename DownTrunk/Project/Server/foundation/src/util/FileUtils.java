package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	/**
	 * 创建文件夹
	 * 
	 * @param path
	 * @return realPath
	 */
	public static String MakeDirs(String path) {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f.getPath();
	}

	/**
	 * 获取文件类型
	 * 
	 * @param fileName
	 * @return find the file type
	 */
	public static String getFileType(String fileName) {
		int pos = fileName.lastIndexOf(".");
		if (pos < 0) {
			return "";
		} else {
			return fileName.substring(pos + 1);
		}
	}

	/**
	 * 文件拷贝
	 * 
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	public static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName
	 * @param encoding
	 * @return file content
	 */
	public static String readStringFile(String fileName, String encoding) {
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, encoding));
			sb = new StringBuffer();
			while (reader.ready()) {
				String line = reader.readLine();
				sb.append(line);
				sb.append("\r\n");
			}
			reader.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName
	 * @return file content
	 */
	public static String readStringFileByUTF(String fileName) {
		return readStringFile(fileName, "UTF-8");
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName
	 * @return file content
	 */
	public static String readStringFileByGBK(String fileName) {
		return readStringFile(fileName, "GBK");
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName
	 * @return file content
	 */
	public static String readStringFileByISO(String fileName) {
		return readStringFile(fileName, "ISO-8859-1");
	}

	/**
	 * 得到文件路径
	 * 
	 * @param file
	 * @return file real path
	 */
	public static String getFileNamePath(File file) {
		return getFileNamePath(file.getAbsolutePath());
	}

	/**
	 * 得到文件路径
	 * 
	 * @param fileName
	 * @return file real path
	 */
	public static String getFileNamePath(String fileName) {
		int pos = fileName.lastIndexOf("\\");
		int pos2 = fileName.lastIndexOf("/");
		if (pos == -1 && pos2 == -1) {
			return "";
		} else {
			if (pos2 > pos) {
				return fileName.substring(0, pos2);
			} else {
				return fileName.substring(0, pos);
			}
		}
	}

	/**
	 * 在文件末尾追加内容
	 * 
	 * @param addContent
	 * @param fileName
	 * @param encoding
	 * @return add access or fail
	 */
	public static boolean addWriteStringFile(String addContent,
			String fileName, String encoding) {
		String s = readStringFile(fileName, encoding);
		s = s + addContent;
		return writeStringFile(s, fileName, encoding);
	}

	/**
	 * 将内容写入文件里
	 * 
	 * @param fileContent
	 * @param fileName
	 * @param encoding
	 * @return add access or fail
	 */
	public static boolean writeStringFile(String fileContent, String fileName,
			String encoding) {
		try {
			MakeDirs(getFileNamePath(fileName));
			File file = new File(fileName);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			byte[] b = fileContent.getBytes(encoding);
			fileOutputStream.write(b);
			fileOutputStream.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 持贝指定的目录下所有文件及子目录到目标文件夹
	 * 
	 * @param file
	 * @param tofile
	 */
	public static void CopyFolder(File file, File tofile) {
		MakeDirs(getFileNamePath(tofile));
		// 获取源目录下一级所有目录文件
		File[] files = file.listFiles();
		// 逐个判断，创建目录，执行递归调用
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File copyPath = new File(tofile.getAbsolutePath() + "\\"
						+ files[i].getName());
				copyPath.mkdir();
				CopyFolder(files[i], copyPath);
			} else { // 如果file为文件，读取字节流写入目标文件;
				try {
					FileInputStream fiStream = new FileInputStream(files[i]);
					BufferedInputStream biStream = new BufferedInputStream(
							fiStream);
					File copyFile = new File(tofile.getAbsolutePath() + "\\"
							+ files[i].getName());
					copyFile.createNewFile();
					FileOutputStream foStream = new FileOutputStream(copyFile);
					BufferedOutputStream boStream = new BufferedOutputStream(
							foStream);
					int j;
					while ((j = biStream.read()) != -1) {
						boStream.write(j);
					}
					/* 关闭流 */
					biStream.close();
					boStream.close();
					fiStream.close();
					foStream.close();
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 读取文件夹中所有文件名(不包括文件夹中的子目录)
	 * 
	 * @param filePath
	 * @return
	 */
	public static List<String> readDirsAllFiles(String filePath) {
		List<String> fileNameList = new ArrayList<String>();

		File f = new File(filePath);
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				File file = files[i];
				int pos = file.getName().lastIndexOf(".");
				if (pos < 0) {
					fileNameList.add(file.getName());
				} else {
					fileNameList.add(file.getName().substring(0, pos));
				}
			}
		}
		return fileNameList;
	}

	/**
	 * 转换文件大小为KB
	 * 
	 * @param fileLength
	 * @return file KB size
	 */
	public static Float getFileSizeKB(Long fileLength) {
		return new Float(fileLength / 1024);
	}

	/**
	 * 转换文件大小为MB
	 * 
	 * @param fileLength
	 * @return file MB size
	 */
	public static Float getFileSizeMB(Long fileLength) {
		return new Float(fileLength / 1024 / 1024);
	}

	/**
	 * 转换文件大小为KB
	 * 
	 * @param file
	 * @return file KB size
	 */
	public static Float getFileSizeKB(File file) {
		return getFileSizeKB(file.length());
	}

	/**
	 * 转换文件大小为MB
	 * 
	 * @param file
	 * @return file MB size
	 */
	public static Float getFileSizeMB(File file) {
		return getFileSizeMB(file.length());
	}
}