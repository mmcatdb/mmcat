package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.statements.DDLStatement;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class PullToDDLAndDMLTestBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PullToDDLAndDMLTestBase.class);

    private final String fileNamePrefix = "pullToDDLAndDML/";

    private final AbstractPullWrapper pullWrapper;
    private final AbstractDDLWrapper ddlWrapper;
    private final AbstractPushWrapper pushWrapper;

    public PullToDDLAndDMLTestBase(AbstractPullWrapper pullWrapper, AbstractDDLWrapper ddlWrapper, AbstractPushWrapper pushWrapper)
    {
        this.pullWrapper = pullWrapper;
        this.ddlWrapper = ddlWrapper;
        this.pushWrapper = pushWrapper;
    }

    public PullToDDLAndDMLTestBase setAll(String dataFileName, SchemaCategory schema, String rootName, SchemaObject rootObject, ComplexProperty path)
    {
        return setDataFileName(dataFileName).setSchema(schema).setRootName(rootName).setRootObject(rootObject).setPath(path);
    }

    private String dataFileName;

    public PullToDDLAndDMLTestBase setDataFileName(String dataFileName)
    {
        this.dataFileName = dataFileName;
        return this;
    }

    private SchemaCategory schema;

    public PullToDDLAndDMLTestBase setSchema(SchemaCategory schema)
    {
        this.schema = schema;
        
        LOGGER.trace("Schema Category set:\n" + schema);

        return this;
    }

    private String rootName;

    public PullToDDLAndDMLTestBase setRootName(String rootName)
    {
        this.rootName = rootName;

        return this;
    }

    private SchemaObject rootObject;

    public PullToDDLAndDMLTestBase setRootObject(SchemaObject rootObject)
    {
        this.rootObject = rootObject;

        return this;
    }

    private ComplexProperty path;

    public PullToDDLAndDMLTestBase setPath(ComplexProperty path)
    {
        this.path = path;

        LOGGER.trace("Access Path set:\n" + path);

        return this;
    }

	public void testAlgorithm()
    {
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        ForestOfRecords forest;
        try
        {
		    forest = pullWrapper.pullForest(path, new PullWrapperOptions.Builder().buildWithKindName(rootName));
        }
        catch (Exception e)
        {
            Assertions.fail("Exception thrown when building forest.");
            return;
        }

        LOGGER.trace("Pulled Forest Of Records:\n" + forest);
        
		Mapping mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, null, null);

		var transformation = new ModelToCategory();
		transformation.input(mapping, instance, forest);
		transformation.algorithm();

        LOGGER.trace("Created Instance Category:\n" + instance);
        
        var ddlAlgorithm = new DDLAlgorithm();
        ddlAlgorithm.input(schema, instance, rootName, path, ddlWrapper);
        DDLStatement ddlStatement = ddlAlgorithm.algorithm();

        LOGGER.info("Created DDL Statement:\n" + ddlStatement);

        var dmlAlgorithm = new DMLAlgorithm();
        dmlAlgorithm.input(schema, instance, rootName, mapping, pushWrapper);
        List<DMLStatement> dmlStatements = dmlAlgorithm.algorithm();

        LOGGER.info("Created DML Statement-s:\n" + dmlStatements);

        // TODO
	}
}
