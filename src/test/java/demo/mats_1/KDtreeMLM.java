package demo.mats_1;

import weka.core.*;
import weka.core.neighboursearch.KDTree;

public class KDtreeMLM {
    /*

        !!!
        The following code was copy paste from:
        https://imagej.net/Using_Weka
        !!!
         */
    /**
     * Creates a Weka Datastructure out of a List of 3D Points
     * @param pointsx - List of x-coordinates of 2D Points
     * @param pointsy - List of y-coordinates of 2D Points
     * @param name - Instance name
     * @return Instances containing all 3D points
     */
    public Instances insertIntoWeka(final float[] pointsx, final float[] pointsy, final String name)
    {
        // Create numeric attributes "x" and "y" and "z"
        Attribute x = new Attribute("x");
        Attribute y = new Attribute("y");
        //Attribute z = new Attribute("z");

        // Create vector of the above attributes
        FastVector attributes = new FastVector(3);
        attributes.addElement(x);
        attributes.addElement(y);
        //attributes.addElement(z);

        // Create the empty datasets "wekaPoints" with above attributes
        Instances wekaPoints = new Instances(name, attributes, 0);

        //for (Iterator<Point3d> i = points.iterator(); i.hasNext();)
        int len_pointsx = pointsx.length;
        for (int j = 0; j < len_pointsx-10; j++)
        {
            // Create empty instance with three attribute values

            Instance inst = new DenseInstance(2);


            /*
            // get the point3d
            Point3d p = i.next();

            // Set instance's values for the attributes "x", "y", and "z"
            inst.setValue(x, p.x);
            inst.setValue(y, p.y);
            inst.setValue(z, p.z);
            */
            inst.setValue(x, pointsx[j]);
            inst.setValue(y, pointsy[j]);
            //inst.setValue(z, 0);

            // Set instance's dataset to be the dataset "wekaPoints"
            inst.setDataset(wekaPoints);

            // Add the Instance to Instances
            wekaPoints.add(inst);
        }

        return wekaPoints;
    }

    /**
     * Create an Instance of the same type as the Instances object you are searching in.
     * @param pointx - List of x-coordinates of 2D Points
     * @param pointy - List of y-coordinates of 2D Points
     * @param dataset - the dataset you are searching in, which was used to build the KDTree
     * @return an Instance that the nearest neighbor can be found for
     */
    public Instance createInstance(final float pointx, final float pointy, final Instances dataset)
    {
        // Create numeric attributes "x" and "y" and "z"
        Attribute x = dataset.attribute(0);
        Attribute y = dataset.attribute(1);
        //Attribute z = dataset.attribute(2);

        // Create vector of the above attributes
        FastVector attributes = new FastVector(3);
        attributes.addElement(x);
        attributes.addElement(y);
        //attributes.addElement(z);

        // Create empty instance with three attribute values
        Instance inst = new DenseInstance(2);

        // Set instance's values for the attributes "x", "y", and "z"
        /*
        inst.setValue(x, p.x);
        inst.setValue(y, p.y);
        inst.setValue(z, p.z);
        */
        inst.setValue(x, pointx);
        inst.setValue(y, pointy);

        // Set instance's dataset to be the dataset "points1"
        inst.setDataset(dataset);

        return inst;
    }

    private final KDTree tree = new KDTree();
    private Instances wekaPoints1;

    public KDtreeMLM(final float[] pointsx, final float[] pointsy) {
        wekaPoints1 = insertIntoWeka(pointsx,pointsy, "wekaPoints1");

        try
        {
            tree.setInstances(wekaPoints1);

            EuclideanDistance df = new EuclideanDistance(wekaPoints1);
            df.setDontNormalize(true);

            tree.setDistanceFunction(df);
        }
        catch (Exception e) { e.printStackTrace();}

        }

    public float query_dist(final float pointx, final float pointy){
        Instance nn1;
        final Instance p = createInstance(pointx,pointy, wekaPoints1);
        try
        {
            Instances neighbors = tree.kNearestNeighbours(p, 1);
            nn1 = neighbors.instance(0);
        }
        catch (Exception e) { nn1 = null; }
        DistanceFunction df = tree.getDistanceFunction();
        df.distance(nn1, p);
        return (float)  df.distance(nn1, p);
    }

    public float[] query_neighbour(final float pointx, final float pointy){
        // returns the pixels of the nearest neighbour
        Instance nn1;
        final Instance p = createInstance(pointx,pointy, wekaPoints1);
        try
        {
            Instances neighbors = tree.kNearestNeighbours(p, 1);
            nn1 = neighbors.instance(0);
        }
        catch (Exception e) { nn1 = null; }

        float[] nn_point =  new float[]{(float) nn1.value(0), (float) nn1.value(1)};

        return nn_point;
    }
}
