
package demo.mats_1;

public interface OptimizationProblem {

	int numberOfParameters();

	float[][] parameterBounds();

	double likelihood(float[] param);
}
