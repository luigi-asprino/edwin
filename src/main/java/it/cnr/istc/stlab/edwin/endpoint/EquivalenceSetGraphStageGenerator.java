package it.cnr.istc.stlab.edwin.endpoint;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.StageGeneratorGenericStar;

public class EquivalenceSetGraphStageGenerator extends StageGeneratorGenericStar {
	
	@Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
		return super.execute(pattern, input, execCxt);
		
	}


}
