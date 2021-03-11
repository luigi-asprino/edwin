package it.cnr.istc.stlab.edwin.endpoint;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;

public class ESGPropertyFunctionEval extends PropertyFunctionEval {

	public ESGPropertyFunctionEval(PropFuncArgType subjArgType, PropFuncArgType objFuncArgType) {
		super(subjArgType, objFuncArgType);
	}

	@Override
	public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
			ExecutionContext execCxt) {
		return null;
	}

}
