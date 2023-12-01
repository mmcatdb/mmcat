package cz.matfyz.tests.example.queryevolution;

public abstract class Queries {

    private Queries() {}

    public static final String findFriends = """
        SELECT {
            _:knows friend ?friendA ;
                friend ?friendB .
        
            ?friendA name ?friendAName ;
                surname ?friendASurname .
        
            ?friendB name ?friendBName ;
                surname ?friendBSurname .
        }
        WHERE {
            ?friendA -4/5 ?friendB .
        
            ?friendA 2 ?friendAName ;
                3 ?friendASurname .
        
            ?friendB 2 ?friendBName ;
                3 ?friendBSurname .
                
            FILTER(?friendA < ?friendB)
        }
        """;

    public static final String mostExpensiveOrder = """
        SELECT {
            SUM(?price) AS ?totalPrice
        }
        WHERE {
            ?orders 12 ?customer ;
                10 ?price .

            ?customer 2 "Anna" .
        }
        ORDER BY (?totalPrice) DESC
        LIMIT 1
        """;

}
