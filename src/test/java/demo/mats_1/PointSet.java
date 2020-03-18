package demo.mats_1;

import weka.core.*;
import weka.core.neighboursearch.KDTree;

import java.util.ArrayList;

/**
 * Set of points, that may be asked for the shortest distances between a
 * given point and a point of the set.
 */
public class PointSet {

	private final KDTree tree = new KDTree();
	private final Instances wekaPoints1;

	public PointSet(final float[] pointsx, final float[] pointsy) {
		wekaPoints1 = insertIntoWeka(pointsx, pointsy, "wekaPoints1");

		try {
			tree.setInstances(wekaPoints1);

			EuclideanDistance df = new EuclideanDistance(wekaPoints1);
			df.setDontNormalize(true);

			tree.setDistanceFunction(df);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Shortest distance between the point given by the coordinates (x, y)
	 * and a point in the point set.
	 */
	public float distanceTo(final float x, final float y) {
		try {
			final Instance point = createInstance(x, y, wekaPoints1);
			Instance neighbor = tree.kNearestNeighbours(point, 1).instance(0);
			DistanceFunction df = tree.getDistanceFunction();
			df.distance(neighbor, point);
			return (float) df.distance(neighbor, point);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float[] closestPoint(final float pointx, final float pointy) {
		try {
			final Instance point = createInstance(pointx, pointy, wekaPoints1);
			Instance neighbor = tree.kNearestNeighbours(point, 1).instance(0);
			return new float[] { (float) neighbor.value(0), (float) neighbor.value(1) };
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a Weka data structure out of a List of 3D Points
	 *
	 * @param pointsx - List of x-coordinates of 2D Points
	 * @param pointsy - List of y-coordinates of 2D Points
	 * @param name    - Instance name
	 * @return Instances containing all 3D points
	 */
	private Instances insertIntoWeka(final float[] pointsx,
			final float[] pointsy, final String name)
	{
		// Create numeric attributes "x" and "y"
		ArrayList< Attribute > attributes = new ArrayList<>(2);
		attributes.add(new Attribute("x"));
		attributes.add(new Attribute("y"));

		// Create the empty datasets "wekaPoints" with above attributes
		Instances wekaPoints = new Instances(name, attributes, 0);

		for (int i = 0; i < pointsx.length - 10; i++)
			wekaPoints.add(createInstance(pointsx[i], pointsy[i], wekaPoints));

		return wekaPoints;
	}

	/**
	 * Create an Instance of the same type as the Instances object you are searching in.
	 *
	 * @param pointx  - List of x-coordinates of 2D Points
	 * @param pointy  - List of y-coordinates of 2D Points
	 * @param dataset - the dataset you are searching in, which was used to build the KDTree
	 * @return an Instance that the nearest neighbor can be found for
	 */
	private Instance createInstance(final float pointx, final float pointy,
			final Instances dataset)
	{
		Instance inst = new DenseInstance(2);
		inst.setValue(0, pointx);
		inst.setValue(1, pointy);
		inst.setDataset(dataset);
		return inst;
	}
}
