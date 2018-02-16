package net.imglib2.labkit.labeling;

import net.imglib2.RandomAccess;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LabelingTest
{
	@Test
	public void testRenameLabel() {
		// setup
		Labeling labeling = new Labeling( Arrays.asList("foreground", "background"), Intervals.createMinSize(0,0,2,2) );
		addPixelLabel( labeling, "foreground", 0, 0 );
		addPixelLabel( labeling, "background", 1, 1 );
		// process
		labeling.renameLabel( "foreground", "fg" );
		// test
		assertEquals( Collections.singleton("fg"), getPixelLabels( labeling, 0, 0 ) );
		assertEquals( Collections.singleton("background"), getPixelLabels( labeling, 1, 1 ) );
	}

	private void addPixelLabel( Labeling labeling, String value, long... position )
	{
		RandomAccess< ? extends Set< String > > randomAccess = labeling.asImgLabeling().randomAccess();
		randomAccess.setPosition( position );
		randomAccess.get().add( value );
	}

	private Set< String > getPixelLabels( Labeling labeling, long... position )
	{
		RandomAccess< ? extends Set< String > > randomAccess = labeling.asImgLabeling().randomAccess();
		randomAccess.setPosition( position );
		return randomAccess.get();
	}

	@Test
	public void testRenameLabel2() {
		// setup
		Labeling labeling = new Labeling( Arrays.asList("foreground", "background"), Intervals.createMinSize(0,0,2,2) );
		// process
		labeling.renameLabel( "foreground", "fg" );
		// test
		assertEquals( Arrays.asList( "fg", "background" ), labeling.getLabels() );
	}

}
