package cn.weekdragon.m3u8;

public class testM3U8 {

	public static String s1 = "http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8";

	public static void main(String[] args) {
		try {
			MUtil.downloadM3U8(s1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}