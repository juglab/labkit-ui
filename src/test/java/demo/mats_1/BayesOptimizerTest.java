
package demo.mats_1;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class BayesOptimizerTest {

	@Test
	public void testOneDimensionalProblem() {
		OptimizationProblem optimizationProblem = new OneDimensionalProblem();
		BayesOptimizer optimizer = new BayesOptimizer(optimizationProblem);
		float[] result = optimizer.run();
		assertArrayEquals(new float[] { 75 }, result, 20);
	}

	@Test
	public void testTwoDimensionalProblem() {
		OptimizationProblem optimizationProblem = new TwoDimensionalProblem();
		BayesOptimizer optimizer = new BayesOptimizer(optimizationProblem);
		float[] result = optimizer.run();
		assertArrayEquals(new float[] { 75, 30 }, result, 20);
	}

	@Test
	public void testSixGaussians() {
		OptimizationProblem optimizationProblem = new SixGaussians();
		BayesOptimizer optimizer = new BayesOptimizer(optimizationProblem);
		optimizer.setNumberOfLifePoints(100000);
		optimizer.setNumberOfSteps(2000);
		float[] result = optimizer.run();
		assertArrayEquals(new float[] { 65, 32, 75, 50, 25, 90 }, result, 10);
	}
}

class OneDimensionalProblem implements OptimizationProblem {

	private final Gaussian gaussian1 = new Gaussian(75, 15);
	private final Gaussian gaussian2 = new Gaussian(33, 20);

	@Override
	public int numberOfParameters() {
		return 1;
	}

	@Override
	public float[][] parameterBounds() {
		return new float[][] { { 0, 100 } };
	}

	@Override
	public double likelihood(float[] param) {
		return gaussian1.value(param[0]) + gaussian2.value(param[0]);
	}
}

class TwoDimensionalProblem implements OptimizationProblem {

	private final Gaussian gaussian1 = new Gaussian(75, 15);
	private final Gaussian gaussian2 = new Gaussian(30, 10);
	private final Gaussian gaussian3 = new Gaussian(25, 25); // to make it (bi-/)multi-modal
	private final Gaussian gaussian4 = new Gaussian(80, 13);

	@Override
	public int numberOfParameters() {
		return 2;
	}

	@Override
	public float[][] parameterBounds() {
		return new float[][] { { 0, 100 }, { 0, 100 } };
	}

	@Override
	public double likelihood(float[] param) {
		return gaussian1.value((int) param[0]) * gaussian2.value((int) param[1]) +
			gaussian3.value((int) param[0]) * gaussian4.value((int) param[1]);
	}
}

class SixGaussians implements OptimizationProblem {

	private final Gaussian gaussian1 = new Gaussian(65, 15);
	private final Gaussian gaussian2 = new Gaussian(32, 10);
	private final Gaussian gaussian3 = new Gaussian(75, 5);
	private final Gaussian gaussian4 = new Gaussian(50, 15);
	private final Gaussian gaussian5 = new Gaussian(25, 5);
	private final Gaussian gaussian6 = new Gaussian(90, 25);

	@Override
	public int numberOfParameters() {
		return 6;
	}

	@Override
	public float[][] parameterBounds() {
		return new float[][] { { 0, 100 }, { 0, 100 }, { 0, 100 }, { 0, 100 },
			{ 0, 100 }, { 0, 100 } };
	}

	@Override
	public double likelihood(float[] param) {

		return gaussian1.value(param[0]) * gaussian2.value(param[1]) *
			gaussian3.value(param[2]) * gaussian4.value(param[3]) *
			gaussian5.value(param[4]) * gaussian6.value(param[5]);
	}
}
