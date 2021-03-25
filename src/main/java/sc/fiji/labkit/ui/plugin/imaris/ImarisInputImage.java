package sc.fiji.labkit.ui.plugin.imaris;

import Imaris.Error;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import com.bitplane.xt.ImarisDataset;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.InputImage;

public class ImarisInputImage< T extends NativeType< T > & RealType< T > >
	implements InputImage
{

	private final ImarisDataset< T > dataset;

	private final ImgPlus< T > image;

	private final ImarisShowable showable;

	private final String defaultLabelingFilename;

	public ImarisInputImage(final ImarisDataset< T > dataset)
	{
		this.dataset = dataset;
		try {
			image = dataset.asImgPlus();
			showable = new ImarisShowable();
			defaultLabelingFilename = dataset.getFilename();
		}
		catch (Error error) {
			throw new RuntimeException(error);
		}
	}

	@Override
	public ImgPlus< ? extends NumericType< ? > > imageForSegmentation() {
		return image;
	}

	@Override
	public BdvShowable showable() {
		return showable;
	}

	@Override
	public String getDefaultLabelingFilename() {
		return defaultLabelingFilename;
	}

	public class ImarisShowable implements BdvShowable {

		private final AffineTransform3D sourceTransform = new AffineTransform3D();

		ImarisShowable()
		{
			dataset.getSources().get( 0 ).getSpimSource().getSourceTransform(0,0,sourceTransform );
		}

		public ImarisDataset<T> getDataset()
		{
			return dataset;
		}

		@Override
		public Interval interval() {
			return image;
		}

		@Override
		public AffineTransform3D transformation() {
			return sourceTransform;
		}

		@Override
		public BdvStackSource< ? > show(String title, BdvOptions options) {
			return BdvFunctions.show( dataset, options );
		}
	}
}
