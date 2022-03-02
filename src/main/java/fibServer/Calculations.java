package fibServer;

public class Calculations {
	/**
	 * For testing purposes ONLY, NOT THREAD SAFE.
	 */
	static void clearCache() {
			cache = new long[] {1,1};
	}
	static long[] getCache() {
		synchronized(cacheLock) {
			return cache;
		}
	}
	public static final int MAX_NUMBER = 92;
	private static Object cacheLock = new Object();
	private static long[] cache = new long[] {1,1};
	public static long[] getFibNums(int number) {
		long[] numbers = new long[number+1];
		for(int i = 0; i<= number; i++) {
			numbers[i] = getFibNumber(i, numbers);
		}
		new Thread(()->{
			synchronized(cacheLock) {

				if(numbers.length > cache.length) {
					cache = numbers;
				}
			}
		}).start();
		return numbers;
	}
	private static final long getFibNumber(int number, long[] newCache) {
		if(cache.length>number) {
			return cache[number];
		}
		if(newCache.length>number) {
			if(newCache[number] != 0) {
				return newCache[number];
			}
		}
		return getFibNumber(number-1, newCache) + getFibNumber(number-2, newCache);
	}
}
