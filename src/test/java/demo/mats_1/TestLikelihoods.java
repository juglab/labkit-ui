package demo.mats_1;

import org.apache.commons.math3.analysis.function.Gaussian;

public class TestLikelihoods {

	private static double testLikelihood(int i) {
		Gaussian gaussian1 = new Gaussian(75, 15);
		Gaussian gaussian2 = new Gaussian(33, 20);

		return gaussian1.value(i) + gaussian2.value(i);
	}

	private static double testLikelihood2D(int[] xy) {
		int x   = xy[0];
		int y   = xy[1];
		Gaussian gaussian1 = new Gaussian(75, 15);
		Gaussian gaussian2 = new Gaussian(30, 10);


		Gaussian gaussian3 = new Gaussian(25, 25); // to make it (bi-/)multi-modal
		Gaussian gaussian4 = new Gaussian(80, 13);

		return gaussian1.value(x) * gaussian2.value(y) + gaussian3.value(x) * gaussian4.value(y);
	}

	private static double testLikelihood6D(int[] xy) {
		int x1   = xy[0];
		int x2   = xy[1];
		int x3   = xy[2];
		int x4   = xy[3];
		int x5   = xy[4];
		int x6   = xy[5];

		Gaussian gaussian1 = new Gaussian(65, 15);
		Gaussian gaussian2 = new Gaussian(32, 10);
		Gaussian gaussian3 = new Gaussian(75, 5);
		Gaussian gaussian4 = new Gaussian(50, 15);
		Gaussian gaussian5 = new Gaussian(25, 5);
		Gaussian gaussian6 = new Gaussian(90, 25);

		return gaussian1.value(x1) * gaussian2.value(x2) * gaussian3.value(x3) * gaussian4.value(x4) * gaussian5.value(x5) * gaussian6.value(x6);
	}
}
