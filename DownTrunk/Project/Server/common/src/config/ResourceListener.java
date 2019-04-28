package config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.ErrorPrint;

public class ResourceListener {
	private static ExecutorService fixedThreadPool = Executors.newCachedThreadPool();
	private WatchService ws;
	private String listenerPath;

	private ResourceListener(String path) {
		try {
			ws = FileSystems.getDefault().newWatchService();
			this.listenerPath = path;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}

	private void start() throws ClassNotFoundException {
		fixedThreadPool.execute(new Listner(ws, listenerPath));
	}

	public static void addListener(String path) throws IOException, ClassNotFoundException {
		ResourceListener resourceListener = new ResourceListener(path);
		Path p = Paths.get(path);
		p.register(resourceListener.ws, StandardWatchEventKinds.ENTRY_MODIFY);
		// 如要监控子文件
		File file = new File(path);
		LinkedList<File> fList = new LinkedList<File>();
		fList.addLast(file);
		while (fList.size() > 0) {
			File f = fList.removeFirst();
			if (f.listFiles() == null)
				continue;
			for (File file2 : f.listFiles()) {
				if (file2.isDirectory()) {// 下一级目录
					fList.addLast(file2);
					// 依次注册子目录
					Paths.get(file2.getAbsolutePath()).register(resourceListener.ws,
							StandardWatchEventKinds.ENTRY_MODIFY);
				}
			}
		}

		resourceListener.start();
	}
}
