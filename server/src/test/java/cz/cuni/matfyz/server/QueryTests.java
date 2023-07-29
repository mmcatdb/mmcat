package cz.cuni.matfyz.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.cuni.matfyz.server.controller.QueryController;
import cz.cuni.matfyz.server.controller.QueryController.QueryInput;
import cz.cuni.matfyz.server.entity.Id;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author jachymb.bartik
 */
@SpringBootTest
public class QueryTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTests.class);
    
    @Autowired
    private QueryController queryController;

    @ParameterizedTest
    @MethodSource("basicQueryArguments")
    public void basicQuery_doesNotThrow(String queryString) {
        final var input = new QueryInput(new Id("1"), queryString);

        assertDoesNotThrow(() -> {
            final var result = queryController.executeQuery(input);
            LOGGER.info("Query result:\n{}", result.jsonValues());
        });
    }

    @ParameterizedTest
    @MethodSource("multipleDatabasesQueryArguments")
    public void multipleDatabasesQuery_doesNotThrow(String queryString) {
        final var input = new QueryInput(new Id("1"), queryString);

        assertDoesNotThrow(() -> {
            final var result = queryController.executeQuery(input);
            LOGGER.info("Query result:\n{}", result.jsonValues());
        });
    }

    static Stream<Arguments> basicQueryArguments() {
        return Stream.of(basicQuery).map(Arguments::of);
    }

    private static final String[] basicQuery = new String[] {
        """
        SELECT {
            ?customer has ?id .
        }
        WHERE {
            ?customer 1 ?id .
        }
            """,
        """
        SELECT {
            ?customer has ?id .
        }
        WHERE {
            ?id -1 ?customer .
        }
            """,
        """
        SELECT {
            ?customer has ?id .
        }
        WHERE {
            ?customer 1 ?id .

            FILTER(?id = \"1\")
        }
            """,
        """
        SELECT {
            ?customer has ?id .
        }
        WHERE {
            ?customer 1 ?id .

            FILTER(?id = \"1\")
            FILTER(?id != \"3\")
        }
            """
    };

    static Stream<Arguments> multipleDatabasesQueryArguments() {
        return Stream.of(multipleDatabasesQuery).map(Arguments::of);
    }

    private static final String[] multipleDatabasesQuery = new String[] {
        """
        SELECT {
            ?order customer_id ?id ;
                number ?number .
        }
        WHERE {
            ?order 2 ?number ;
                3/1 ?id .
        }
            """,
        """
        SELECT {
            ?order customer_id ?id ;
                number ?number .
        }
        WHERE {
            ?order 2 ?number ;
                3 ?customer .
            ?customer 1 ?id .
        }
            """,
        """
        SELECT {
            ?order customer_id ?id ;
                number ?number .
        }
        WHERE {
            ?order 2 ?number ;
                3/1 ?id .

            FILTER(?number = \"1\")
        }
            """,
        """
        SELECT {
            ?order customer_id ?id ;
                number ?number .
        }
        WHERE {
            ?order 2 ?number ;
                3/1 ?id .

            FILTER(?id = \"1\")
        }
            """
    };

}
