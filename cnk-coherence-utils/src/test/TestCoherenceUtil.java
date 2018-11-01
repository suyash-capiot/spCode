package test;

import com.coxandkings.coherence.utils.CoherenceUtil;

public class TestCoherenceUtil {
	public static void main(String[] args) {
		CoherenceUtil.putInCache("D:/Temp/coherence/AccoCacheConfigAdapter.xml", "AccoCache", "accoKey880001", "Yet another message from CoherenceUtil Test");
		System.out.println(CoherenceUtil.getFromCache("D:/Temp/coherence/AccoCacheConfigAdapter.xml", "AccoCache", "accoKey880001"));
		System.exit(0);
	}

}
