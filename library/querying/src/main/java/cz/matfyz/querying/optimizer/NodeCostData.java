package cz.matfyz.querying.optimizer;

/**
* This class contains evaluated or estimated cost data of a {@link cz.matfyz.querying.core.querytree.QueryNode}.
*/
public record NodeCostData(
   /** Cost of sending data over network */
   double network,
   /** Cost of parsing retrieved data (either from db or from some other sending) */
   double dataParse,
   /** Query evaluation cost (filtering, joining, etc.) */
   double queryExecution
) {
   // Later on, this will probably contain more detailed data about the distribution of the query result so it can be used further.
   // Or just split into totalCost + additionalData


   public double total() {
       return network + dataParse + queryExecution;
   }

}
