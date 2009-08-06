package de.cachehound.filter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { BearingFilterTest.class, SizeFilterTest.class,
		AndFilterTest.class, DistanceFilterTest.class })
public class AllTests {
	// Nothing to do here
}
