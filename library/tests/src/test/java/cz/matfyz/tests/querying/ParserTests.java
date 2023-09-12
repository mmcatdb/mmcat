package cz.matfyz.tests.querying;

import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserTests.class);

    static Stream<Arguments> parserArguments() {
        return Stream.of(examples).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("parserArguments")
    public void parserTest(String queryString) {
        LOGGER.info("Parsing query:\n{}", queryString);
        Assertions.assertDoesNotThrow(() -> {
            Query query = QueryParser.run(queryString);
            LOGGER.info("Parsed result:\n{}", query);
        });
    }
    
    private static final String[] examples = {
        """
        SELECT {
            ?order orderId ?orderId ;
                customerName ?customerName ;
                customerSurname ?customerSurname .
        }
        WHERE {
            ?customer -9 ?order ;
                2 ?customerName ;
                3 ?customerSurname .

            ?order 10 ?orderId .

            FILTER(?customerName = "Alice")
        }
        """,
        """
        SELECT {
            ?order itemId ?itemId ;
                status ?status ;
                customerName ?name ;
                city ?city .
        }
        WHERE {
            ?order 10 ?id ;
                11 ?status ;
                12/14 ?itemId ;
                9 ?customer .
        
            ?customer 2 ?name ;
                4/7 ?city .
        
            FILTER(?name = "Alice")
            VALUES ?status {"completed" "shipped"}
        }
        """,
    """
        SELECT {
            ?customer name ?name ;
                surname ?surname .
        }
        WHERE {
            ?customer 1 ?id ;
                2 ?name ;
                3 ?surname .
        }
        """,
        """
        SELECT {
            ?customer name ?name ;
                surname ?surname ;
                street ?street ;
                city ?city ;
                zipCode ?zipCode .
        }
        WHERE {
            ?customer 1 ?customerId ;
                2 ?name ;
                3 ?surname ;
                4 ?address .
        
            ?address 5 ?addressId ;
                6 ?street ;
                7 ?city ;
                8 ?zipCode .
        }
        """,
        """
        SELECT {
            ?address street ?street ;
                surname ?surname .
        }
        WHERE {
            ?address 5 ?id ;
                6 ?street ;
                -4/3 ?surname .
        }
        """,
    };

}
