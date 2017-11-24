package cn.weekdragon.m3u8;

import java.io.File;


public class TempFile {

	/**
	 * get a unique tmp folder, should delete after no use
	 * 
	 * @param preFolder
	 * @return
	 * @throws Exception
	 */
	public synchronized static File getTmpFolder(String preFolder) throws Exception {
		String path = preFolder;
		File parent = new File(path);
		if (!parent.exists()) {
			parent.mkdirs();
		}
		for (int i = 0; i < 1000; i++) {
			long time = System.currentTimeMillis();

			File tmp = new File(path + File.separator + time);
			if (!tmp.exists()) {
				boolean res = tmp.mkdirs();
				if (!res) {
					System.out.println("failed to create tmp folder "+ tmp.getAbsolutePath());
				}
				return tmp;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("failed to get tmp  folder of parent path " + path);
		throw new Exception("Can't create more tmp file");
	}
}
