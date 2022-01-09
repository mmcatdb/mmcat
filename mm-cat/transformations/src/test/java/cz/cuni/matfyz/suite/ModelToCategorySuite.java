package cz.cuni.matfyz.suite;

import cz.cuni.matfyz.transformations.ModelToCategory1BasicTest;
import cz.cuni.matfyz.transformations.ModelToCategory2StructureTest;

import org.junit.platform.suite.api.*;

/**
 *
 * @author jachymb.bartik
 */
@Suite
@SelectClasses({
    ModelToCategory1BasicTest.class,
    ModelToCategory2StructureTest.class
})
//@SelectPackages("cz.cuni.matfyz.transformations")
public class ModelToCategorySuite
{

}
