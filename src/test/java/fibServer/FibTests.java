package fibServer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FibTests {
	static long getFib(int val) {
		return Calculations.getFibNums(val)[val];
	}

	@ParameterizedTest
	@ValueSource(ints = {0 , 1})
	void checkDefaultCases(int val) {
		assertEquals(getFib(val), val, 
				() -> String.format("The default case of %d did not return itself!",val));
	}

	@Test
	void checkGeneralCases() {
		assertEquals(getFib(2),1,
				"Fibonacci of the general case 2 did not return 1!");
		assertEquals(getFib(8),21,
				"Fibonacci of the general case 8 did not return 21!");
	}
	
	@Test
	void checkCachedCases() {
		Calculations.getFibNums(75);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(Calculations.getFibNums(10)[10],55,
				"The caching system is not working, 10 was not 55!");
		
	}
	
	@Test
	void tryToOverrideCache() throws InterruptedException {
		int size = 50;
		for(int j = 0; j<4; j++) {
			Calculations.clearCache();
			for(int i = 0; i<size; i++) {
				Calculations.getFibNums(i);
			}
			Thread.sleep(100);
			assertEquals(Calculations.getCache().length, (size),
					"The caching system failed to cache the largest sequence.");
		}
	}
	@Test
	void validateMaximum() throws InterruptedException {
		assertTrue(getFib(Calculations.MAX_NUMBER)>0,
				"The largest allowed number causes an overflow error!");
	}
}
